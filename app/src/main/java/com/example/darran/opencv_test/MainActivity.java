package com.example.darran.opencv_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnCustomCamera, btnStartExp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCustomCamera = findViewById(R.id.btnCustomCamera);
        btnStartExp = findViewById(R.id.btnStartExp);

        btnCustomCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, com.example.darran.opencv_test.CameraActivity.class);
                startActivity(intent);
            }
        });

        btnStartExp.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, com.example.darran.opencv_test.StartExperimentActivity.class);
                startActivity(intent);
            }
        }));

    }

    // Static block to load the OpenCv lib...
    static {
        System.loadLibrary("opencv_java3");
       // System.loadLibrary("jniLibs");
    }
}
