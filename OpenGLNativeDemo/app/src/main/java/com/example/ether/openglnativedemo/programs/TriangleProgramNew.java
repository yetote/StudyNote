package com.example.ether.openglnativedemo.programs;

import android.content.Context;

import com.example.ether.openglnativedemo.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;

public class TriangleProgramNew extends ShaderProgram {
    private int uColor, aPosition;

    public TriangleProgramNew(Context context) {
        super(context, R.raw.vertex_shader, R.raw.fragment_shader);
        aPosition = glGetAttribLocation(program, A_POSITION);
        uColor = glGetUniformLocation(program, U_COLOR);
    }

    public void setUniform(float r, float g, float b) {
        glUniform4f(uColor, r, g, b, 1.0f);
    }

    public int getAttributePosition() {
        return aPosition;
    }

}
