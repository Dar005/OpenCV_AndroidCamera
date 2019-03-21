package com.example.darran.opencv_test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.darran.opencv_test.Constants.SCAN_IMAGE_LOCATION;
import static org.opencv.android.Utils.matToBitmap;

public class CameraActivity extends AppCompatActivity {


    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    ArrayList<Experiment> experimentLedger = new ArrayList<Experiment>();


    private SurfaceView sv;
    private ImageView imgPreview, gridImage;
    private Button previewImage;

    private static final String TAG = "CustomCameraAPI";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;


    // Location variables
    String lat = "";
    String lon = "";

    // Weather variables
    private static final String APP_ID = "b11dc521fd3aecc6374e2e331dc090e3";
    String weather = "";
    String units = "metric";
    String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;

    String fileLoc;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackGroundHandler;
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imgPreview = findViewById(R.id.imgPreview);
     //   previewImage = findViewById(R.id.btn_previewImage);
        gridImage = findViewById(R.id.imageView);

        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
       // textureView.setOnTouchListener((View.OnTouchListener) this);

        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;

        // capture image button
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               // gridImage.setVisibility(View.GONE);
                takePicture();
            }
        });
        // preview captured image NIT WORKING
//        previewImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //previewCapturedImage();
//            }
//        });

        // Start Location servive and get lat and lon
        startService(new Intent(CameraActivity.this,
                com.example.darran.opencv_test.GPSTracker.class));
        final GPSTracker gps = new GPSTracker(CameraActivity.this);
        lat = Double.toString(gps.getLatitude());
        lon = Double.toString(gps.getLongitude());

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

            cameraDevice.close();
            cameraDevice = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallBackListener = new CameraCaptureSession.CaptureCallback(){
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_LONG).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackGroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackGroundHandler = null;
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    protected void takePicture(){
        if (null == cameraDevice){
            Log.e(TAG, "cameraDevice is null");
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null){
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 640;
            if (jpegSizes != null && 0 < jpegSizes.length){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));


            /**
             * {@link com.darran.customcamera.FolderUtil}
             * {@link com.darran.customcamera.Constants}
             */
            // Creates a filename for the image and then creates a folder...
            String outPic = Constants.SCAN_IMAGE_LOCATION + File.separator + Utilities.generateFilename();
            FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION);

            fileLoc = outPic;

            final File file = new File(outPic);
            // final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try{




                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);

                        Bitmap bmp =  BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);

                        //Bitmap.createScaledBitmap(bmp, 120,120, false);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteOut = stream.toByteArray();
//
////
//                        Intent test = new Intent(CameraActivity.this, com.example.darran.opencv_test.ViewImage.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putByteArray("image", bytes);
//
//                        test.putExtras(bundle);

                      //  startActivityForResult(test, 2);

                        /**

                        //Create bitmap from byte array then crate a mat from the bitmap
                        Bitmap bmp =  BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Mat orig = new Mat();
                        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
                        Utils.bitmapToMat(bmp, orig);

                        // Convert Mat back to Bitmap then byte array for saving changed image.
                        Utils.matToBitmap(orig, bmp);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteOut = stream.toByteArray();


                         * At this point the image is capture and in byte array form, need to
                         * display the image to the user so they can select what is considered good.
                         * Might b possible to start activity for result and display an image view
                         * to the user and allow and them to interact with the image

                        //  Attempt to display image to user
                        //  Intent viewImage = new Intent(CameraActivity.this, com.example.darran.opencv_test.ViewImage.class);
                        //  viewImage.putExtra("image", byteOut);
                        //  startActivityForResult(viewImage, 2);

                        // Need to use byte array
                        String encodedImg = Base64.encodeToString(byteOut, Base64.DEFAULT);

                        // Get weather
                        String units = "metric";
                        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;
                        String weatherData =  new GetWeatherTask(weather).execute(url).get();


                        // Set format for time and date
                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");

                        // Create strings for time and date
                        String date = df.format(Calendar.getInstance().getTime());
                        String time = tf.format(Calendar.getInstance().getTime());

                        String [] imageDetails = new String[6];
                        imageDetails[0] = date;
                        imageDetails[1] = time;
                        imageDetails[2] = encodedImg;
                        imageDetails[3] = lat;
                        imageDetails[4] = lon;
                        imageDetails[5] = weatherData;

                        UploadData ud = new UploadData();
                        ud.execute(imageDetails);
*/
                        save(byteOut);
                        //save(bytes);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null){
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;

                    try{
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    }finally{
                        if (null != output){
                            output.close();
                        }
                    }
                }

            };

            reader.setOnImageAvailableListener(readerListener, mBackGroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();

                    String fileName = file.toString();

                    /**
                     * Need to try display the captured image here, this is where the camera
                     * preview is set/called. Hopefully i can stop the preview from loading and
                     * make the image capture display by using a while loop until user does
                     * something. Might be able to force the display to show the image and make the
                     * grid overlay disappear until the preview is called again....
                     * or maybe force a new Intent to display the image
                     *
                     * instead of calling the below function createCameraPreview() try create new
                     * function to display the captured image and and then call the preview when the
                     * image is returned
                     */

                    // This calls the preview(duh) but by stopping calling until im ready may mean
                    // i  can interact with the image....
                    /**
                     * remove the grid over lay
                     *
                     */

                    Intent test = new Intent(CameraActivity.this, com.example.darran.opencv_test.ViewImage.class);
                    test.putExtra("file", fileName);

                    startActivityForResult(test, 2);



                    // view capture

                  //  viewCapture();

                    //createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackGroundHandler);
                    }catch(CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackGroundHandler);

        }catch (CameraAccessException e){
            e.printStackTrace();
        }

    }


//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        if (requestCode == 2){
//            // good
//
//            if (resultCode == RESULT_OK){
//                Toast.makeText(CameraActivity.this, "Returned from view", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


    // display camera preview on the screen for the user
    protected void createCameraPreview(){
        try{


            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight()-100);
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // Camera already closed...
                    if (null == cameraDevice){
                        return;
                    }

                    // Run on ui needed to alow background thread to update the UI on main thread...
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridImage.setVisibility(View.VISIBLE);
                        }
                    });

                    // When session is ready start displaying
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);

        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }


    protected void viewCapture(){
        Log.e("Inside viewCapture", "______________------------IN VIEW CAPTURE----------_____________");
      //  createCameraPreview();

    }


    private void openCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera ans let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview(){
        if(null == cameraDevice){
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackGroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    private void closeCamera() {
        if(null != cameraDevice){
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader){
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                // Close app
                Toast.makeText(CameraActivity.this, "Sorry, you cannot use this app without granting permissions", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()){
            openCamera();
        }else{
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause(){
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

//    private void previewCapturedImage(){
//        try{
//
//            //int w = 200, h = 400;
//
//            //Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//
//            Bitmap bmp;
//            textureView.setVisibility(View.GONE);
//            imgPreview.setVisibility(View.VISIBLE);
//            //BitmapFactory.Options options = new BitmapFactory.Options();
//
//            // downsizing image as it throws OutOfMemory Exception for larger
//            // images
//            bmp = BitmapFactory.decodeFile(fileLoc);
//            imgPreview.setImageBitmap(bmp);
//
//
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }
//
//    }

    // Async task for sending data i.e. image date and time....
    public class UploadData extends AsyncTask<String, Void, JSONObject>{



        @Override
        protected JSONObject doInBackground(String... args){

            try{
                URL url = new URL("http://www.c0009839.candept.com/API/imageUpload.php");


                // DATE TIME IMAGE
                // put params in a JSON Object
                JSONObject dataParams = new JSONObject();
                dataParams.put("date", args[0]);
                dataParams.put("time", args[1]);
                dataParams.put("image", args[2]);
                dataParams.put("lat", args[3]);
                dataParams.put("lon", args[4]);
                dataParams.put("weather", args[5]);

                // Set up connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //send date
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(getPostDateString(dataParams));

                writer.flush();
                writer.close();
                os.close();

                // Get Response
                int responseCode = conn.getResponseCode();
                error = String.valueOf(conn.getResponseCode());

                 if (responseCode == HttpURLConnection.HTTP_OK){
                     inputStream = conn.getInputStream();
                     BufferedReader in  = new BufferedReader(new InputStreamReader(inputStream));
                     StringBuilder sb = new StringBuilder();
                     String line;

                     while(null!= (line = in.readLine())){
                         sb.append(line).append("\n");
                     }
                     in.close();
                     inputStream.close();
                     json = sb.toString();
                     Log.i("API Camera: ", json);
                 }
                 else{
                     Log.e("Buffer Error", "Error Getting Result " +responseCode);
                 }
                 try{
                     jObj = new JSONObject(json);
                     jObj.put("error_code", error);
                 }catch(JSONException e){
                     Log.e("JSON Parser", "Error Parsing Data " + e.toString());
                 }
            }catch(Exception e){
                Log.e("Exception: ", "Overall Try Block " + e.toString());
            }
            return jObj;
        }// end of doInBackground

        @Override
        protected void onPostExecute(JSONObject result){

            try {

                if (result != null){

                    String uploadSuccess = result.getString("message");
                    if (uploadSuccess.equals("Successfully uploaded data")){
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Unable to retrieve data from the server", Toast.LENGTH_LONG).show();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    // Turn json object to string for post
    public String getPostDateString(JSONObject params) throws Exception{

        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();

        while(itr.hasNext()){
            String key = itr.next();
            Object value = params.get(key);
            if(first){
                first = false;
            }else{
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    private class GetWeatherTask extends AsyncTask<String, Void, String>{

        private String weather;

        public GetWeatherTask(String weather){
            this.weather = weather;
        }

        @Override
        protected  String doInBackground(String... strings){
           String weather = "UNDEFINED";

           try {
               URL url = new URL(strings[0]);
               HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

               InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
               BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
               StringBuilder builder = new StringBuilder();

               String inputString;
               while ((inputString = bufferedReader.readLine()) != null){
                   builder.append(inputString);
               }

               JSONObject topLevel = new JSONObject(builder.toString());
               JSONObject main = topLevel.getJSONObject("main");
               String temp = String.valueOf(main.getDouble("temp"));

               String overview = topLevel.getJSONArray("weather")
                       .getJSONObject(0).get("main").toString();
               String desc = topLevel.getJSONArray("weather")
                       .getJSONObject(0).get("description").toString();

               weather = temp + "C, " + overview + "(" + desc + ")";

               urlConnection.disconnect();
           }catch (IOException | JSONException e){
               e.printStackTrace();
           }
           return weather;
        }

        @Override
        protected void onPostExecute(String temp) {
            weather = "Current Weather " + temp;
        }
    }
}