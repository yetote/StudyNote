package com.example.ffmpegdemo;

/**
 * @author yetote QQ:503779938
 * @name FFmpegDemo2
 * @class name：com.example.ffmpegdemo
 * @class describe
 * @time 2018/9/3 11:08
 * @change
 * @chang time
 * @class describe
 */
public class VideoUtils {
    static {
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
        System.loadLibrary("avdevice");
        System.loadLibrary("native-lib");
    }

    /**
     * 视频解码
     *
     * @param inputPath  视频封装地址
     * @param outputPath 解码后yuv地址
     */
    public static native void decode(String inputPath, String outputPath);
}
