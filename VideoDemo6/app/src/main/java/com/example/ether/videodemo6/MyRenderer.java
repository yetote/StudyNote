package com.example.ether.videodemo6;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class MyRenderer implements GLSurfaceView.Renderer {
    private Triangle triangle;
    private TriangleProgram program;
    private Context context;
    private float[] modelMatrix = new float[16];
    private float[] viewModelMatrxi = new float[16];

    public MyRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0, 0, 0, 0);
        triangle = new Triangle();
        program = new TriangleProgram(context);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        Matrix.perspectiveM(viewModelMatrxi, 0, 45, (float) width / (float) height, 1f, 10f);
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0, 0, -2.5f);
        rotateM(modelMatrix, 0, -60f, 1f, 0, 0);
        float[] temp = new float[16];
        multiplyMM(temp, 0, viewModelMatrxi, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, viewModelMatrxi, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        program.useProgram();
        program.setUniform(viewModelMatrxi);
        triangle.bindData(program);
        triangle.draw();
    }
}
