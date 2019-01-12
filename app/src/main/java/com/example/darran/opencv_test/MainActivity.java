package com.example.darran.opencv_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.opencv.core.Core;

public class MainActivity extends AppCompatActivity {


    Button btnCustomCamera;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCustomCamera = findViewById(R.id.btnCustomCamera);


        btnCustomCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, com.example.darran.opencv_test.CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    static {
        System.loadLibrary("opencv_java3");
       // System.loadLibrary("jniLibs");
    }
}
