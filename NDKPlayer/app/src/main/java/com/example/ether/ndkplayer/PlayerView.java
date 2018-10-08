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
        super("PlayerView");
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

    public native void configEGLContext();


    public native void destroyEGLContext();


    public native int draw(String videoPath, String vertexShaderCode, String fragShaderCode, Surface surface,int w,int h);
}
