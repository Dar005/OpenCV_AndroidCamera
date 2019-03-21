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
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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

                int touchX = (int) motionEvent.getX();
                int touchY = (int) motionEvent.getY();

                int imageX = touchX - viewCoords[0];
                int imageY = touchY - viewCoords[1];

                Toast.makeText(ViewImage.this, "R(" + r + ")\n" + "G(" + g + ")\n" + "B("+ b + ")", Toast.LENGTH_SHORT).show();

                //Toast.makeText(ViewImage.this, "X-Coord = " +imageX + ", Y-Coord = "+imageY, Toast.LENGTH_LONG).show();

//                int colorAtLocation = bitmap.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());
//                //int colorAtLocation = bitmap.getPixel(imageX, imageY);
//
//                int r = Color.red(colorAtLocation);
//                int g = Color.green(colorAtLocation);
//                int b = Color.blue(colorAtLocation);


                Log.v("Color at Touch location", r + g + b +"");

                //int real_x = (imageX *)

                Log.v("X-COORD", imageX+"");
                Log.v("Y-COORD", imageY+"");

                //Toast.makeText(ViewImage.this, "X-Coord = " +imageX + ", Y-Coord = "+imageY, Toast.LENGTH_LONG).show();

               // Toast.makeText(ViewImage.this, "R(" + r + ")\n" + "G(" + g + ")\n" + "B("+ b + ")", Toast.LENGTH_SHORT).show();

                int upperR = r + 25;
                int lowerR = r - 25;
                int upperG = g + 25;
                int lowerG = g - 25;
                int upperB = b + 25;
                int lowerB = b - 25;

                int upperWhite = 255;
                int lowerWhite = 190;

                Toast.makeText(ViewImage.this, "R(" + upperR +", "+ lowerR + ")\n" + "G(" + upperG + ", " + lowerG + ")\n" + "B("+ upperB + ", "+ lowerB + ")", Toast.LENGTH_SHORT).show();


                int [] pixelColor = new int[2];
                int colorCount = 0;
               // int pixelColor;
                String color = "";
                int count = 0;
                int total = bitmap.getHeight() * bitmap.getWidth();

                HashMap<String, Integer> colorList = new HashMap<String, Integer>();

                for (int y = 0; y < bitmap.getHeight(); y++){

                    for (int x = 0; x < bitmap.getWidth(); x++){

                        //pixel = bitmap.getPixel(x, y);

                        int R = Color.red(pixelColor[count]);
                        int G = Color.green(pixelColor[count]);
                        int B = Color.blue(pixelColor[count]);

                        pixelColor[count] = bitmap.getPixel(x, y);
                        // White
                        if (lowerWhite < Color.red(pixelColor[count]) && upperWhite >= Color.red(pixelColor[count])
                                && lowerWhite < Color.green(pixelColor[count]) && upperWhite >= Color.green(pixelColor[count])
                                && lowerWhite < Color.blue(pixelColor[count]) && upperWhite >= Color.blue(pixelColor[count])) {
                            // add white ++ count
                            color = "white";

                        }
                        else if(lowerR < Color.red(pixelColor[count]) && upperR > Color.red(pixelColor[count])
                                && lowerG < Color.green(pixelColor[count]) && upperG > Color.green(pixelColor[count])
                                && lowerB < Color.blue(pixelColor[count]) && upperB > Color.blue(pixelColor[count])){
                            // within selected colour range..
                            color = "good";

                        }
                        // other colours
                        else {
                            color = "unknown";
                            //total++;
                        }
                        //count++;

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

                Integer whiteCount = colorList.get("white");
                Integer goodCount = colorList.get("good");
                Integer unknownCount = colorList.get("unknown");

                Toast.makeText(ViewImage.this, "White = " + whiteCount + "\nGood = "
                        + goodCount + "\nUnknown = " + unknownCount , Toast.LENGTH_LONG).show();

                Integer leaf = total - whiteCount;
                Integer diseasePer = (goodCount * 100) / leaf;


//                int bad = unknownCount/leaf;
//                int per = bad * 100;
               // int ans = 100 - diseasePer;

                Toast.makeText(ViewImage.this, "Disease percentage = " + diseasePer + "%", Toast.LENGTH_LONG).show();

                    // Toast.makeText(ViewImage.this, "R(" + r + ")\n" + "G(" + g + ")\n" + "B("+ b + ")", Toast.LENGTH_SHORT).show();
               // int [] pixelColor = new int[2];
                //int pixelColor;
                //int count = 0;
               // int colorCount = 0;
               // String color = "";
               // int total = 0;

//                Mat pic = new Mat();
//                Mat hsvPic = new Mat();
//
//                Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//                Utils.bitmapToMat(bmp, pic);
//
//               // pic = Imgcodecs.imread(name);
//
//                Imgproc.cvtColor(pic, hsvPic, Imgproc.COLOR_RGB2HSV);
//
//                int height = hsvPic.rows();
//                int width = hsvPic.cols();
//                int total = height * width;
//
//                HashMap<String, Integer> colorList = new HashMap<String, Integer>();
//
//                String color = "";
//                int colorCount = 0;
//                double blue = 0.0;
//                String test = "";
//
//                for (int i = 0; i < height; i++) {
//                    for (int k = 0; k < width; k++) {
//
//                        /**
//                         * HSV works usually works on degrees, percent, percent a H = 0 - 360,
//                         * S = 0 - 100%, V = 0 - 100%
//                         * In OpenCV however HSV uses degrees, value, value. Value is between 0 and 255
//                         * like RGB values
//                         * Furthermore OpenCv does not store Hue(H) in range of 0 - 360, instead it uses
//                         * 0 - 180 (something to do with storing it in 8 bits.....)
//                         *
//                         */
//
//                        double[] hsv = hsvPic.get(i, k);
//
//                        double h = hsv[0];
//                        double s = hsv[1];
//                        double v = hsv[2];
//
//
//                       // test = h + "," + s + "," + v;
//
//                        // check for white and ignore if found
//                        if (h == 0.0 && s == 0.0 && v == 255.0) {
//                            color = "white";
//                        }
//
//                        // red
//                        else if(h >= 0.0 && h <= 20.0){
//                            color = "red";
//                        }
//
//                        // yellow
//                        else if(h > 20.0 && h <= 40.0){
//                            color = "yellow";
//                        }
//
//
//                        // green
//                        else if(h > 40.0 && h <= 80.0){
//                            color = "green";
//                        }
//
//                        // blue
//                        else if(h > 80.0 && h <= 180.0){
//
//                            color = "blue";
//                            //System.out.println("color" + h + ", " + s + ", " + v);
//                            blue = h;
//                        }
//                        else {
//                            color = "Unknown color";
//                            //System.out.println("color" + h + ", " + s + ", " + v);
//                        }
//
//
//
//                        /**
//                         *  Check if color is already in hash map and increase count if it
//                         *  is or add new key value with count equal to 1
//                         */
//                        if (colorList.containsKey(color)) {
//                            colorCount = colorList.get(color);
//                            colorCount++;
//                            colorList.put(color, colorCount);
//                        }else {
//
//                            colorCount = 1;
//                            colorList.put(color, colorCount);
//                        }
//
//                    }
//                }

//                Toast.makeText(ViewImage.this, "Analysing.....", Toast.LENGTH_SHORT).show();
//                for (int y = 0; y < bitmap.getHeight(); y++){
//
//                    for (int x = 0; x < bitmap.getWidth(); x++){
//
//                        pixelColor = bitmap.getPixel(x, y);
//                        // White
//                        if (Color.red(pixelColor) > 217 && Color.green(pixelColor) > 217 && Color.blue(pixelColor) > 217){
//                            // add white ++ count
//                            color = "white";
//                            total++;
//                        }
//                        // other colours
//                        else {
//                            color = "unknown";
//                            total++;
//                        }
//                        //count++;
//
//                        if (colorList.containsKey(color)){
//                            colorCount = colorList.get(color);
//                            colorCount++;
//                            colorList.put(color, colorCount);
//
//                        }else{
//                            colorCount = 1;
//                            colorList.put(color, colorCount);
//                        }
//                    }
//                }


                //int white = colorList.get("white");
                //int other = colorList.get("unknown");
               // Toast.makeText(ViewImage.this, "Total : " + total , Toast.LENGTH_SHORT).show();
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
