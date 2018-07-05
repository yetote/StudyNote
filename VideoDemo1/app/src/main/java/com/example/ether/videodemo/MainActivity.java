package com.example.ether.videodemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ether.videodemo.objects.Rect;
import com.example.ether.videodemo.view.MyView;

public class MainActivity extends AppCompatActivity {

    private boolean surfaceSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(useMyView());
    }

    private View useMyView() {
        MyView view=new MyView(this);
        return view;
    }

    private ImageView useImage() {
        ImageView imageView = new ImageView(this);
        Drawable drawable = getResources().getDrawable(R.drawable.bg, null);
        imageView.setImageDrawable(drawable);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    private GLSurfaceView useGLSurfaceView() {
        GLSurfaceView surfaceView = new GLSurfaceView(this);
        MyRenderer renderer = new MyRenderer(this);
        surfaceView.setEGLContextClientVersion(3);
        surfaceView.setRenderer(renderer);

        return surfaceView;
    }
}
