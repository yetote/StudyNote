package com.example.ffmpegdemo;

public class Player {
    static {
        System.loadLibrary("native-lib");
    }

    public Player() {
    }

    public native void prepare(String path);
}
