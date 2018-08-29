package com.example.ether.openglnativedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.ether.openglnativedemo.utils.TextRecourseReader;

public class Main2Activity extends AppCompatActivity {
    private EGLCRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        SurfaceView surfaceView = findViewById(R.id.surfaceView2);
        renderer = new EGLCRenderer();
        renderer.start();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                String vertexShader = TextRecourseReader.readTextFileFromResource(Main2Activity.this, R.raw.vertex_shader);
                String fragmentShader = TextRecourseReader.readTextFileFromResource(Main2Activity.this, R.raw.fragment_shader);

                renderer.draw(holder.getSurface(), width, height, vertexShader, fragmentShader);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        renderer.release();
        renderer = null;
        super.onDestroy();

    }


}
