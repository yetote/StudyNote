package com.yetote.ijkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private IjkMediaPlayer ijkMediaPlayer;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/test.mp4";
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                createPlayer();
                ijkMediaPlayer.setDisplay(holder);
//                ijkMediaPlayer.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void createPlayer() {
        if (ijkMediaPlayer == null) {
            ijkMediaPlayer = new IjkMediaPlayer();
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                ijkMediaPlayer.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ijkMediaPlayer._prepareAsync();
        }
    }
}
