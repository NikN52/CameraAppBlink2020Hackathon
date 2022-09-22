package com.example.camtest;


import org.opencv.videoio.VideoCapture;

public interface myRunnable_transform_video extends Runnable {

    public void setParams(VideoCapture input_cap, int param1, int param2);
}
