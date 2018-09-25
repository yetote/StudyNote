package com.example.openglplay.programs;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.example.openglplay.R;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static android.opengl.GLES20.GL_LUMINANCE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexSubImage2D;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLUtils.texImage2D;

/**
 * @author yetote QQ:503779938
 * @name OpenGLPlay
 * @class name：com.example.openglplay.programs
 * @class describe
 * @time 2018/9/17 16:52
 * @change
 * @chang time
 * @class describe
 */
public class RectProgra extends ShaderProgram {
    public final int aPositionLocation;
    public final int aTexCoordLocation;
    public final int uTexYLocation;
    public final int uTexULocation;
    public final int uTexVLocation;
    private static final String TAG = "RectProgram";
    private static int[] yuvLocation;
    private static final int[] YUV_TEXTURE_ID = {GL_TEXTURE0, GL_TEXTURE1, GL_TEXTURE2};

    public RectProgra(Context context) {
        super(context, R.raw.yuv_vertex_shader, R.raw.yuv_frag_shader);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTexCoordLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        uTexYLocation = glGetUniformLocation(program, U_TEXY);
        uTexULocation = glGetUniformLocation(program, U_TEXU);
        uTexVLocation = glGetUniformLocation(program, U_TEXV);
        yuvLocation = new int[]{uTexYLocation, uTexULocation, uTexVLocation};
    }

    public int getAttrPositionLocation() {
        return aPositionLocation;
    }

    public int getAttrTexCoordLocation() {
        return aTexCoordLocation;
    }

    public void setUniform(int textureId, Buffer buffer, int type) {
        buffer.position(0);
        glActiveTexture(GL_TEXTURE0 + type);
        glBindTexture(GL_TEXTURE_2D, textureId);
        if (type == 0) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320, 240, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320 / 2, 240 / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
        }
        glUniform1i(yuvLocation[type], type);

    }
}
