package com.example.camtest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.camtest.Brain.extend_point_list;
import static com.example.camtest.Brain.find_scalars_2;
import static com.example.camtest.Brain.get_absdiff_video;
import static com.example.camtest.Brain.get_border_blinks;
import static com.example.camtest.Brain.get_brightest_spot_from_image;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "myLogs";

    CameraService[] myCameras = null;

    private CameraManager mCameraManager = null;
    private final int CAMERA1 = 0;
    private int count =1;
    private String skyUrl = "";

    private Button mButtonOpenCamera1 = null;
    private Button mButtonRecordVideo = null;
    private Button mButtonStopRecordVideo = null;
    private Button mButtonOpenUrl = null;
    public static TextureView mImageView = null;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler = null;
    private ImageButton settingsButton = null;
    public static int Mode = 0;

    /* 0) Автоматическая подстройка порогов HSV
     *
     * */
    public static boolean automatic_hsv_levels = false;

    /* 1) HSV (цветовой фильтр, настройки маски):
    0..179 Hue (Оттенок), 0..255 Saturation (Насыщенность), 0.255 Value (Яркость)*/
    public static int Hue_low = 0;//0
    public static int Sat_low = 40;//50
    public static int Val_low = 160;//232
    public static int Hue_high = 179;//111
    public static int Sat_high = 255;//113
    public static int Val_high = 255;
    public static Scalar s1 = new Scalar(Hue_low, Sat_low, Val_low);
    public static Scalar s2 = new Scalar(Hue_high, Sat_high, Val_high);

    /* 2) Яркость и контраст:
    ALPHA (яркость) - 1..3
    BETA (контраст) 0..100 */
    public static Integer alpha = 2; // Contrast control (1.0-3.0)
    public static Integer beta = 70;//90

    /* 3) Binary threshold (бинарный фильтр):
    MinVal: 0..255
    MaxVal: 0..255 */
    public static Integer th_MinVal = 100;//230
    public static Integer th_MaxVal = 255;

    /* 4) Submat (настройки подрезка картинки)
    submat_radius: 10..500
    */
    public static int submat_radius = 200;//100

    /* 5) Радиус точки закрашивающей яркие области
    submat_radius: 1..20 */
    public static int radius_for_bright_points_search = 10;//8

    /* 6) Уровень допустимой однородности ярких точек, %
    submat_radius: 0..100 */
    public static int eq_bright_level = 70;

    private File mCurrentFile;

    private MediaRecorder mMediaRecorder = null;

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void loadSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", Context.MODE_PRIVATE); //Считывание данных настроек из файла
        automatic_hsv_levels = sharedPreferences.getBoolean("automatic_hsv_levels", automatic_hsv_levels);
        Mode = sharedPreferences.getInt("Mode", Mode);
        Hue_low = sharedPreferences.getInt("Hue_low", Hue_low);
        Sat_low = sharedPreferences.getInt("Sat_low", Sat_low);
        Val_low = sharedPreferences.getInt("Val_low", Val_low);
        Hue_high = sharedPreferences.getInt("Hue_high", Hue_high);
        Sat_high = sharedPreferences.getInt("Sat_high", Sat_high);
        Val_high = sharedPreferences.getInt("Val_high", Val_high);
        alpha = sharedPreferences.getInt("alpha", alpha);
        beta = sharedPreferences.getInt("beta", beta);
        th_MinVal = sharedPreferences.getInt("th_MinVal", th_MinVal);
        th_MaxVal = sharedPreferences.getInt("th_MaxVal", th_MaxVal);
        submat_radius = sharedPreferences.getInt("submat_radius", submat_radius);
        radius_for_bright_points_search = sharedPreferences.getInt("radius_for_bright_points_search", radius_for_bright_points_search);
        eq_bright_level = sharedPreferences.getInt("eq_bright_level", eq_bright_level);
    }


    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }

        loadSettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //final Stopwatch tim = new Stopwatch();
        //final OpenCV opencv = new OpenCV();
        final TextView textView1 = findViewById(R.id.textView);
        final View view3 = findViewById(R.id.view3);
        //final TextureView textureView1 = findViewById(R.id.textureView);
        final TextView textView2 = findViewById(R.id.textView2);
        //final ToggleButton toggleMode = (ToggleButton) findViewById(R.id.toggleButton1);
        s1 = new Scalar(Hue_low, Sat_low, Val_low);
        s2 = new Scalar(Hue_high, Sat_high, Val_high);


        textView1.setText("Хакатон blink_2020");
        textView2.setText("");

        Log.d(LOG_TAG, "Запрашиваем разрешение");
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }

        settingsButton = findViewById(R.id.imageButton1);
        mButtonOpenCamera1 = findViewById(R.id.button1);
        mButtonRecordVideo = findViewById(R.id.button2);
        mButtonStopRecordVideo = findViewById(R.id.button3);
        mButtonOpenUrl = findViewById(R.id.button4);
        mImageView = findViewById(R.id.textureView);
        mButtonOpenCamera1.setVisibility(View.INVISIBLE);
        mButtonStopRecordVideo.setVisibility(View.INVISIBLE);
        mButtonOpenUrl.setVisibility(View.INVISIBLE);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        mButtonOpenCamera1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myCameras[CAMERA1] != null) {
                    if (!myCameras[CAMERA1].isOpen()) myCameras[CAMERA1].openCamera();
                }
                //CaptureRequest.Builder afBuilder = myCameras[CAMERA1].mPreviewBuilder;
                //myCameras[CAMERA1].mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) 0);
            }
        });

        mButtonRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///
                /*try {
                    myCameras[CAMERA1].mPreviewBuilder = myCameras[CAMERA1].mCameraDevice.createCaptureRequest(myCameras[CAMERA1].mCameraDevice.TEMPLATE_PREVIEW);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                myCameras[CAMERA1].mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 9.5f);
                myCameras[CAMERA1].mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                //myCameras[CAMERA1].mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, 0);
                //myCameras[CAMERA1].mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) 10);
                myCameras[CAMERA1].mPreviewBuilder.build();
                myCameras[CAMERA1].CameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), callback, mHandler);
/////*/
                mButtonOpenUrl.setVisibility(View.INVISIBLE);
                mButtonRecordVideo.setClickable(false);
                if ((myCameras[CAMERA1] != null) & mMediaRecorder != null) {
                    mMediaRecorder.start();

                }
                new CountDownTimer(10000, 100)
                {
                    private double hold = 10;

                    public void onTick(long millisUntilFinished)
                    {
                        // do something every 0.1 seconds...
                        hold = hold - 0.1;

                        if (hold<0.2){
                            textView2.setText("Идет распознавание...");
                        }
                        else{
                            textView2.setText("Удерживайте камеру еще " + String.format("%.1f", hold) + " сек.");
                        }
                    }
                    public void onFinish()
                    {
                        //textView1.setText("Stopped!");
                        textView2.setText("");
                        textView2.setText("Идет распознавание...");
                        skyUrl = "";
                        //myCameras[CAMERA1].mPreviewBuilder.
                        myCameras[CAMERA1].stopRecordingVideo(); //Остановка записи видео
                        //view3.setVisibility(View.INVISIBLE);
                        //textureView1.setVisibility(View.INVISIBLE);
                        //OpenCV.frame(count-1); //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/test" + count + ".mp4";
                        //OpenCV.transform_video(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/test1.mp4");test10avantspace.mp4
                        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //Подгрузка библиотеки
                        //Mode=1;
                        ArrayList<Mat> imageArray = null;
                        System.out.print("Mode ");
                        System.out.println(Mode);
                        if (Mode == 0) { //Созвездие монитор
                            // если изображение не яркое - максимально добавить контрастности
                            imageArray = Brain.transform_video(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/test" + (count-1) + ".mp4", submat_radius); //Разбивка видео по фреймам
                        }
                        if (Mode == 1) { //Созвездие матрица
                            // если изображение не яркое - максимально добавить контрастности
                            imageArray = ManyLights_Led_Matrix.transform_video(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/test" + (count-1) + ".mp4", submat_radius); //Разбивка видео по фреймам
                        }
                        if (Mode == 2) { //Лазер
                            imageArray = OneLight.transform_video(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/test" + (count-1) + ".mp4", submat_radius); //Разбивка видео по фреймам
                        }
                        if (Mode == 3) { //Бегущая строка
                            imageArray = null;
                        }
                        System.out.println("End transform_video");
                        if ((Mode == 0) || (Mode == 1) || (Mode == 2)) {
                        Log.i(LOG_TAG, String.valueOf(imageArray.get(10).size()));
                        //Mat imageTest = test_get_thresh(imageArray.get(50));
                        Log.i(LOG_TAG, String.valueOf(imageArray.size()));
                        MatToJpg.SaveToFile(imageArray.get(10), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/test" + (count - 1) + ".bmp"); //Сохраняем 10 кадр для отладки
                        }
                        if (Mode == 0) { //Проверка выбранного режима
                            try {
                                //textView2.setText("Идет распознавание (20 Leds)");
                                if (Core.mean(imageArray.get(0)).val[2]<70) {alpha = 3;} //короче это очень спорно - надо придумать способ калибровки яркости

                                Scalar s1_test = s1;
                                if (automatic_hsv_levels==true)
                                {
                                    s1_test = find_scalars_2(imageArray, radius_for_bright_points_search, eq_bright_level, alpha, beta, s1);
                                }
                                System.out.println("Уточненная подрезка кадров...");


                                ArrayList<Mat> absdiff_video = get_absdiff_video(imageArray, s1_test, s2);

                                ArrayList<Point> temp = new ArrayList<>();
                                ArrayList<Point> summ = new ArrayList<>();
                                Point buf = new Point();

                                try
                                {
                                    ArrayList<Point> temp_test = new ArrayList<>();
                                    for (int i = 0; i < (int) absdiff_video.size(); i++)
                                    {
                                        temp_test = extend_point_list(get_brightest_spot_from_image(absdiff_video.get(i), radius_for_bright_points_search, eq_bright_level, alpha, beta), temp_test);
                                    }
                                    Mat test_test = new Mat();

                                    ArrayList<Integer> border_test = new ArrayList<>();
                                    border_test = get_border_blinks(temp_test);

                                    test_test = imageArray.get(0);
                                    test_test = test_test.submat(border_test.get(0), border_test.get(1), border_test.get(2), border_test.get(3));

                                }
                                catch (Exception e)
                                {
                                    System.out.println("Попал в исключение");
                                    beta = 20; // Brightness control (0-100)
                                    System.out.println("Пробую понизить яркость (beta) до уровня: "+beta);
                                }

                                for (int i = 0; i < (int) absdiff_video.size(); i++)
                                {
                                    temp = extend_point_list(get_brightest_spot_from_image(absdiff_video.get(i), radius_for_bright_points_search, eq_bright_level, alpha, beta), temp);
                                }
//            Point center = get_average_center(temp);
                                Mat test = new Mat();

                                ArrayList<Integer> border = new ArrayList<>();
                                border = get_border_blinks(temp);

                                ArrayList<Mat> new_video = new ArrayList<>();

                                for (int i = 0; i < imageArray.size(); i++)
                                {
                                    test = imageArray.get(i);
                                    test = test.submat(border.get(0), border.get(1), border.get(2), border.get(3));
                                    new_video.add(test.clone());
                                }
                                System.out.println("------------------------------------------");
                                skyUrl = Brain.decode(Brain.mainly(new_video, 0, new_video.size() - 1, s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta)); //Декодирование 20 светодиодов //imageArray.size()-20}
                                Hue_low = (int)s1_test.val[0];
                                Sat_low = (int)s1_test.val[1];
                                Val_low = (int)s1_test.val[2];
                            }
                            catch (Exception e) {
                                ;
                            }
                        }
                        if (Mode == 1) {
                            try {
                                // если изображение не яркое - максимально добавить контрастности
                                if (Core.mean(imageArray.get(0)).val[2]<70)
                                {
                                    alpha = 3;
                                    System.out.println("Повышаю контрастность изображения до уровня: "+alpha);
                                } //короче это очень спорно - надо придумать способ калибровки яркости

                                Scalar s1_test = s1;
                                if (automatic_hsv_levels==true)
                                {
                                    s1_test = ManyLights_Led_Matrix.find_scalars_2(imageArray, radius_for_bright_points_search, eq_bright_level, alpha, beta, s1, s2);
                                }
                                //textView2.setText("Идет распознавание (1 Led)");
                                skyUrl = ManyLights_Led_Matrix.decode(ManyLights_Led_Matrix.mainly(imageArray, 0, imageArray.size() - 1, s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta)); //Декодирование 1 светодиода //imageArray.size()-20}
                            }
                            catch (Exception e) {
                                ;
                            }
                        }
                        if (Mode == 2) {
                            try {
                                //textView2.setText("Идет распознавание (1 Led)");
                                skyUrl = OneLight.decode(OneLight.mainly(imageArray, 0, imageArray.size() - 1, s1, s2, alpha, beta, th_MinVal, th_MaxVal)); //Декодирование 1 светодиода //imageArray.size()-20}
                            }
                            catch (Exception e) {
                                ;
                            }
                        }
                        if (Mode == 3) {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                        /*if (!skyUrl.startsWith("http://") && !skyUrl.startsWith("https://"))
                            skyUrl = "http://" + skyUrl;*/
                        mButtonOpenUrl.setVisibility(View.VISIBLE); //Показываем кнопку для перехода по ссылке
                        mButtonRecordVideo.setClickable(true); //Кнопка для записи следующего видео снова активна
                        //textView1.setText(skyUrl);
                        textView2.setText("Результат:" + "\n" + skyUrl); //Вывод результата в textview
                        if (skyUrl.endsWith(".ru") || skyUrl.endsWith(".com") || skyUrl.endsWith(".net")){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(skyUrl));
                        startActivity(browserIntent);
                        }
                    }
                }.start();
            }
        });
        //public static final CaptureRequest.Key<Integer> LENS_INFO_FOCUS_DISTANCE_CALIBRATION;

        mButtonStopRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((myCameras[CAMERA1] != null) & (mMediaRecorder != null)) {
                    myCameras[CAMERA1].stopRecordingVideo();
                }


            }
        });

        mButtonOpenUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(skyUrl));
                startActivity(browserIntent);
            }
        });


        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Получение списка камер с устройства

            myCameras = new CameraService[mCameraManager.getCameraIdList().length];


            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: " + cameraID);
                int id = Integer.parseInt(cameraID);

                // создаем обработчик для камеры
                myCameras[id] = new CameraService(mCameraManager, cameraID);


            }
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }


        setUpMediaRecorder();

        //запуск камеры сразу после конфигурации медиарекордера
        if (myCameras[CAMERA1] != null) {
            if (!myCameras[CAMERA1].isOpen()) myCameras[CAMERA1].openCamera();
        }

    }

    private void setUpMediaRecorder() {

        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mCurrentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "test"+count+".mp4");
        mMediaRecorder.setOutputFile(mCurrentFile.getAbsolutePath());
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        //mMediaRecorder.setVideoFrameRate(240);
        //mMediaRecorder.setCaptureRate(120);
        //mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);

        try {
            mMediaRecorder.prepare();
            Log.i(LOG_TAG, " запустили медиа рекордер");

        } catch (Exception e) {
            Log.i(LOG_TAG, "не запустили медиа рекордер");
        }


    }

    public class CameraService {


        private String mCameraID;
        private CameraDevice mCameraDevice = null;
        private CameraCaptureSession mSession;
        private CaptureRequest.Builder mPreviewBuilder;


        public CameraService(CameraManager cameraManager, String cameraID) {

            mCameraManager = cameraManager;
            mCameraID = cameraID;

        }


        private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                Log.i(LOG_TAG, "Open camera  with id:" + mCameraDevice.getId());

                startCameraPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                mCameraDevice.close();

                Log.i(LOG_TAG, "disconnect camera  with id:" + mCameraDevice.getId());
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                Log.i(LOG_TAG, "error! camera id:" + camera.getId() + " error:" + error);
            }
        };

        private void startCameraPreviewSession() {

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mImageView.setRotation(270.0f);
            }
            SurfaceTexture texture = mImageView.getSurfaceTexture();
            texture.setDefaultBufferSize(640, 640);
            Surface surface = new Surface(texture);


            try {

                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

                /**Surface for the camera preview set up*/

                mPreviewBuilder.addTarget(surface);

                /**MediaRecorder setup for surface*/

                Surface recorderSurface = mMediaRecorder.getSurface();

                mPreviewBuilder.addTarget(recorderSurface);

                mCameraDevice.createCaptureSession(Arrays.asList(surface, mMediaRecorder.getSurface()),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mSession = session;
                                mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.4f);
                                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                                Range<Integer>[] fps = new Range[1];
                                fps[0] = Range.create(30,30);
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,fps[0]);
                                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,fps[0]);
                                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON); //
                                try {
                                    mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                            }
                        }, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }


        public void stopRecordingVideo() {

            try {
                mSession.stopRepeating();
                mSession.abortCaptures();
                mSession.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            mMediaRecorder.stop();
            mMediaRecorder.release();
            count++;
            /*if (count == 1) {count = 2;}
            else {count = 1;}*/
            setUpMediaRecorder();
            startCameraPreviewSession();
        }


        public boolean isOpen() {
            if (mCameraDevice == null) {
                return false;
            } else {
                return true;
            }
        }


        public void openCamera() {
            try {

                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    mCameraManager.openCamera(mCameraID, mCameraCallback, mBackgroundHandler);

                }


            } catch (CameraAccessException e) {
                Log.i(LOG_TAG, e.getMessage());

            }
        }

    }





    @Override
    public void onPause() {

        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("tag", "Portrait");
            showSystemUI();
        }
        else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("tag", "Landscape");
            hideSystemUI();
        }
        else{
            Log.w("tag", "other: " + orientation);
        }
    }*/


    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }*/

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}

