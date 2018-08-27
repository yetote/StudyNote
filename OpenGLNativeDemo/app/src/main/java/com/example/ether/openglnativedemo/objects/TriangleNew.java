package com.example.ether.openglnativedemo.objects;

import com.example.ether.openglnativedemo.data.VertexArray;
import com.example.ether.openglnativedemo.programs.TriangleProgramNew;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;

public class TriangleNew {
    private final float[] vertexData = new float[]{

            -0.5f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.0f,
    };
    private VertexArray vertexArray;
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (COLOR_COMPONENT_COUNT + POSITION_COMPONENT_COUNT) * 4;

    public TriangleNew() {
        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(TriangleProgramNew program) {
        vertexArray.setVertexAttributePointer(0, program.getAttributePosition(), POSITION_COMPONENT_COUNT, 0);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
