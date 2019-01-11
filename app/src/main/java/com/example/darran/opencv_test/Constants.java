package com.example.darran.opencv_test;

import android.os.Environment;

import java.io.File;

/**
 * Created by Darran on 11/01/2019.
 */

public class Constants {

    private Constants(){

    }

    public static final String SCAN_IMAGE_LOCATION = Environment.getExternalStorageDirectory() +
            File.separator + "OpenCV ANDROID CAMERA";

}
