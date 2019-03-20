package com.example.darran.opencv_test;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private static final String APP_ID = "b11dc521fd3aecc6374e2e331dc090e3";
    Button btnCustomCamera, btnStartExp, btnGPS, btnWeather;
    TextView tvGPS, tvTemp, tvDesc;

    String lon = "";
    String lat = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String city = "Dublin, IE";

        // Start my Location Service and get the Longitude and Latitude
        startService(new Intent(MainActivity.this, com.example.darran.opencv_test.GPSTracker.class));
        final GPSTracker gps = new GPSTracker(MainActivity.this);
        lat = Double.toString(gps.getLatitude());
        lon = Double.toString(gps.getLongitude());

        btnCustomCamera = findViewById(R.id.btnCustomCamera);
        btnStartExp = findViewById(R.id.btnStartExp);
        btnGPS = findViewById(R.id.btnGPS);
        btnWeather = findViewById(R.id.btnWeather);

        tvGPS = findViewById(R.id.tvGPS);
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvDesc);


        String units = "metric";
        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;
        new GetWeatherTask(tvTemp).execute(url);


        // Detect Disease Button
        btnCustomCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, com.example.darran.opencv_test.CameraActivity.class);
                startActivity(intent);
            }
        });

        // Start Experiment
        btnStartExp.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, com.example.darran.opencv_test.StartExperimentActivity.class);
                startActivity(intent);
            }
        }));

        // Get Gps data
        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

             //  final GPSTracker gps = new GPSTracker(MainActivity.this);

                startService(new Intent(MainActivity.this, com.example.darran.opencv_test.GPSTracker.class));
                final GPSTracker gps = new GPSTracker(MainActivity.this);
                lat = Double.toString(gps.getLatitude());
                lon = Double.toString(gps.getLongitude());

                tvGPS.setText(lat + " : " + lon);

            }
        });

        btnWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                   // lat = Double.toString(gps.getLatitude());
                   // lon = Double.toString(gps.getLongitude());

                //tvTemp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "C");
                //tvDesc.setText(weather.currentCondition.getCondition() + "("
                  //      + weather.currentCondition.getDescr() + ")");
            }
        });

    }

    private class GetWeatherTask extends AsyncTask<String, Void, String> {
        private TextView textView;

        public GetWeatherTask(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected String doInBackground(String... strings) {
            String weather = "UNDEFINED";
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");
                String temp = String.valueOf(main.getDouble("temp"));

                String overview = topLevel.getJSONArray("weather").getJSONObject(0).get("main").toString();
                String desc =  topLevel.getJSONArray("weather").getJSONObject(0).get("description").toString();

                weather = temp + "C, " + overview +"( " + desc + ")";

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(String temp) {
            textView.setText("Current Weather: " + temp);
        }

    }


    // Static block to load the OpenCv lib...
    static {
        System.loadLibrary("opencv_java3");
       // System.loadLibrary("jniLibs");
    }
}
