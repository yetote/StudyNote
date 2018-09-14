package com.example.ether.ndkdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int PERMISSION_WRITE_CODE = 1;

    static {
        System.loadLibrary("native-lib");
    }

    private String ioPath;
    private String videoOutputPath;
    private String videoInputPath;
    private String audioOutputPath;
    private String audioInputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ioPath = this.getExternalCacheDir().getPath() + "/test.txt";

        videoOutputPath = this.getExternalCacheDir().getPath() + "/test.yuv";
        videoInputPath = this.getExternalCacheDir().getPath() + "/test.mp4";
        audioOutputPath = this.getExternalCacheDir().getPath() + "/test.pcm";
        audioInputPath = this.getExternalCacheDir().getPath() + "/sample.mp3";
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_CODE);
//        } else {
//            ioTest(ioPath);
        decodeAudio(audioInputPath, audioOutputPath);
//        }
    }

    public native int add(int a, int b);

    public native void ioTest(String ioPath);

    public native void decodeVideo(String inputPath, String outputPath);

    public native void decodeAudio(String inputPath, String outputPath);
}
