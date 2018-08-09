package com.example.ether.ffmpegdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button avcodecBtn, avformatBtn, avfilterBtn;
    private TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        FFmpegTest fFmpegTest = new FFmpegTest();
        avcodecBtn.setOnClickListener(v -> tv.setText(fFmpegTest.avcodecInfo()));
        avformatBtn.setOnClickListener(v -> tv.setText(fFmpegTest.avformatInfo()));
        avfilterBtn.setOnClickListener(v -> tv.setText(fFmpegTest.avfilterInfo()));
    }

    private void init() {
        avcodecBtn = findViewById(R.id.acodec_info_btn);
        avformatBtn = findViewById(R.id.avformat_info_btn);
        avfilterBtn = findViewById(R.id.avfilter_info_btn);
        tv = findViewById(R.id.tv);
    }


}
