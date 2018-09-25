//package com.example.openglplay.programs;
//
//import android.content.Context;
//
//import com.example.openglplay.R;
//
//import java.nio.ByteBuffer;
//
//import static android.opengl.GLES20.GL_LUMINANCE;
//import static android.opengl.GLES20.GL_TEXTURE0;
//import static android.opengl.GLES20.GL_TEXTURE_2D;
//import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
//import static android.opengl.GLES20.glActiveTexture;
//import static android.opengl.GLES20.glBindTexture;
//import static android.opengl.GLES20.glGetAttribLocation;
//import static android.opengl.GLES20.glGetUniformLocation;
//import static android.opengl.GLES20.glTexSubImage2D;
//import static android.opengl.GLES20.glUniform1i;
//
//public class RectProgram extends ShaderProgram {
//    public final int aPositionLocation;
//    public final int aTextureCoordinatesLocation;
//
//    private final int u_TextureLocation;
//
//
//    public RectProgram(Context context) {
//        super(context, R.raw.yuv_vertex_shader, R.raw.yuv_frag_shader);
//        aPositionLocation = glGetAttribLocation(program, A_POSITION);
//        aTexCoordLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
//
//        uTexYLocation = glGetUniformLocation(program, U_TEXY);
//        uTexULocation = glGetUniformLocation(program, U_TEXU);
//        uTexVLocation = glGetUniformLocation(program, U_TEXV);
//        yuvLocation = new int[]{uTexYLocation, uTexULocation, uTexVLocation};
//    }
//
//    public void setUniform(int textureId, ByteBuffer buffer, int type) {
//        buffer.position(0);
//        glActiveTexture(GL_TEXTURE0 + type);
//        glBindTexture(GL_TEXTURE_2D, textureId);
//        if (type == 0) {
//            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 320, 240, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
//        } else {
//            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 320 / 2, 240 / 2, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
//        }
//        glUniform1i(u_TextureLocation, type);
//    }
//
//    public int getPositionAttributeLocation() {
//        return aPositionLocation;
//    }
//
//    public int getTextureCoordinatesAttributeLocation() {
//        return aTextureCoordinatesLocation;
//    }
//}
