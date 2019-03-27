package com.example.darran.opencv_test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextReg extends AppCompatActivity {

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
        setContentView(R.layout.activity_text_reg);

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


        // Start Location servive and get lat and lon
        startService(new Intent(TextReg.this,
                com.example.darran.opencv_test.GPSTracker.class));
        final GPSTracker gps = new GPSTracker(TextReg.this);
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
            Toast.makeText(TextReg.this, "Saved:" + file, Toast.LENGTH_LONG).show();
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

                    Intent test1 = new Intent(TextReg.this, com.example.darran.opencv_test.textResult.class);
                    test1.putExtra("file", fileName);

                    startActivityForResult(test1, 2);



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
                    Toast.makeText(TextReg.this, "Configuration change", Toast.LENGTH_SHORT).show();
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

                ActivityCompat.requestPermissions(TextReg.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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
                Toast.makeText(TextReg.this, "Sorry, you cannot use this app without granting permissions", Toast.LENGTH_LONG).show();
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
}
