package com.example.ether.videodemo3;

public class WavHeader {
    public static final int SUB_CHUNK_SIZE_POSITION = 4;
    public static final int SUB_CHUNK_SIZE2_POSITION = 40;

    public String chunkId = "RIFF";//块id RIFF
    public int chunkSize = 0;//块大小
    public String format = "WAVE";//格式  "WAVE"

    public String subChunk1Id = "fmt "; //子块1id "fmt";
    public int subChunk1Size = 16; //子块1大小,PCM为16
    public short audioFormat = 1; //语音格式，pcm为1
    public short numChannel = 1; //通道数，单声道为1，双声道为2
    public int sampleRate = 44100;// 采样率
    public int byteRate = 0;//字节率
    public short blockAlign = 0;//块对齐
    public short bitsPerSample = 16;// 样本位数，8位或16位，8位已不支持

    public String subChunk2Id = "data"; // 子块2id "data";
    public int subChunk2Size = 0;//子块2大小

    /**
     * wav文件头
     *
     * @param numChannel 通道
     * @param sampleRate 采样率
     */
    public WavHeader(short numChannel, int sampleRate) {
        this.numChannel = numChannel;
        this.sampleRate = sampleRate;
        byteRate = sampleRate * numChannel * bitsPerSample / 8;
        blockAlign = (short) (numChannel * bitsPerSample / 8);
    }

    public WavHeader() {
    }

    public void set1ChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public void set2ChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void set3Format(String format) {
        this.format = format;
    }

    public void set4SubChunk1Id(String subChunk1Id) {
        this.subChunk1Id = subChunk1Id;
    }

    public void set5SubChunk1Size(int subChunk1Size) {
        this.subChunk1Size = subChunk1Size;
    }

    public void set6AudioFormat(short audioFormat) {
        this.audioFormat = audioFormat;
    }

    public void set7NumChannel(short numChannel) {
        this.numChannel = numChannel;
    }

    public void set8SampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void set9ByteRate(int byteRate) {
        this.byteRate = byteRate;
    }

    public void setABlockAlign(short blockAlign) {
        this.blockAlign = blockAlign;
    }

    public void setBBitsPerSample(short bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public void setCSubChunk2Id(String subChunk2Id) {
        this.subChunk2Id = subChunk2Id;
    }

    public void setDSubChunk2Size(int subChunk2Size) {
        this.subChunk2Size = subChunk2Size;
    }

    @Override
    public String toString() {
        return "WavHeader{" +
                "chunkId='" + chunkId + '\'' +
                ", chunkSize=" + chunkSize +
                ", format='" + format + '\'' +
                ", subChunk1Id='" + subChunk1Id + '\'' +
                ", subChunk1Size=" + subChunk1Size +
                ", audioFormat=" + audioFormat +
                ", numChannel=" + numChannel +
                ", sampleRate=" + sampleRate +
                ", byteRate=" + byteRate +
                ", blockAlign=" + blockAlign +
                ", bitsPerSample=" + bitsPerSample +
                ", subChunk2Id='" + subChunk2Id + '\'' +
                ", subChunk2Size=" + subChunk2Size +
                '}';
    }
}
