package com.example.darran.opencv_test;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Darran on 11/01/2019.
 */

public class Utilities {

    private Utilities(){

    }

    public static String generateFilename(){
        @SuppressLint("SimpleDateFormat")SimpleDateFormat sdf
                = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return "CVcam" + sdf.format(new Date()) + ".jpg";
    }
}
