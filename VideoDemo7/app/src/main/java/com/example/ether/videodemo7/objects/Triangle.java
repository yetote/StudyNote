package com.example.ether.videodemo7.objects;

import android.opengl.GLES20;

import com.example.ether.videodemo7.Constants;
import com.example.ether.videodemo7.data.VertexArray;
import com.example.ether.videodemo7.programs.TriangleProgram;

import java.util.Vector;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;

public class Triangle {
    private final float[] vertexData = new float[]{
            0, 0.433f, 0.25f, 0.466f,
            -0.5f, -0.433f, 0.25f, 0.234f,
            0.5f, -0.433f, 0.75f, 0.235f
    };
    public static final int VERTEX_COMPONENT_COUNT = 2;
    public static final int TEXTURE_COMPONENT_COUNT = 2;
    public static final int STRIDE = (VERTEX_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
    private VertexArray vertexArray;

    public Triangle() {
        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(TriangleProgram program) {
        vertexArray.setVertexAttribPointer(0, program.getPositionAttributeLocation(), VERTEX_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(VERTEX_COMPONENT_COUNT, program.getTextureCoordinatesAttributeLocation(), TEXTURE_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
