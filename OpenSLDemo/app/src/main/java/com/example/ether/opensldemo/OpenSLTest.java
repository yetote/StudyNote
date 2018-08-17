package com.example.ether.opensldemo;

public class OpenSLTest {
    static {
        System.loadLibrary("native-lib");
    }

    public native int create();

    public native void play(String pacPath);
}
