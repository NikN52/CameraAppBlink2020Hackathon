package com.example.camtest;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.ArrayList;


public class ImplementMyRunnable_transform_video implements myRunnable_transform_video{
    VideoCapture input_cap;
    int param1;
    int param2;
    private volatile ArrayList<Mat> video = new ArrayList<Mat>();
    public void setParams(VideoCapture input_cap, int param1, int param2){
        this.input_cap = input_cap;
        this.param1 = param1;
        this.param2 = param2;
    }

    @Override
    public void run() {
        /*
        Метод возвращает массив картинок из видео

        path_to_video example: "Videos/test2.mp4"
        duration_frames example: 500 frames = FPS * video duration in seconds
        */

        // При необходимости выгрузить картинки, раскомментировать код:
        // Папка с кучей картинок
        //String output = "Images";


        Mat frame = new Mat();
        int frame_number = (int) input_cap.get(Videoio.CAP_PROP_POS_FRAMES);
        //ArrayList<Mat> video = new ArrayList<Mat>();
        input_cap.set(Videoio.CAP_PROP_POS_FRAMES, param1);

        if (input_cap.isOpened())
        {
//            System.out.println("Video is opened");
//            System.out.println("Number of Frames: " + video_length);
//            System.out.println(frames_per_second + " Frames per Second");
//            System.out.println("Converting Video...");

            for (int i = param1; i<= param2; i++)
            {


                input_cap.read(frame);
                video.add(frame.clone());
                frame_number++;
            }
            input_cap.release();
            //System.out.println(video);

        }

        else
        {
            System.out.println("Fail");
        }
    }

    public ArrayList<Mat> getValue() {
        //System.out.println(video);
        return video;
    }
}
