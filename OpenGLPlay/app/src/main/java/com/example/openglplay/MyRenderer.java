package com.example.openglplay;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.openglplay.objects.Rect;
import com.example.openglplay.programs.RectProgra;
import com.example.openglplay.utils.YUVHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static com.example.openglplay.utils.YUVHelper.U_TYPE;
import static com.example.openglplay.utils.YUVHelper.V_TYPE;
import static com.example.openglplay.utils.YUVHelper.Y_TYPE;

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
    RectProgra program;
    int textureY, textureU, textureV;
    int w, h;
    private ByteBuffer yBuffer, uBuffer, vBuffer, yuvDataBuffer;
    private static final String TAG = "MyRenderer";
    private final String YUVPATH;
    private FileChannel channel;
    private FileInputStream stream;
    int count = 0;
    int textures[] = new int[3];

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
        glClearColor(1, 1, 1, 0);
        rect = new Rect();
        program = new RectProgra(context);
//        textureY=TextureHelper.loadTexture(context,R.drawable.test);
//        resolveYUV(readYUV());
        YUVHelper.loadTexture(textures);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


        resolveYUV(readYUV());

        program.useProgram();
//        program.setUniform(textureY, textureU, textureV);
        program.setUniform(textures[0], yBuffer, Y_TYPE);
        program.setUniform(textures[1], uBuffer, U_TYPE);
        program.setUniform(textures[2], vBuffer, V_TYPE);
        rect.bindData(program);
        rect.draw();
    }

    /**
     * 分解yuv数据
     *
     * @param yuvBuffer yuv数据
     */
    public void resolveYUV(ByteBuffer yuvBuffer) {
        if (yuvBuffer == null) {
            return;
        }
        if (!isBufferNull(yBuffer, uBuffer, vBuffer)) {
            clearBuffers(yBuffer, uBuffer, vBuffer);
        }
        if (yuvBuffer.limit() != ySize + 2 * uvSize) {
            Log.e(TAG, "resolve: " + "帧长度不对");
            return;
        }
        yuvBuffer.position(0);
        byte[] yb = new byte[ySize];
        byte[] ub = new byte[uvSize];
        byte[] vb = new byte[uvSize];

        yuvBuffer.get(yb, 0, ySize);
        yBuffer.put(yb);
        Log.e(TAG, "resolveYUV: y" + yuvBuffer.position());

        yuvBuffer.get(ub, 0, uvSize);
        Log.e(TAG, "resolveYUV: u" + yuvBuffer.position());
        uBuffer.put(ub);

        yuvBuffer.get(vb, 0, uvSize);
        Log.e(TAG, "resolveYUV: v" + yuvBuffer.position());
        vBuffer.put(vb);

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
            if (channel.read(yuvDataBuffer, count) != -1) {
                if (yuvDataBuffer.position() != yuvDataBuffer.limit()) {
                    Log.e(TAG, "readYUV: 读取数据出错" + "position:" + yuvDataBuffer.position() + "\n" + "limit:" + yuvDataBuffer.limit());
                    yuvDataBuffer.mark();
                    return null;
                }
                Log.e(TAG, "readYUV: " + yuvDataBuffer.position());

                count += yuvDataBuffer.position();
                yuvDataBuffer.position(0);
                return yuvDataBuffer;
            } else {
                Log.e(TAG, "readYUV: " + "数据读完，共：" + count / (ySize + uvSize + uvSize) + "帧");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
