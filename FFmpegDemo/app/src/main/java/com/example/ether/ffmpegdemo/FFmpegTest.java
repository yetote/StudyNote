package com.example.ether.ffmpegdemo;

public class FFmpegTest {
    static {
        System.loadLibrary("avcodec");
        System.loadLibrary("avfilter");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("swscale");
        System.loadLibrary("avdevice");
        System.loadLibrary("native-lib");
    }
    public native String avcodecInfo();
    public native String avformatInfo();
    public native String avfilterInfo();
}
