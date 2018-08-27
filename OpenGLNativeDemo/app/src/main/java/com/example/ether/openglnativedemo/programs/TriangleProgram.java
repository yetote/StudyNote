package com.example.ether.openglnativedemo.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.example.ether.openglnativedemo.R;
import com.example.ether.openglnativedemo.data.VertexArray;

import static android.opengl.GLES20.glGetAttribLocation;

public class TriangleProgram extends ShaderProgram {
    private int aColor, aPosition;

    public TriangleProgram(Context context) {
        super(context, R.raw.vertex_shader, R.raw.fragment_shader);
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
