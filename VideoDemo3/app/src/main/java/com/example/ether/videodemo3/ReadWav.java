package com.example.ether.videodemo3;

import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class ReadWav {
    private DataInputStream dataInputStream;
    private ByteBuffer buffer;
    private FileInputStream inputStream;
    private FileChannel channel;
    int size;
    String path;

    public ReadWav(int size, String path) {
        this.size = size;
        this.path = path;
        buffer = ByteBuffer.allocateDirect(4)
                .order(ByteOrder.LITTLE_ENDIAN);
        try {
            inputStream = new FileInputStream(path);
            dataInputStream = new DataInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public short read() {
        short data ;
        byte[] temp = new byte[2];
        try {
            dataInputStream.read(temp);
            data = byteArrayToShort(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static float byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
}
