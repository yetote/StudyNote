package com.example.ether.videodemo2;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ReadWav {
    private ByteBuffer buffer;
    private FileInputStream inputStream;
    private FileChannel channel;
    int size;
    String path;

    public ReadWav(int size, String path) {
        this.size = size;
        this.path = path;
        buffer = ByteBuffer.allocateDirect(size);
        try {
            inputStream = new FileInputStream(path);
            channel = inputStream.getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float[] read() {
        float[] data = new float[3];
        try {
            for (int i = 0; i < 3; i++) {
                int resultCode = channel.read(buffer);
                data[i] = buffer.getFloat(i);
                if (resultCode == -1) {
                    return data;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
