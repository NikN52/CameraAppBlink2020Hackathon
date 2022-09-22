package com.example.camtest;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class MatToJpg {
    public static void SaveToFile(Mat mat, String filename) {
        Mat tmp = new Mat();

        Imgproc.cvtColor(mat, tmp, Imgproc.COLOR_BGRA2BGR);

        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmp);

        //return(bmp);

        try (FileOutputStream out = new FileOutputStream(filename)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
