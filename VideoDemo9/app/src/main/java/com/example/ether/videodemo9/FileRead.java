package com.example.ether.videodemo9;

import android.provider.Settings;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class FileRead {
    private static final String TAG = "FileRead";
    private ByteBuffer buffer;
    private String path;
    private FileInputStream stream;
    private FileChannel channel;
    int count = 0;
    int size;
    int firstIndex, secondIndex = 0;

    public FileRead(String path, int size) {
        this.path = path;
        this.size = size;
        buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        try {
            stream = new FileInputStream(path);
            channel = stream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int readData(byte[] data) {
        try {
            if (count >= channel.size()) {
                Log.e(TAG, "readData: " + "数据全部读取");
                return -1;
            }
            channel.position(count);
            int result = channel.read(buffer);
            Log.e(TAG, "readData: " + channel.size());
            if (result == -1) {
                Log.e(TAG, "readData: " + "数据全部读取");
                return -1;
            }
            buffer.flip();
            byte[] temp = new byte[size];
            buffer.get(temp);

            firstIndex = findHead(temp, firstIndex);
            if (firstIndex != -1) {
                secondIndex = findHead(temp, firstIndex);
                if (secondIndex != -1) {
                    System.arraycopy(temp, firstIndex, data, 0, secondIndex - firstIndex);
                }
            }

            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        count += size;
        return null;
    }

    /**
     * @param data   数据
     * @param offset i帧或p帧的索引
     */
    int findHead(byte data[], int offset) {
        for (int i = offset; i < data.length; i++) {
            //00 00 00 01 x
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01 && isVideoFrameHeadType(data[i + 4])) {

                return i;
            }
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && isVideoFrameHeadType(data[i + 3])) {
                return i;
            }
            //00 00 01 x
        }
        return -1;
    }

    boolean isVideoFrameHeadType(byte data) {
        return data == 0x65 || data == 0x61 || data == 0x41;
    }
}
