package com.example.ether.openglnativedemo;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.ether.openglnativedemo.utils.TextRecourseReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private float angleX, angleY;

    public MyRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexShader = TextRecourseReader.readTextFileFromResource(context, R.raw.vertex_shader);
        String fragmentShader = TextRecourseReader.readTextFileFromResource(context, R.raw.fragment_shader);
        create(vertexShader, fragmentShader);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        change(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        draw(angleX, angleY);
    }

    public native void create(String vertexShader, String fragmentShader);

    public native void draw(float angleX, float angleY);

    public native void change(int width, int height);
}
