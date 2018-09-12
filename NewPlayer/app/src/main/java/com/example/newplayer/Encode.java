package com.example.newplayer;

/**
 * @author yetote QQ:503779938
 * @name NewPlayer
 * @class name：com.example.newplayer
 * @class describe
 * @time 2018/9/12 15:25
 * @change
 * @chang time
 * @class describe
 */
public class Encode {
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * 视频解码
     *
     * @param inputPath 视频输入文件
     */
    public native void videoEncode(String inputPath);

    /**
     * 音频解码
     *
     * @param inputPath 音频输入文件
     */
    public native void audioEncode(String inputPath);
}
