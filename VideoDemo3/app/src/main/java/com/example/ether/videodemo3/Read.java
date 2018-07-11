package com.example.ether.videodemo3;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class Read {
    private ByteBuffer headerBuffer, dataBuffer;
    private FileInputStream inputStream;
    private FileChannel channel;
    private String path;
    int size;
    private WavHeader header;
    public static final int DATA_POSITION = 44;

    public Read(String path, int size) {
        this.path = path;
        this.size = size;
        headerBuffer = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
        dataBuffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        try {
            inputStream = new FileInputStream(path);
            channel = inputStream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        header = new WavHeader();
        readHeader();
    }

    public void readHeader() {
        try {
            channel.read(headerBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        headerBuffer.flip();
        header.set1ChunkId("" + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get());
        header.set2ChunkSize(headerBuffer.getInt());
        header.set3Format("" + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get());
        header.set4SubChunk1Id("" + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get());
        header.set5SubChunk1Size(headerBuffer.getInt());
        header.set6AudioFormat(headerBuffer.getShort());
        header.set7NumChannel(headerBuffer.getShort());
        header.set8SampleRate(headerBuffer.getInt());
        header.set9ByteRate(headerBuffer.getInt());
        header.setABlockAlign(headerBuffer.getShort());
        header.setBBitsPerSample(headerBuffer.getShort());
        header.setCSubChunk2Id("" + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get() + (char) headerBuffer.get());
        header.setDSubChunk2Size(headerBuffer.getInt());
        System.out.println(header.toString());
//            }
        headerBuffer.clear();
    }

    public byte[] readData() {
        try {
            byte[] data;
            channel.read(dataBuffer,DATA_POSITION);
            data = dataBuffer.array();
            dataBuffer.clear();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
