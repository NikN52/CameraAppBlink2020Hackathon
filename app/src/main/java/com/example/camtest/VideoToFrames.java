package com.example.camtest;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.ArrayList;

public class VideoToFrames {
    static ArrayList<Mat> transform_video(String path_to_video) {

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
        /*VideoCapture cap2 = cap;
        VideoCapture cap3 = cap;
        VideoCapture cap4 = cap;
        VideoCapture cap5 = cap;*/

        /*cap2.open(path_to_video);
        cap3.open(path_to_video);
        cap4.open(path_to_video);
        cap5.open(path_to_video);*/

        //int frames = get_video_frames(cap);
        int video_length = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        int frames_per_second = (int) cap.get(Videoio.CAP_PROP_FPS);
        int frame_number = (int) cap.get(Videoio.CAP_PROP_POS_FRAMES);
        Mat frame = new Mat();
        ArrayList<Mat> video = new ArrayList<Mat>();
        /*video_length = 143;
        ArrayList<ArrayList<Integer>> start_stop = new ArrayList<>(get_start_stop(video_length, 5));

        System.out.println(start_stop);

        ImplementMyRunnable_transform_video object_tv_1 = new ImplementMyRunnable_transform_video();
        object_tv_1.setParams(cap,start_stop.get(0).get(0), start_stop.get(0).get(1));
        Thread myThread_tv_1 = new Thread(object_tv_1);

        ImplementMyRunnable_transform_video object_tv_2 = new ImplementMyRunnable_transform_video();
        object_tv_2.setParams(cap2,start_stop.get(1).get(0), start_stop.get(1).get(1));
        Thread myThread_tv_2 = new Thread(object_tv_2);

        ImplementMyRunnable_transform_video object_tv_3 = new ImplementMyRunnable_transform_video();
        object_tv_3.setParams(cap3,start_stop.get(2).get(0), start_stop.get(2).get(1));
        Thread myThread_tv_3 = new Thread(object_tv_3);

        ImplementMyRunnable_transform_video object_tv_4 = new ImplementMyRunnable_transform_video();
        object_tv_4.setParams(cap4,start_stop.get(3).get(0), start_stop.get(3).get(1));
        Thread myThread_tv_4 = new Thread(object_tv_4);

        ImplementMyRunnable_transform_video object_tv_5 = new ImplementMyRunnable_transform_video();
        object_tv_5.setParams(cap5,start_stop.get(4).get(0), start_stop.get(4).get(1));
        Thread myThread_tv_5 = new Thread(object_tv_5);

        myThread_tv_1.start();
        myThread_tv_2.start();
        myThread_tv_3.start();
        myThread_tv_4.start();
        myThread_tv_5.start();

        try {
            myThread_tv_1.join();
            myThread_tv_2.join();
            myThread_tv_3.join();
            myThread_tv_4.join();
            myThread_tv_5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(e);
            System.out.println("error threads");
        }


        ArrayList<ArrayList<Mat>> video_list = new ArrayList<>();
        //System.out.println(object_tv_1.getValue());
        video_list.add(object_tv_1.getValue());
        //System.out.println(video_list);
        video_list.add(object_tv_2.getValue());
        //System.out.println(video_list);
        video_list.add(object_tv_3.getValue());
        //System.out.println(video_list);
        video_list.add(object_tv_4.getValue());
        //System.out.println(video_list);
        video_list.add(object_tv_5.getValue());
        //System.out.println(video_list);

        ArrayList<Mat> video = new ArrayList<>(unite_video(video_list));*/
        if (cap.isOpened())
        {
//            System.out.println("Video is opened");
//            System.out.println("Number of Frames: " + video_length);
//            System.out.println(frames_per_second + " Frames per Second");
//            System.out.println("Converting Video...");

            while(cap.read(frame)) //the last frame of the movie will be invalid. check for it !
            {
                // Раскомментировать код при необходимости записи в файл
                // Imgcodecs.imwrite(output + "/" + frame_number +".jpg", frame);
//                video.add(get_rotate(frame.clone(), -90));
                video.add(frame.clone());
//                get_rotate(frame.clone(), -90)
//                HighGui.imshow("Image", frame.clone());
//                HighGui.waitKey(1);
                frame_number++;
            }
            cap.release();
        }

        else
        {
            System.out.println("Fail to open video");
        }
        System.out.println("Video successfully opened");
        return video;
    }
}
