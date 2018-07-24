package com.example.ether.videodemo9;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
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
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CameraDevice cameraDevice;
    private CameraManager cameraManager;
    public static final int PERMISSION_CAMERA_CODE = 1;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button btn, stop;
    private String cameraId;
    private HandlerThread handlerThread;
    private CameraCaptureSession cameraCaptureSession;
    private Handler handler;
    private ImageReader imageReader;
    private MediaCodec mediaCodec;
    int i = 0;
    private static final String TAG = "MainActivity";
    public static final String MIME_TYPE = "video/avc";
    private FileWrite write;
    String path;
    private boolean isVideoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        btn.setOnClickListener(v -> {
            isVideoing = true;
            startVideo();
        });
        stop.setOnClickListener(v -> isVideoing = false);
    }

    private void initEncode(int width, int height) {
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 800, 600);
//            byte[] csd_info = {0, 0, 0, 1, 103, 100, 0, 40, -84, 52, -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3, 3, -23, 0, 0, -22, 96, -108, 0, 0, 0, 1, 104, -18, 60, -128};
//            format.setByteBuffer("csd-0", ByteBuffer.wrap(csd_info));
            format.setInteger(MediaFormat.KEY_BIT_RATE, 800 * 600 * 5);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1280 * 720 * 10);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
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
        stop = findViewById(R.id.stop);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initEncode(1280/2, 720/2);

                imageReader = ImageReader.newInstance(surfaceView.getWidth()/2, surfaceView.getHeight()/2, ImageFormat.YUV_420_888, 1);
                imageReader.setOnImageAvailableListener(reader -> {
                    Image image = reader.acquireNextImage();
                    i++;
                    Log.e(TAG, "onImageAvailable: " + i);
//                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] temp = getDataFromImage(image);
//                    buffer.get(temp);
                    encodeH264(temp);
//                    write.writeData(getDataFromImage(image));
                    Log.e(TAG, "surfaceCreated: " + surfaceView.getWidth() + surfaceView.getHeight());
                    image.close();
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
        path = this.getExternalCacheDir().getPath() + "/test.h264";
        write = new FileWrite(path);
    }

    private void encodeH264(byte[] data) {
        int index = mediaCodec.dequeueInputBuffer(System.nanoTime());
        if (index >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            if (data.length > 0) {
                Log.e(TAG, "encodeH264: " + data.length);
                Log.e(TAG, "encodeH264: " + inputBuffer.capacity());
                inputBuffer.put(data);
                mediaCodec.queueInputBuffer(index, 0, data.length, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
            }
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex;
        do {
            outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outIndex >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outIndex);
                outputBuffer.position(bufferInfo.offset);
                byte[] temp = new byte[bufferInfo.size];
                outputBuffer.get(temp);

                write.writeData(temp);
                mediaCodec.releaseOutputBuffer(outIndex, false);
            }
        } while (outIndex >= 0);
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

    private static byte[] getDataFromImage(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format)/8];
        Log.e(TAG, "getDataFromImage: data.length " + ImageFormat.getBitsPerPixel(format));
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();

            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            Log.v(TAG, "pixelStride " + pixelStride);
            Log.v(TAG, "rowStride " + rowStride);
            Log.v(TAG, "width " + width);
            Log.v(TAG, "height " + height);
            Log.v(TAG, "buffer size " + buffer.remaining());

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }
}
