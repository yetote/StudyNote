package com.example.ether.videodemo6;

import android.opengl.GLES20;

public class Triangle {
    private final float[] vertexData = new float[]{
            0, 0.433f, 1, 0, 0,
            -0.5f, -0.433f, 0, 1, 0,
            0.5f, -0.433f, 0, 0, 1
    };
    public static final int VERTEX_COMPONENT_COUNT = 2;
    public static final int COLOR_COMPONENT_COUNT = 3;
    public static final int STRIDE = (VERTEX_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * 4;
    private VertexArray vertexArray;

    public Triangle() {
        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(TriangleProgram program) {
        vertexArray.setVertexAttribPointer(0, program.getPositionAttributeLocation(), VERTEX_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(VERTEX_COMPONENT_COUNT, program.getColorAttributeLocation(), COLOR_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
