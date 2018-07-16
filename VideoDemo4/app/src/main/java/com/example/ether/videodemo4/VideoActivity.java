package com.example.ether.videodemo4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class VideoActivity extends AppCompatActivity {
    private CameraDevice cameraDevice;
    private SurfaceView surfaceView;
    private Handler handler;
    private Button btn;
    private ImageReader imageReader;
    private SurfaceView img;
    private static final int PERMISSION_CAMERA_CODE = 1;
    private SurfaceHolder surfaceHolder;
    private CameraCaptureSession captureSession;
    private static final String TAG = "VideoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_CODE);
                } else {
                    initCamera();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        btn.setOnClickListener(v -> takePhoto());
    }

    private void initCamera() {
        HandlerThread handlerThread = new HandlerThread("camera2");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        imageReader = ImageReader.newInstance(surfaceView.getWidth(), surfaceView.getHeight(), ImageFormat.YUV_420_888, 1);
        imageReader.setOnImageAvailableListener(reader -> {
            cameraDevice.close();
            surfaceView.setVisibility(View.GONE);
            Image image = imageReader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            buffer.flip();
            while (buffer.hasRemaining()) {

                Log.e(TAG, "initCamera: " + buffer.get());
            }
//            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            if (bitmap != null) {
//               img.
//            }
        }, new Handler(getMainLooper()));
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    openPreView();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启预览
     */
    private void openPreView() {
        try {
            final CaptureRequest.Builder previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            cameraDevice.createCaptureSession(Arrays.asList(surfaceHolder.getSurface(), imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        captureSession = session;
                        previewBuilder.addTarget(surfaceHolder.getSurface());
                        CaptureRequest request = previewBuilder.build();
                        captureSession.setRepeatingRequest(request, null, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        if (cameraDevice == null) {
            Log.e(TAG, "takePhoto: "+null );
            return;}
        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureBuilder.addTarget(imageReader.getSurface());
            CaptureRequest request = captureBuilder.build();
            captureSession.capture(request, null, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        surfaceView = findViewById(R.id.videoSur);
        img = findViewById(R.id.videoImg);
        btn = findViewById(R.id.videoBtn);
        surfaceHolder = surfaceView.getHolder();
    }
}
