package com.example.ether.myplayer;

import android.util.Log;
import android.view.Surface;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.ether.myplayer
 * @class 播放器控制器
 * @time 2018/8/30 11:04
 * @change
 * @chang time
 * @class describe
 */
public class PlayerController extends AbstractMediaCodecDecoderLifeCycle {
    private static final String TAG = "PlayerController";
    private OnInitializedCallback initializedCallback;

    public void init(final String srcFilenameParam, final Surface surface, final int width, final int height,
                     final OnInitializedCallback onInitializedCallback) {
        this.init(srcFilenameParam, new int[]{-1, -1, -1}, -1, 0.5f, 1.0f, surface, width, height,
                onInitializedCallback);
    }

    /**
     * 初始化
     *
     * @param srcFilenameParam      文件地址
     * @param maxAnalyzeDuration    不知道干啥的 最大解析时间，解析啥？？？
     * @param probesize             不知道干啥的
     * @param minBufferedDuration   ？？？
     * @param maxBufferedDuration   ？？？
     * @param surface               surface
     * @param width                 宽度
     * @param height                高度
     * @param onInitializedCallback 初始化接口
     */
    public void init(final String srcFilenameParam, final int[] maxAnalyzeDuration, final int probesize,
                     final float minBufferedDuration, final float maxBufferedDuration, final Surface surface, final int width,
                     final int height, final OnInitializedCallback onInitializedCallback) {
        initializedCallback = onInitializedCallback;

        prepare(srcFilenameParam, maxAnalyzeDuration, maxAnalyzeDuration.length, probesize, true, minBufferedDuration,
                maxBufferedDuration, width, height, surface);
    }


    /**
     * 播放完成回调
     */
    public void onCompletion() {
        Log.i(TAG, "播放完成");
    }

    /**
     * 播放暂停回调
     */

    /**
     * 解码异常回调
     */
    public void videoDecodeException() {
        Log.i("problem", "videoDecodeException...");
    }

    /**
     * 从方法名没看出来是干啥的，从方法体来看应该是设置surfaceview的尺寸的
     *
     * @param width    宽度
     * @param height   高度
     * @param duration 时间？？？
     */
    public void viewStreamMetaCallback(int width, int height, float duration) {
        Log.i("problem", "width is : " + width + ";height is : " + height + ";duration is : " + duration);
    }

    public void stopPlay() {
        new Thread() {
            @Override
            public void run() {
                PlayerController.this.stop();
            }
        }.start();
    }

    public native void onPause();

    public native void onSurfaceCreated(Object surface);

    public native void resetRenderSize(int left, int top, int width, int height);

    public native void play();

    public native void stop();

    public native void onSurfaceDestroyed(Object surface);

    public native boolean prepare(String srcFilenameParam, int[] maxAnalyzeDurations, int size, int probesize, boolean fpsProbesizeConfigured,
                                  float minBufferedDuration, float maxBufferedDuration, int width, int height, Object surface);
}
