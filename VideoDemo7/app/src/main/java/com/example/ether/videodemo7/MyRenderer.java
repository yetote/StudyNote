package com.example.ether.videodemo7;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.ether.videodemo7.objects.Triangle;
import com.example.ether.videodemo7.programs.TriangleProgram;
import com.example.ether.videodemo7.utils.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

public class MyRenderer implements GLSurfaceView.Renderer {
    private float[] projectionMatrix = new float[16];
    private Triangle triangle;
    private TriangleProgram program;
    private Context context;
    private int textureId;

    public MyRenderer(Context context) {
        this.context = context;
    }

    @Override

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0, 0, 0, 0);
        triangle = new Triangle();
        program = new TriangleProgram(context);
        textureId = TextureHelper.loadTexture(context, R.drawable.test);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        final float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
        else orthoM(projectionMatrix, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        program.useProgram();
        program.setUniform(projectionMatrix, textureId);
        triangle.bindData(program);
        triangle.draw();
    }
}
