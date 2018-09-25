package com.example.ether.videodemo;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.ether.videodemo.objects.Rect;
import com.example.ether.videodemo.programs.RectProgram;
import com.example.ether.videodemo.utils.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class MyRenderer implements GLSurfaceView.Renderer {
    private Rect rect;
    private RectProgram program;
    private Context context;
    private int texture;

    public MyRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0, 0, 0, 0);
        rect = new Rect();
        program = new RectProgram(context);
        texture = TextureHelper.loadTexture(context, R.drawable.bg);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        program.useProgram();
        program.setUniform(texture);
        rect.bindData(program);
        rect.draw();
    }
}
