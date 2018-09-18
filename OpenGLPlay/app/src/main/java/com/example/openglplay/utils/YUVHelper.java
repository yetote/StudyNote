package com.example.openglplay.utils;

import android.util.Log;

import java.nio.ByteBuffer;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_LUMINANCE;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static com.example.openglplay.utils.MathUtil.even;
import static com.example.openglplay.utils.MathUtil.powerOfTwo;

/**
 * @author yetote QQ:503779938
 * @name OpenGLPlay
 * @class name：com.example.openglplay.utils
 * @class describe
 * @time 2018/9/18 13:25
 * @change
 * @chang time
 * @class describe
 */
public class YUVHelper {
    private static final String TAG = "YUVHelper";

    public static int loadTexture(int w, int h, ByteBuffer bufferData) {
        final int[] textureObjectIds = new int[1];

        glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "loadTexture: " + "opengl无法创建纹理对象");
            return 0;
        }
        if (bufferData == null) {
            Log.e(TAG, "输入数据为null: " + "无法加载位图图片");
            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        if (!even(w, h)) {
            Log.e(TAG, "loadTexture: " + "yuv数据的宽高必须是偶数");
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w, h, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, bufferData);
        bufferData.clear();
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }
}
