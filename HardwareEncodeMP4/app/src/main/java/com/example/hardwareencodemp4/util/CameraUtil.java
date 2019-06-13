package com.example.hardwareencodemp4.util;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;

import androidx.core.app.ActivityCompat;

/**
 * @author yetote QQ:503779938
 * @name HardwareEncodeMP4
 * @class name：com.example.hardwareencodemp4.util
 * @class describe
 * @time 2019/5/27 10:57
 * @change
 * @chang time
 * @class describe
 */
public class CameraUtil {
    private Context context;
    private CameraDevice cameraDevice;
    private String cameraIds;
    private int frontCameraId = -1, backCameraId = -1;
    private CaptureRequest.Builder previewBuilder, recordBuilder;
    private CameraCaptureSession captureSession;
    private CameraCharacteristics backCameraCharacteristics, frontCameraCharacteristics;

    public CameraUtil(Context context) {
        this.context = context;
    }

    private void openCamera(int width, int height) {

    }

}
