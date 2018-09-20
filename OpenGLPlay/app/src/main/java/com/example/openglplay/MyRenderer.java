package com.example.openglplay;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.openglplay.objects.Rect;
import com.example.openglplay.programs.RectProgram;
import com.example.openglplay.utils.TextureHelper;
import com.example.openglplay.utils.YUVHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
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
    int textureY, textureU, textureV;
    int w, h;
    private ByteBuffer yBuffer, uBuffer, vBuffer, yuvDataBuffer;
    private static final String TAG = "MyRenderer";
    private final String YUVPATH;
    private FileChannel channel;
    private FileInputStream stream;
    int count = 0;

    public MyRenderer(Context context, int w, int h, String path) {
        this.context = context;
        this.w = w;
        this.h = h;
        this.YUVPATH = path;
        ySize = w * h;
        uvSize = ySize / 4;
        yBuffer = ByteBuffer.allocate(w * h).order(ByteOrder.nativeOrder());
        uBuffer = ByteBuffer.allocate(w * h / 4).order(ByteOrder.nativeOrder());
        vBuffer = ByteBuffer.allocate(w * h / 4).order(ByteOrder.nativeOrder());
        yuvDataBuffer = ByteBuffer.allocate(w * h * 3 / 2).order(ByteOrder.nativeOrder());
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "MyRenderer: " + "文件不存在");
        }
        try {
            stream = new FileInputStream(file);
            channel = stream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        glClear(GL_COLOR_BUFFER_BIT);
        resolveYUV(readYUV());
        textureY = YUVHelper.loadTexture(w, h, yBuffer, YUVHelper.Y_TYPE);
        textureU = YUVHelper.loadTexture(w, h, uBuffer, YUVHelper.U_TYPE);
        textureV = YUVHelper.loadTexture(w, h, vBuffer, YUVHelper.V_TYPE);
        program.useProgram();
        program.setUniform(textureY, textureU, textureV);
        rect.bindData(program);
        rect.onDraw();
    }

    /**
     * 分解yuv数据
     *
     * @param yuvBuffer yuv数据
     */
    public void resolveYUV(ByteBuffer yuvBuffer) {
        if (!isBufferNull(yBuffer, uBuffer, vBuffer)) {
            clearBuffers(yBuffer, uBuffer, vBuffer);
        }
        if (yuvBuffer.limit() != ySize + 2 * uvSize) {
            Log.e(TAG, "resolve: " + "帧长度不对");
            return;
        }
        yuvBuffer.position(0);
        byte[] yb = new byte[ySize];
        byte[] uvb = new byte[uvSize];

        yuvBuffer.get(yb, 0, ySize);
        yBuffer.put(yb);
        yuvBuffer.get(uvb, 0, uvSize);
        uBuffer.put(uvb);
        yuvBuffer.get(uvb, 0, uvSize);
        vBuffer.put(uvb);
        if (yuvBuffer.position() != yuvBuffer.limit()) {
            Log.e(TAG, "resolve: " + "取出数据出错");
        }
    }

    /**
     * 清理buffer
     *
     * @param bufferArr buffer
     */
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

    /**
     * 读取YUV数据
     *
     * @return yuvBuffer
     */
    ByteBuffer readYUV() {

        if (yuvDataBuffer.position() != 0) {
            yuvDataBuffer.clear();
            yuvDataBuffer.position(0);
        }
        try {
            channel.read(yuvDataBuffer, count);
            if (yuvDataBuffer.position() != yuvDataBuffer.limit()) {
                Log.e(TAG, "readYUV: 读取数据出错" + "position:" + yuvDataBuffer.position() + "\n" + "limit:" + yuvDataBuffer.limit());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        count += yuvDataBuffer.position();
        yuvDataBuffer.position(0);
        return yuvDataBuffer;
    }
}
