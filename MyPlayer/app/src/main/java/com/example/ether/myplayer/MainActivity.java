package com.example.ether.myplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.ether.myplayer
 * @class MainActivity
 * @time 2018/8/30 11:04
 * @change
 * @chang time
 * @class describe
 */
public class MainActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private Button playBtn, pauseBtn;
    private ProgressBar progressBar;
    private SurfaceHolder surfaceHolder;
    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                playerController = new PlayerController();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    private void initView() {
        surfaceView = findViewById(R.id.surfaceView);
        playBtn = findViewById(R.id.play);
        pauseBtn = findViewById(R.id.pauseBtn);
        progressBar = findViewById(R.id.progressBar);

        surfaceHolder = surfaceView.getHolder();
    }


}
