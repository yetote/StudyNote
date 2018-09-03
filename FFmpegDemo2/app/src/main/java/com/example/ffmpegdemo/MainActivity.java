package com.example.ffmpegdemo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String inputPath = this.getExternalCacheDir().getPath() + "/test.mp4";
        String outputPath = this.getExternalCacheDir().getPath() + "/test.yuv";
        Log.e(TAG, "onCreate: "+outputPath );
        VideoUtils.decode(inputPath, outputPath);
    }


}
