package com.example.ether.videodemo9;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CameraDevice cameraDevice;
    private CameraManager cameraManager;
    public static final int PERMISSION_CAMERA_CODE = 1;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button btn;
    private String cameraId;
    private HandlerThread handlerThread;
    private CameraCaptureSession cameraCaptureSession;
    private Handler handler;
    private ImageReader imageReader;
    private MediaCodec mediaCodec;
    int i = 0;
    private static final String TAG = "MainActivity";
    public static final String MIME_TYPE = "Video/AVC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initEncode();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startVideo();
            }
        });
    }

    private void initEncode() {
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat format = new MediaFormat();
            format.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void startVideo() {
        try {
            CaptureRequest.Builder videoBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            videoBuilder.addTarget(imageReader.getSurface());
            CaptureRequest captureRequest = videoBuilder.build();
            cameraCaptureSession.setRepeatingRequest(captureRequest, null, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void initCamera() {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    openPreview();
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

    private void openPreview() {
        try {
            final CaptureRequest.Builder previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            cameraDevice.createCaptureSession(Arrays.asList(surfaceHolder.getSurface(), imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    previewBuilder.addTarget(surfaceHolder.getSurface());
                    CaptureRequest request = previewBuilder.build();
                    try {
                        cameraCaptureSession.setRepeatingRequest(request, null, handler);
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

    private void init() {
        btn = findViewById(R.id.btn);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                imageReader = ImageReader.newInstance(surfaceView.getWidth(), surfaceView.getHeight(), ImageFormat.YUV_420_888, 1);
                imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = reader.acquireNextImage();
                        i++;
                        Log.e(TAG, "onImageAvailable: " + i);
                        image.close();
                        encodeH264();
                    }
                }, new Handler(getMainLooper()));
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_CODE);
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
        handlerThread = new HandlerThread("camera2");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void encodeH264(byte[] data) {
        
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCamera();
                } else {
                    Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
