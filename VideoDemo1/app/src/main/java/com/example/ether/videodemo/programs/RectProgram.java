package com.example.ether.videodemo.programs;

import android.content.Context;
import android.opengl.GLES30;

import com.example.ether.videodemo.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

public class RectProgram extends ShaderProgram {
    public final int aPositionLocation;
    public final int aTextureCoordinatesLocation;

    private final int u_TextureLocation;


    public RectProgram(Context context) {
        super(context, R.raw.image_vertex_shader, R.raw.image_fragment_shader);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        u_TextureLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
    }

    public void setUniform(int textureId) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(u_TextureLocation,0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
