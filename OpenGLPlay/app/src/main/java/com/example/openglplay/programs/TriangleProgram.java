package com.example.openglplay.programs;

import android.content.Context;

import com.example.openglplay.R;

import static android.opengl.GLES20.glGetAttribLocation;

public class TriangleProgram extends ShaderProgram {
    private int aColor, aPosition;

    public TriangleProgram(Context context) {
        super(context, R.raw.yuv_vertex_shader, R.raw.yuv_frag_shader);
        aColor = glGetAttribLocation(program, A_COLOR);
        aPosition = glGetAttribLocation(program, A_POSITION);
    }

    public int getAttributeColor() {
        return aColor;
    }

    public int getAttributePosition() {
        return aPosition;
    }

}
