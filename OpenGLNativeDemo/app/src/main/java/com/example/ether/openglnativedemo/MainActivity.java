package com.example.ether.openglnativedemo;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView surfaceView = new GLSurfaceView(this);
        MyRenderer renderer = new MyRenderer(this);
        surfaceView.setEGLContextClientVersion(3);
        surfaceView.setRenderer(renderer);
        setContentView(surfaceView);
    }
}
