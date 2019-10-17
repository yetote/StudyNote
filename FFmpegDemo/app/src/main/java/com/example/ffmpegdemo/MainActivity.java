package com.example.ffmpegdemo;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Player player;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        player = new Player();
        if (getExternalFilesDir(Environment.DIRECTORY_MUSIC).exists()) {
            getExternalFilesDir(Environment.DIRECTORY_MUSIC).mkdirs();
        }

        String path = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath() + "/test.mp3";
        Log.e(TAG, "onCreate: " + path);
        player.prepare(path);

    }
}