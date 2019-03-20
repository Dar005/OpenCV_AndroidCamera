package com.example.darran.opencv_test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ViewImage extends AppCompatActivity {

    ImageView imgView;

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    private Bitmap bitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        imgView = findViewById(R.id.imgView);

        imgView.setDrawingCacheEnabled(true);
        imgView.buildDrawingCache(true);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("file");

        Bitmap b = BitmapFactory.decodeFile(name);

        imgView.setImageBitmap(b);


        imgView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int[] viewCoords = new int[2];
                imgView.getLocationOnScreen(viewCoords);

                bitmap = imgView.getDrawingCache();
                int pixel = bitmap.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                HashMap<String, Integer> colorList = new HashMap<String, Integer>();


               // Toast.makeText(ViewImage.this, "R(" + r + ")\n" + "G(" + g + ")\n" + "B("+ b + ")", Toast.LENGTH_SHORT).show();
                int [] pixelColor = new int[2];
                int count = 0;
                int colorCount = 0;
                String color = "";
                for (int y = 0; y < bitmap.getHeight(); y++){

                    for (int x = 0; x < bitmap.getWidth(); x++){

                        pixelColor[count] = bitmap.getPixel(x, y);
                        // White
                        if (Color.red(pixelColor[count]) > 229 && Color.green(pixelColor[count]) > 229 && Color.blue(pixelColor[count]) > 229){
                            // add white ++ count
                            color = "white";
                        }
                        // other colours
                        else {
                            color = "unknown";
                        }
                        count++;

                        if (colorList.containsKey(color)){
                            colorCount = colorList.get(color);
                            colorCount++;
                            colorList.put(color, colorCount);

                        }else{
                            colorCount = 1;
                            colorList.put(color, colorCount);
                        }
                    }
                }


                int white = colorList.get("white");
                int other = colorList.get("unknown");
                Toast.makeText(ViewImage.this, "White : " + white + "\n Other : " + other , Toast.LENGTH_SHORT).show();
//
//                int touchX = (int) motionEvent.getX();
//                int touchY = (int) motionEvent.getY();
//
//                int imageX = touchX - viewCoords[0];
//                int imageY = touchY - viewCoords[1];



                //int real_x = (imageX *)
//
//                Log.v("X-COORD", imageX+"");
//                Log.v("Y-COORD", imageY+"");
//
//                Toast.makeText(ViewImage.this, "X-Coord = " +imageX + ", Y-Coord = "+imageY, Toast.LENGTH_SHORT).show();
//
//                Intent result = new Intent(ViewImage.this, CameraActivity.class);
//                result.putExtra("filename", name);
//                result.putExtra("xCoord", imageX);
//                result.putExtra("yCoord", imageY);


                /**
                 * Try process image here....
                 *
                 */






               // finish();

                return true;
            }
        });

//        finish();

    }


    // Async task for sending data i.e. image date and time....
//    public class UploadData extends AsyncTask<String, Void, JSONObject> {
//
//
//
//        @Override
//        protected JSONObject doInBackground(String... args){
//
//            try{
//                URL url = new URL("http://www.c0009839.candept.com/API/imageUpload.php");
//
//
//                // DATE TIME IMAGE
//                // put params in a JSON Object
//                JSONObject dataParams = new JSONObject();
//                dataParams.put("date", args[0]);
//                dataParams.put("time", args[1]);
//                dataParams.put("image", args[2]);
//                dataParams.put("lat", args[3]);
//                dataParams.put("lon", args[4]);
//                dataParams.put("weather", args[5]);
//
//                // Set up connection
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(15000);
//                conn.setConnectTimeout(15000);
//                conn.setRequestMethod("POST");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
//                //send date
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
//                writer.write(getPostDateString(dataParams));
//
//                writer.flush();
//                writer.close();
//                os.close();
//
//                // Get Response
//                int responseCode = conn.getResponseCode();
//                error = String.valueOf(conn.getResponseCode());
//
//                if (responseCode == HttpURLConnection.HTTP_OK){
//                    inputStream = conn.getInputStream();
//                    BufferedReader in  = new BufferedReader(new InputStreamReader(inputStream));
//                    StringBuilder sb = new StringBuilder();
//                    String line;
//
//                    while(null!= (line = in.readLine())){
//                        sb.append(line).append("\n");
//                    }
//                    in.close();
//                    inputStream.close();
//                    json = sb.toString();
//                    Log.i("API Camera: ", json);
//                }
//                else{
//                    Log.e("Buffer Error", "Error Getting Result " +responseCode);
//                }
//                try{
//                    jObj = new JSONObject(json);
//                    jObj.put("error_code", error);
//                }catch(JSONException e){
//                    Log.e("JSON Parser", "Error Parsing Data " + e.toString());
//                }
//            }catch(Exception e){
//                Log.e("Exception: ", "Overall Try Block " + e.toString());
//            }
//            return jObj;
//        }// end of doInBackground
//
//        @Override
//        protected void onPostExecute(JSONObject result){
//
//            try {
//
//                if (result != null){
//
//                    String uploadSuccess = result.getString("message");
//                    if (uploadSuccess.equals("Successfully uploaded data")){
//                        Toast.makeText(getApplicationContext(), result.getString(
//                                "message"), Toast.LENGTH_LONG).show();
//                    }else{
//                        Toast.makeText(getApplicationContext(), result.getString(
//                                "message"), Toast.LENGTH_LONG).show();
//                    }
//                }else{
//                    Toast.makeText(getApplicationContext(),
//                            "Unable to retrieve data from the server", Toast.LENGTH_LONG).show();
//                }
//            }catch(JSONException e){
//                e.printStackTrace();
//            }
//        }
//    }
}
