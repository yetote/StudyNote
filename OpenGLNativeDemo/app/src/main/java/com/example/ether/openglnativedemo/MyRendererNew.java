package com.example.ether.openglnativedemo;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.ether.openglnativedemo.objects.Triangle;
import com.example.ether.openglnativedemo.programs.TriangleProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

/**
 * @author ether QQ:503779938
 * @name OpenGLNativeDemo
 * @class name：com.example.ether.openglnativedemo
 * @class describe
 * @time 2018/8/27 16:41
 * @change
 * @chang time
 * @class describe
 */
public class MyRendererNew implements GLSurfaceView.Renderer {
    private Triangle triangle;
    private TriangleProgram program;
    private Context context;

    public MyRendererNew(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1f, 0f, 1f, 0f);
        triangle = new Triangle();
        program = new TriangleProgram(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        program.useProgram();
        triangle.bindData(program);
        triangle.draw();
    }
}
