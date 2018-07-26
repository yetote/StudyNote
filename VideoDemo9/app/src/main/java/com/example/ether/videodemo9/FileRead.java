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
                return 0;
            }
            channel.position(count);
            Log.e(TAG, "readData: count" + count + "\n" + "size" + size);
            int result = channel.read(buffer);
            Log.e(TAG, "readData: " + channel.size());
            if (result == -1) {
                Log.e(TAG, "readData: " + "数据全部读取");
                return 0;
            }
            buffer.flip();
            byte[] temp = new byte[size];
            for (int i = 0; i < buffer.limit(); i++) {
                temp[i] = buffer.get();
            }

            int index = findHead(temp, firstIndex);
            if (index != -1) {
                firstIndex = index;
                int secIndex = findHead(temp, firstIndex + 1024);
                if (secIndex != -1) {
                    secondIndex = secIndex;
                    System.arraycopy(temp, firstIndex, data, 0, secondIndex - firstIndex);
                    Log.e(TAG, "readData: fps" + (secondIndex - firstIndex));
                    count += secondIndex;
                    Log.e(TAG, "readData: " + "找到帧头");
                    buffer.clear();
                    return secondIndex - firstIndex;
                } else {
                    Log.e(TAG, "readData: " + "未找到第二帧头，copy数组");
                    count += firstIndex;
                    size = size << 1;
                }
            } else {
                Log.e(TAG, "readData: " + "未找到第一帧帧头");
                size = size << 1;
            }
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param data   数据
     * @param offset i帧或p帧的索引
     */
    int findHead(byte data[], int offset) {
        Log.e(TAG, "findHead: " + offset + "\n" + data.length);
        for (int i = offset; i < data.length; i++) {
            //00 00 00 01 x
            if (i + 4 > data.length) {
                return -1;
            } else {
                if (data[i] == 0x00
                        && data[i + 1] == 0x00
                        && data[i + 2] == 0x00
                        && data[i + 3] == 0x01
                        && isVideoFrameHeadType(data[i + 4])) {
                    return i;
                }
            }
            if (i + 3 > data.length) {
                return -1;
            } else {
                if (data[i] == 0x00
                        && data[i + 1] == 0x00
                        && data[i + 2] == 0x00
                        && isVideoFrameHeadType(data[i + 3])) {
                    return i;
                }
            }
            //00 00 01 x
        }
        return -1;
    }

    boolean isVideoFrameHeadType(byte data) {
        return data == 0x65 || data == 0x61 || data == 0x41;
    }
}
