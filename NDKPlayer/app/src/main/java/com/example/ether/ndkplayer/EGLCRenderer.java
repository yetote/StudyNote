package com.example.ether.ndkplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

/**
 * @author ether QQ:503779938
 * @name OpenGLNativeDemo
 * @class name：com.example.ether.openglnativedemo.utils
 * @class describe
 * @time 2018/8/28 11:03
 * @change
 * @chang time
 * @class describe
 */
public class EGLCRenderer extends HandlerThread {
    static {
        System.loadLibrary("native-lib");
    }

    public EGLCRenderer() {
        super("EGLCRenderer");
    }

    @Override
    public synchronized void start() {
        super.start();
        new Handler(getLooper()).post(this::configEGLContext);
    }

    void release() {
        new Handler(getLooper()).post(() -> {
            destroyEGLContext();
            quit();
        });
    }


    public native void configEGLContext();


    public native void destroyEGLContext();


    public native int draw(String videoPath, String vertexShaderCode, String fragShaderCode, Surface surface, int w, int h);
}
