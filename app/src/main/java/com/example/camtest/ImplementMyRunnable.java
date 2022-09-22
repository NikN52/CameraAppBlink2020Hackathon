package com.example.camtest;

import org.opencv.core.*;
import java.util.ArrayList;

import static com.example.camtest.Brain.get_image_status;
import static com.example.camtest.Brain.update_df;

public class ImplementMyRunnable implements myRunnable{
    ArrayList<Mat> video;
    int param1;
    int param2;
    Scalar s1;
    Scalar s2;
    int rad;
    int eq_bright_level;
    int th_MinVal;
    int th_MaxVal;
    int alpha;
    int beta;
    private volatile ArrayList<ArrayList<Double>> df = new ArrayList<>();
    public void setParams(ArrayList<Mat> video, int param1, int param2, Scalar s1, Scalar s2, int rad, int eq_bright_level, int th_MinVal, int th_MaxVal, int alpha, int beta){
        this.video = video;
        this.param1 = param1;
        this.param2 = param2;
        this.s1 = s1;
        this.s2 = s2;
        this.rad = rad;
        this.eq_bright_level = eq_bright_level;
        this.th_MinVal = th_MinVal;
        this.th_MaxVal = th_MaxVal;
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public void run() {
                /*
        Метод возвращает анализ видео
        */
        double counter = 0.0;

        //ArrayList<ArrayList<Double>> df = new ArrayList<>();



        for (int i=param1; i<param2;i++)
        {
            counter += 0.01;
            ArrayList<Integer> status = new ArrayList<Integer>();
            status = get_image_status(video.get(i), s1, s2, rad, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta);
            df = update_df(df, status, counter);
        }
        //System.out.println(df);
    }

    public ArrayList<ArrayList<Double>> getValue() {
        return df;
    }
}
