package com.example.ether.videodemo7.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.example.ether.videodemo7.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class TriangleProgram extends ShaderProgram {
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    public TriangleProgram(Context context) {
        super(context, R.raw.triangle_vertex_shader, R.raw.triangle_fragment_shader);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
    }

    public void setUniform(float[] matrix,int textureId){
        glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,textureId);
        glUniform1f(uTextureUnitLocation,0);

    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
