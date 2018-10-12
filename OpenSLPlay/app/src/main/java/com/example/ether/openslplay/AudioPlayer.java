package com.example.ether.openslplay;

/**
 * @author ether QQ:503779938
 * @name OpenSLPlay
 * @class name：com.example.ether.openslplay
 * @class describe
 * @time 2018/10/12 14:24
 * @change
 * @chang time
 * @class describe
 */
public class AudioPlayer {
    static {
        System.loadLibrary("native-lib");
    }
    public static native void play(String audioPath,String outPath);
}
