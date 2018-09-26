package com.example.ether.ndkplayer;

import android.os.HandlerThread;

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
        configEGLContext();
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
    public native void draw();
}
