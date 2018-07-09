package com.example.ether.videodemo2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class WavWrite {
    private ByteBuffer byteBuffer;
    private FileChannel channel;
    FileOutputStream outputStream;
    int size;

    public WavWrite(int size, String path) {
        this.size = size;
        byteBuffer = ByteBuffer.allocate(size)
                .order(ByteOrder.nativeOrder());
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        channel = outputStream.getChannel();
    }

    WavWrite writeData(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            byteBuffer.put(data[i]);
        }
//        byteBuffer.put("123".getBytes());
        byteBuffer.flip();
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    WavWrite close() {
        try {
            channel.close();
            outputStream.close();
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
}
