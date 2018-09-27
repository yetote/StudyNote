package com.example.ether.ndkplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

/**
 * @author ether QQ:503779938
 * @name NDKPlayer
 * @class name：com.example.ether.ndkplayer
 * @class describe
 * @time 2018/9/26 10:43
 * @change
 * @chang time
 * @class describe
 */
public class PlayerView extends HandlerThread {
    static {
        System.loadLibrary("native-lib");
    }

    public PlayerView() {
        super("GLThread");
    }

    @Override
    public synchronized void start() {
        super.start();
        new Handler(getLooper()).post(this::configEGLContext);
    }
    void release(){
        new Handler(getLooper()).post(() -> {
            destroyEGLContext();
            quit();
        });
    }
    /**
     * 配置egl启动环境
     */
    public native void configEGLContext();

    /**
     * 销毁egl环境
     */
    public native void destroyEGLContext();

    /**
     * 绘制
     */
    public native int draw(String videoPath, String vertexShaderCode, String fragShaderCode, Surface surface);
}
