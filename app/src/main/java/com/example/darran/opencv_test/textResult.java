package com.example.darran.opencv_test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class textResult extends AppCompatActivity {

    ImageView ivText;
    TextView tvText;
    Button btnShowText;


    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    private Bitmap bitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_result);

        ivText = findViewById(R.id.ivText);
        tvText = findViewById(R.id.tvText);
        btnShowText = findViewById(R.id.btnShowText);


       // ivText.setDrawingCacheEnabled(true);
        //ivText.buildDrawingCache(true);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("file");

        Bitmap bmp = BitmapFactory.decodeFile(name);

        final Bitmap b = BitmapFactory.decodeFile(name);

        // add image to fire base here

        ivText.setImageBitmap(bmp);


        btnShowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // code to call text recogoniser
                runTextRecognition(b);
            }
        });





//        finish();

    }


    private void runTextRecognition(Bitmap b){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(b);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        btnShowText.setEnabled(false);
        recognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText texts) {
                        btnShowText.setEnabled(true);
                        processTextRecognitionResult(texts);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                btnShowText.setEnabled(true);
                                e.printStackTrace();
                            }
                        }
                );
    }

    private void processTextRecognitionResult(FirebaseVisionText texts){
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if(blocks.size() == 0){
            Toast.makeText(textResult.this, "No text found", Toast.LENGTH_LONG).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        Boolean first = true;
        String s = "";

        for (int i = 0; i < blocks.size(); i++){
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++){
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                if (first){
                    first = false;
                }else {
                    sb.append(", ");
                }

                for (int k = 0; k< elements.size(); k++){
                    //  Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    // mGraphicOverlay.add(textGraphic);

                    sb.append(elements.get(k).getText()+ " ");
                }
            }
        }

        tvText.setText(sb);


    }



}
