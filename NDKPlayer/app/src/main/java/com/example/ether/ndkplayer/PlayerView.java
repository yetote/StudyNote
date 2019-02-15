package com.example.ether.ndkplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

/**
 * @author yetote QQ:503779938
 * @name Bamboo
 * @class name：com.example.bamboo.util
 * @class describe
 * @time 2018/10/20 14:02
 * @change
 * @chang time
 * @class describe
 */
public class PlayerView extends HandlerThread  {
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

    public native void configEGLContext();


    public native void destroyEGLContext();

    public native void play(String path, String vertexCode, String fragCode, Surface surface,int w,int h);
}
