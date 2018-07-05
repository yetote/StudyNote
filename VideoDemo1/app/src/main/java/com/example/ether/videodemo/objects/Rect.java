package com.example.ether.videodemo.objects;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.example.ether.videodemo.Constants;
import com.example.ether.videodemo.data.VertexArray;
import com.example.ether.videodemo.programs.RectProgram;

public class Rect {
    public static final int POSITION_COMPONENT_COUNT = 2;
    public static final int TEXTURE_COMPONENT_COUNT = 2;
    public static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
    private final float[] vertexData = new float[]{
            1f, 1f, 1f, 1f,
            -1f, 1f, 0f, 1f,
            -1f, -1f, 0f, 0f,

            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            1f, 1f, 1f, 1f
    };
    private VertexArray vertexArray;

    public Rect() {
        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(RectProgram program) {
        vertexArray.setVertexAttribPointer(0, program.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,program.getTextureCoordinatesAttributeLocation(),TEXTURE_COMPONENT_COUNT,STRIDE);
    }

    public void draw() {
        GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }
}
