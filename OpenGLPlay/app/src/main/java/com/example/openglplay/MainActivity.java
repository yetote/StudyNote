package com.example.openglplay;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    private MyRenderer renderer;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glSurfaceView = findViewById(R.id.glSurfaceView);
        String path = this.getExternalCacheDir().getPath() + "/test.yuv";
        Log.e(TAG, "onCreate: "+path );
        renderer = new MyRenderer(this, 320, 240, path);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
    }
}
