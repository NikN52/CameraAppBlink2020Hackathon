package com.example.camtest;
import android.os.Environment;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.time.Clock;
import java.util.*;

public class OneLight {
    static ArrayList<Mat> transform_video(String path_to_video, int submat_radius) {
        /*
        Метод возвращает массив картинок из видео

        path_to_video example: "Videos/test2.mp4"
        duration_frames example: 500 frames = FPS * video duration in seconds
        */

        // При необходимости выгрузить картинки, раскомментировать код:
        // Папка с кучей картинок
        //String output = "Images";

        VideoCapture cap = new VideoCapture();
        cap.open(path_to_video);

        int video_length = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        int frames_per_second = (int) cap.get(Videoio.CAP_PROP_FPS);
        int frame_number = (int) cap.get(Videoio.CAP_PROP_POS_FRAMES);

        System.out.println("video_length "+video_length);
        System.out.println("frames_per_second "+frames_per_second);
        System.out.println("frame_number "+frame_number);

        Mat frame = new Mat();

        ArrayList<Mat> video = new ArrayList<Mat>();

        if (cap.isOpened())
        {
            while(cap.read(frame)) //the last frame of the movie will be invalid. check for it !
            {
                // Раскомментировать код при необходимости записи в файл
                // Imgcodecs.imwrite(output + "/" + frame_number +".jpg", frame);
//                video.add(get_rotate(frame.clone(), -90));
                Mat buffer_image = new Mat();
                int center_x = (int) frame.width()/2;
                int center_y = (int) frame.height()/2;

                int left_border = center_x-submat_radius;
                int right_border = center_x+submat_radius;
                int up_border = center_y-submat_radius;
                int down_border = center_y+submat_radius;
                buffer_image = frame.submat(up_border, down_border, left_border, right_border);

                video.add(buffer_image.clone());
//                get_rotate(frame.clone(), -90)
//                HighGui.imshow("Image", buffer_image);
//                HighGui.waitKey(1);
//                frame_number++;
            }
            cap.release();
        }
        else
        {System.out.println("Fail to open video");}
        System.out.println("Video successfully opened");

        return video;
    }

    static ArrayList<Integer> get_brightest_spot_from_image(Mat image) {
        /*
        Метод возвращает координаты самой яркой области на картинке
        */

        ArrayList<Integer> temp = new ArrayList<>();

        Mat img = new Mat();
        img = image.clone();

        double temp_length = 0;

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        Integer alpha = 1; // Contrast control (1.0-3.0)
        Integer beta = 30; // Brightness control (0-100)
        Core.convertScaleAbs(gray.clone(), gray, alpha,  beta);

        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 3, 3);

        int x0 = (int) Core.minMaxLoc(gray).maxLoc.x;
        int y0 = (int) Core.minMaxLoc(gray).maxLoc.y;
        temp.add(x0);
        temp.add(y0);

        int rad = 8;
        Scalar color = new Scalar(0, 123, 123, 0);
        Point center = new Point(x0, y0);
        Imgproc.circle(img, center, rad, color,-1);

        return temp;
    }

    static int get_repeats_funny(int raw_data, int time_kvant)
    {
        /*
        Метод возвращает целое количество вместившихся base фрэймов
        */
        int result = -1;
        if (raw_data == 1 || raw_data == 2|| raw_data == 3|| raw_data == 4)
        {result = 1;}
        else if (raw_data == 5|| raw_data == 6|| raw_data == 7)
        {result = 2;}
        else if (raw_data == 8 || raw_data == 9|| raw_data == 10)
        {result = 3;}
        else if (raw_data == 11|| raw_data == 12|| raw_data == 13)
        {result = 4;}
        else if (raw_data == 14|| raw_data == 15|| raw_data == 16 )
        {result = 5;}

        return result;
    }

    static int find_separator_fast(String str, String separator) {
//        String str = new String("010101010101111001010101");
        int index = 0;

        try
        {
            index = str.indexOf(separator); // searching for # - symbol
            System.out.println("Index of the letter # (separator): "+index);
        }
        catch (Exception e){System.out.println("SEPARATOR SYMBOL NOT FOUND (FAST METHOD)! SUKA BAREBUH NAHUI");}

        return index;
    }

    static String reverseString(String str)
    {
        if (str.length() <= 1) {return str;}
        return reverseString(str.substring(1)) + str.charAt(0);
    }

    static ArrayList<String> build_signal_code(String str, int index)
    {
        String right_side = "";
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> result_left = new ArrayList<>();

//        120 бит вместе со служебным диодом
//        15 байт
        if (str.length() >= 120)
        {
            try
            {
                int counter_bits = 0;
                int counter_bytes = 0;
                for (int i=index+8; i<str.length(); i++)
                {
                    counter_bits += 1;
                    right_side += str.charAt(i);
                    if (counter_bits == 8)
                    {

                        counter_bytes += 1;
                        counter_bits = 0;
                        System.out.println("letter: "+counter_bytes+":"+right_side);
                        result.add(right_side);
                        right_side = "";

                    }
                }

                if (counter_bytes<=14)
                {
                    int missing_bytes_number = 14-counter_bytes;
//                    System.out.println("missing_bytes_number: "+missing_bytes_number);
                    int missing_bits = missing_bytes_number*8;
                    int count = 0;
                    String left_side = "";
                    for (int i=index-1; i>=index-missing_bits; i--)
                    {
                        count += 1;
                        left_side += str.charAt(i);
                        if (count > 7)
                        {
                            count = 0;
                            result_left.add(reverseString(left_side));
                            System.out.println("letter: "+left_side);
                            left_side = "";
                        }
                    }
                }
            }
            catch (Exception e){System.out.println("SEPARATOR SYMBOL NOT FOUND! SUKA BLYAT NAHUI");}
        }
        else
        {System.out.println("NOT ENOUGH DATA BITS! PIZDEZ HUINYA");}

        for (int i=result_left.size()-1; i>=0; i--)
        {result.add(result_left.get(i));}

        return result;
    }

    static String convert_code_to_text(ArrayList<String> signal_codes) {
        /*
        Метод конвертирует 8битные коды в символы
        */
        String result = new String();
        Integer binary;

        for (int i=0; i< signal_codes.size(); i++)
        {
            if (signal_codes.get(i).length() == 8)
            {
                binary = Integer.valueOf(signal_codes.get(i), 2);
//                System.out.println("binary: "+binary);
                char temp = (char) (int) binary;
                result += temp;
            }
        }
        return String.valueOf(result);
    }

    static String decode(String data) {
        /*
        Метод возвращает переведенный текст из данных, собранных с изображений
        */

        String sep = "00100011";
        int index = -1;
        try {index = find_separator_fast(data, sep);}
        catch (Exception e) {System.out.println("Method find_separator_fast - Error");}

        ArrayList<String> binary_list = new ArrayList<>();
        try {binary_list = build_signal_code(data, index);}
        catch (Exception e) {System.out.println("Method build_signal_code - Error");}


        String result = "Decoding error";
        try {result = convert_code_to_text(binary_list);}
        catch (Exception e) {System.out.println("Method convert_code_to_text - Error");}

        System.out.println("Ответ: "+result);
        return result;
    }

    static Map<String,Integer> prepare_sector(int x0, int y0, int shift_x, int shift_y, int width, int height) {
        int deltaX = (int) (shift_x);

        int deltaY = (int) (shift_y);
        int w = (int) (width);
        int h = (int) (height);

//        System.out.println("y0:"+y0+ "  deltaY:"+deltaY+ " shift_y: "+shift_y);

        Map<String,Integer> sector = new HashMap<>();
        sector.put("x", x0+deltaX);
        sector.put("w", w);
        sector.put("y", y0+deltaY);
        sector.put("h", h);

//        System.out.println("sector: "+sector);

        return sector;
    }

    static Mat get_image_independent_sector(Mat input_image, Map<String,Integer> sector) {
        /*
        Метод возвращает сектор (словарь) картинки
        Пример как сделать словарь: Map<String,Integer> main_sector = new HashMap<String,Integer>();
        */

        Mat output_image = new Mat();
        Integer y0 = sector.get("y");
        Integer y1 = y0+sector.get("h");
        Integer x0 = sector.get("x");
        Integer x1 = x0+sector.get("w");

//        System.out.println("get_image_indep"+y0+" "+ y1 +" "+ x0 +" "+ x1);

        Mat printimg = new Mat();
        printimg = input_image.clone();
        Imgproc.rectangle(printimg, new Point(x0, y0), new Point(x1, y1),new Scalar(0, 0, 0), -1);
//        HighGui.imshow("Image123", printimg);
//        HighGui.waitKey()                                           ;


//        System.out.println("input_image:"+input_image.size()+" x0: "+x0+" y0: "+y0+" x1: "+x1+" y1: "+y1);
        if (y1 < input_image.size().height && x1 < input_image.size().width)
        {
            output_image = input_image.submat(y0, y1, x0, x1);
        }


        return output_image;
    }

    static ArrayList<ArrayList<Integer>> get_color_map(Mat input_image1) {
        /*
        Метод возвращает карту цветов картинки, привязанную к координатам пикселей
        */
        ArrayList<ArrayList<Integer>> color_map = new ArrayList<>();
        Size image_sz = input_image1.size();
        byte buff[] = new byte[(int) (input_image1.total() * input_image1.channels())];

        int a_size = (int) image_sz.width;
        int b_size = (int) image_sz.height;

        double picdata[][] =  new double[a_size][b_size] ;
        double temp[];

//        List<Double> temp = new ArrayList<>();
        ArrayList<Integer> num = new ArrayList<>();

        for (int a=0; a<a_size; a++)
        {
            for (int b=0; b<b_size; b++)
            {
                temp = input_image1.get(b, a);
                picdata[a][b] = (int) temp[0];

                num.clear();
                num.add(0,(int) picdata[a][b]);
                num.add(1, b);
                num.add(2, a);
                color_map.add(new ArrayList<Integer>(num));
//                System.out.println("max_Y:   "+color_map.get(a+b));
            }
        }

        return color_map;

    }

    static Integer get_white_value(ArrayList<ArrayList<Integer>> color_map) {
        /*
        Метод возвращает кол-во белых пикселей в карте цветов
        */
        Integer white_value=0;

        for (int i=0; i<color_map.size(); i++)
        {
            Integer color = color_map.get(i).get(0);
            if (color == 255)
            {
                white_value ++;
            }
        }
        return white_value;
    }
    //
    static Integer get_status(Mat input_image) {
        /*
        Метод возвращает статус включения светодиодов
        */

        ArrayList<ArrayList<Integer>> color_map = new ArrayList<>(get_color_map(input_image));
        Integer white_value = get_white_value(color_map);
        if (white_value > 10) {return (int) 1;} else {return (int) 0;}
    }

    static Mat get_hsv_mask(Mat input_image, Scalar s1, Scalar s2) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(input_image, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv, s1, s2, hsv);
        input_image.copyTo(hsv, hsv);
        //hue range is [0,179], saturation range is [0,255], and value range is [0,255].
        return hsv;
    }


    static Integer get_image_status(Mat input_image, Scalar s1, Scalar s2, Integer alpha, Integer beta, Integer th_MinVal, Integer th_MaxVal) {
        /*
        Метод возвращает результат анализа изображения
        */

        Integer status = 9;
        Integer flag = 0;
        Mat printimg = new Mat();
        Mat big = new Mat();
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat black_and_white = new Mat();
        Mat thresh = new Mat();

        int center_x = (int) input_image.width()/2;
        int center_y = (int) input_image.height()/2;

        Imgproc.GaussianBlur(input_image, blurred, new Size(11, 11), 0);

        input_image = get_hsv_mask(blurred, s1,s2);

        ArrayList<Integer> borders = new ArrayList<>(get_brightest_spot_from_image(input_image));
        int xL = borders.get(0);
        int yL = borders.get(1);

        int check_radius = 50;
        boolean correct_position_condition = xL < center_x+check_radius && xL > center_x-check_radius && yL < center_y+check_radius && yL > center_y-check_radius;
//        System.out.println("center: x:"+center_x+" y:"+center_y);
//        System.out.println("border: x:"+xL+" y:"+yL);
        if(correct_position_condition)
        {
            Imgproc.cvtColor(input_image, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(gray, blurred, new Size(11, 11), 0);

            Core.convertScaleAbs(blurred, black_and_white, alpha,  beta);
            Imgproc.threshold(black_and_white, thresh, th_MinVal, th_MaxVal, Imgproc.THRESH_BINARY);

            Mat led = new Mat();
            Map<String,Integer> sector = new HashMap<String,Integer>();
            sector = prepare_sector(xL, yL, -10, -10, 20, 20);
            led = get_image_independent_sector(thresh, sector);

//            printimg = input_image.clone();
//            Imgproc.rectangle(printimg, new Point(xL-10, yL-10), new Point(xL+10, yL+10),new Scalar(255, 255, 255, 255), 1);
//
//            HighGui.imshow("printimg", printimg);
//            HighGui.waitKey(1000);

            status = get_status(led);
        }
        else
        {
//                printimg = input_image.clone();
//                Imgproc.rectangle(printimg, new Point(center_x-10, center_y-10), new Point(center_x+10, center_y+10),new Scalar(255, 255, 255, 255), 1);
//
//                HighGui.imshow("printimg", printimg);
//                HighGui.waitKey(1000);
            status = 0;
        }
        return status;
    }

    static String struct_status(String status)
    {
        /*
        Метод возвращает Структурированный статус
        */

        String buffer_status = "";
        String result_status = "";
        ArrayList<Integer> periods = new ArrayList<>();
        ArrayList<Integer> struct_periods = new ArrayList<>();
        int time_kvant = 2;
        char tmp;
        int counter = 0;
        for (int i=0; i<status.length()-1; i++)
        {
            counter +=1;
            tmp = status.charAt(i);
            if (tmp != status.charAt(i+1))
            {
                periods.add(counter);
                buffer_status += tmp;
                counter = 0;
            }
        }

        for (int j=0; j<periods.size(); j++)
        {struct_periods.add(get_repeats_funny(periods.get(j), time_kvant));}

        for (int i=0; i<buffer_status.length();i++)
        {for (int j=0; j<struct_periods.get(i); j++){result_status += buffer_status.charAt(i);}}

        System.out.println("----------------------------------------------");
        System.out.println("Raw data from original video: " + status);
        System.out.println("Raw periods: "+periods);
        System.out.println("----------------------------------------------");
        System.out.println("Structed data with Algorithm: " + result_status);
        System.out.println("Structed periods with Algorithm: " + struct_periods);
        System.out.println("----------------------------------------------");

        return result_status;
    }

    static String mainly(ArrayList<Mat> video, int start, int finish, Scalar s1, Scalar s2, Integer alpha, Integer beta, Integer th_MinVal, Integer th_MaxVal)
    {
        /*
        Метод возвращает анализ видео
        */
        String row_status = "";
        for (int i=start; i<finish;i++)
        {
            try {row_status += get_image_status(video.get(i), s1, s2, alpha, beta, th_MinVal, th_MaxVal);}
            catch (Exception e) {System.out.println("Method get_image_status - Error");}
//            System.out.println("текущий статус: "+row_status);
        }

        String output = "";
        try {output = struct_status(row_status);}
        catch (Exception e) {System.out.println("Method struct_status - Error");}

        return output;
    }
}
