package com.example.ether.videodemo8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileWrite {
    private ByteBuffer buffer;
    private int size;
    private FileChannel channel;
    String path;
    private FileOutputStream fos;

    public FileWrite(int size, String path) {
        this.size = size;
        this.path = path;
        buffer = ByteBuffer.allocate(size);
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel = fos.getChannel();
    }

    public void writeData(byte[] data) {
        buffer.put(data);
        buffer.flip();
        try {
            channel.write(buffer);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            fos.close();
            channel.close();
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
