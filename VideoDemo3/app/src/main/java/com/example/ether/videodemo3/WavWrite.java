package com.example.ether.videodemo3;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class WavWrite {
    private File file;
    private ByteBuffer byteBuffer;
    private FileChannel channel;
    FileOutputStream outputStream;
    int size;
    int count;
    String path;
    private WavHeader header;
    private static final String TAG = "WavWrite";

    public WavWrite(int size, String path) {
        this.size = size;
        this.path = path;
        count = 0;
        byteBuffer = ByteBuffer.allocate(size);
        file = new File(path);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        channel = outputStream.getChannel();
    }

    /**
     * 写入wav文件头
     *
     * @param channelNum     通道数
     * @param sampleRateInHz 采样率
     * @return WavWrite
     */
    WavWrite writeHeader(int channelNum, int sampleRateInHz) {
        header = new WavHeader((short) channelNum, sampleRateInHz);

        byteBuffer.putInt(header.chunkId);
        byteBuffer.putInt(header.chunkSize);
        byteBuffer.putInt(header.format);

        byteBuffer.putInt(header.subChunk1Id);
        byteBuffer.putInt(header.subChunk1Size);
        byteBuffer.putShort(header.audioFormat);
        byteBuffer.putShort(header.numChannel);
        byteBuffer.putInt(header.sampleRate);
        byteBuffer.putInt(header.byteRate);
        byteBuffer.putShort(header.blockAlign);
        byteBuffer.putShort(header.bitsPerSample);

        byteBuffer.putInt(header.subChunk2Id);
        byteBuffer.putInt(header.subChunk2Size);
        try {
            byteBuffer.flip();
            channel.write(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    WavWrite writeData(byte[] data) {
        byteBuffer.put(data);

//        byteBuffer.put("1".getBytes());
        count += data.length;
        Log.e(TAG, "writeData: " + count);
        byteBuffer.flip();
        try {
            channel.write(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    WavWrite writeDataSize() {
        Log.e(TAG, "writeDataSize: " + count);
        int chunkSize = count + 36;
        int sampleChunk2Size = count;
//        byteBuffer.clear();
//        byteBuffer.putInt(chunkSize);
//        try {
//            byteBuffer.flip();
//            channel.write(byteBuffer, WavHeader.SUB_CHUNK_SIZE_POSITION);
//            byteBuffer.clear();
//            byteBuffer.putInt(sampleChunk2Size);
//            byteBuffer.flip();
//            channel.write(byteBuffer, WavHeader.SUB_CHUNK_SIZE2_POSITION);
//            byteBuffer.clear();
//            WavFileReader reader=new WavFileReader();
//            reader.openFile(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(path, "rw");
            ras.seek(4);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.putInt(chunkSize);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            ras.write(byteBuffer.array());
            byteBuffer.rewind();
            byteBuffer.putInt(sampleChunk2Size);
            ras.seek(40);
            ras.write(byteBuffer.array());
            WavFileReader reader = new WavFileReader();
            reader.openFile(path);
        } catch (Exception e) {
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

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
