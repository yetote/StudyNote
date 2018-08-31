package com.example.ether.myplayer;

import android.os.Build;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.ether.myplayer
 * @class 播放器生命周期类
 * @time 2018/8/30 11:10
 * @change
 * @chang time
 * @class describe
 */
public abstract class AbstractMediaCodecDecoderLifeCycle {
    private MediaCodecDecoder mediaCodecDecoder;
    private boolean isUseMediaCodec = false;
    private static final String TAG = "AbstractMediaCodecDecod";

    /**
     * 是否选择使用硬解
     *
     * @param value 是否选择使用硬解
     */
    public void setUseMediaCodec(boolean value) {
        isUseMediaCodec = value;
    }

    /**
     * 底层代码判断是否可以使用硬解
     *
     * @return 是否可以使用
     */
    public boolean isHardwareCodecFromNative() {
        Log.e(TAG, "isHardwareCodecForNative: 是否选择使用硬解" + isUseMediaCodec);
        boolean ret = false;
        if (isUseMediaCodec) {
            //获取制造商信息
            String manufacturer = Build.MANUFACTURER;
            //获取设备型号
            String model = Build.MODEL;
            Log.e(TAG, "isHardwareCodecForNative: " + manufacturer + "\n" + model);
            ret = !MediaCodecDecoder.isInAndroidHardwareBlacklist();
        }
        return ret;
    }

    /**
     * 通过底层创建解码器
     *
     * @param width   宽度
     * @param height  高度
     * @param texId   纹理id
     * @param sps     sps帧是数据
     * @param spsSize sps数据大小
     * @param pps     pps帧数据
     * @param ppsSize pps数据大小
     * @return 创建是否成功
     */
    public boolean createVideoDecoderFromNative(int width, int height, int texId, byte[] sps, int spsSize, byte[] pps, int ppsSize) {
        mediaCodecDecoder = new MediaCodecDecoder();
        return mediaCodecDecoder.createVideoDecoder(width, height, texId, sps, spsSize, pps, ppsSize);
    }

    /**
     * 4-1:当需要解码一个packet(compressedData)的时候调用这个方法
     *
     * @param frameData 帧数据
     * @param inputSize 数据大小
     * @param timeStamp 时间戳
     * @return 解码数据大小
     */
    public int decodeFrameFromNative(byte[] frameData, int inputSize, long timeStamp) {
        return mediaCodecDecoder.decodeFrame(frameData, inputSize, timeStamp);
    }

    /**
     * 4-2:调用了2-1之后需要调用这方法获取纹理
     *
     * @return 时间戳
     */
    public long updateTexImageFromNative() {
        long rec = 0;
        if (mediaCodecDecoder == null) {
            return rec;
        } else {
            return mediaCodecDecoder.updateTextureImage();
        }
    }


    /**
     * 当快进等操作执行的时候需要清空现有的解码器内部的数据
     */
    public void flushMediaCodecBuffersFromNative() {
        // TODO: 2018/8/30 快进操作暂时割舍
//        if (null != mediaCodecDecoder) {
//            mediaCodecDecoder.beforeSeek();
//        }
    }


    /**
     * 销毁解码器
     */
    public void cleanupDecoderFromNative() {
        if (null != mediaCodecDecoder) {
            mediaCodecDecoder.cleanUpDecoder();
        }
    }
}
