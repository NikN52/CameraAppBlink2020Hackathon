package com.example.camtest;

import org.opencv.core.*;

import java.util.ArrayList;

public interface myRunnable extends Runnable {

    public void setParams(ArrayList<Mat> video, int param1, int param2, Scalar s1, Scalar s2, int rad, int eq_bright_level, int th_MinVal, int th_MaxVal, int alpha, int beta);
}
