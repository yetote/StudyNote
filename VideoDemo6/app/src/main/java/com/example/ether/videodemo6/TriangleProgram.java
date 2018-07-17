package com.example.ether.videodemo6;

import android.content.Context;
import android.opengl.GLES20;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 *
 */
public class TriangleProgram extends ShaderProgram {
    private final int aPositionLocation;
    private final int aColorLocation;
    private final int uMatrixLocation;

    public TriangleProgram(Context context) {
        super(context, R.raw.triangle_vertex_shader, R.raw.triagnle_fragment_shader);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }

    public void setUniform(float[] matrix) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }
}
