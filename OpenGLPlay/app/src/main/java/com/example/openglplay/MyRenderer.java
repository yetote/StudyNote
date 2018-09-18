package com.example.openglplay;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.openglplay.objects.Rect;
import com.example.openglplay.programs.RectProgram;
import com.example.openglplay.utils.TextureHelper;
import com.example.openglplay.utils.YUVHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

/**
 * @author yetote QQ:503779938
 * @name OpenGLPlay
 * @class name：com.example.openglplay
 * @class describe
 * @time 2018/9/17 16:10
 * @change
 * @chang time
 * @class describe
 */
public class MyRenderer implements GLSurfaceView.Renderer {
    private final int ySize;
    private final int uvSize;
    private Context context;
    Rect rect;
    RectProgram program;
    int[] textureIds;
    int w, h;
    private ByteBuffer yBuffer, uBuffer, vBuffer;
    private static final String TAG = "MyRenderer";

    public MyRenderer(Context context, int w, int h) {
        this.context = context;
        this.w = w;
        this.h = h;
        ySize = w * h;
        uvSize = ySize / 4;
        yBuffer = ByteBuffer.allocate(w * h).order(ByteOrder.nativeOrder());
        uBuffer = ByteBuffer.allocate(w * h / 4).order(ByteOrder.nativeOrder());
        vBuffer = ByteBuffer.allocate(w * h / 4).order(ByteOrder.nativeOrder());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0, 0, 0, 0);
        rect = new Rect();
        program = new RectProgram(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    public void resolve(ByteBuffer yuvBuffer) {
        if (isBufferNull()) {
            clearBuffers(yBuffer, uBuffer, vBuffer);
        }
        if (yuvBuffer.limit() != ySize + 2 * uvSize) {
            Log.e(TAG, "resolve: " + "帧长度不对");
            return;
        }
        yuvBuffer.position(0);
        byte[] b = new byte[]{};
        yuvBuffer.get(b, 0, ySize);
        yBuffer.put(b);
        yuvBuffer.get(b, 0, uvSize);
        uBuffer.put(b);
        yuvBuffer.get(b, 0, uvSize);
        vBuffer.put(b);
        if (yuvBuffer.position() != yuvBuffer.limit()) {
            Log.e(TAG, "resolve: " + "取出数据出错");
        }
    }

    private void clearBuffers(ByteBuffer... bufferArr) {
        for (ByteBuffer buffer : bufferArr) {
            buffer.clear();
        }
    }

    private boolean isBufferNull(ByteBuffer... bufferArr) {
        for (ByteBuffer aBuffer : bufferArr) {
            if (aBuffer.position() != 0) {
                return false;
            }
        }
        return true;
    }

}
