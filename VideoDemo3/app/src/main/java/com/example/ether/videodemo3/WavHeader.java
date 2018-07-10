package com.example.ether.videodemo3;

public class WavHeader {
    public static final int SUB_CHUNK_SIZE_POSITION=4;
    public static final int SUB_CHUNK_SIZE2_POSITION=40;
    public int  chunkId = 0x52494646;//块id RIFF
    public int chunkSize=0;//块大小
    public int format = 0x57415645;//格式  "WAVE"

    public int subChunk1Id = 0x666d7420; //子块1id "fmt";
    public int subChunk1Size = 16; //子块1大小,PCM为16
    public short audioFormat = 1; //语音格式，pcm为1
    public short numChannel = 1; //通道数，单声道为1，双声道为2
    public int sampleRate = 44100;// 采样率
    public int byteRate = 0;//字节率
    public short blockAlign = 0;//块对齐
    public short bitsPerSample = 16;// 样本位数，8位或16位，8位已不支持

    public int subChunk2Id =0x64617461; // 子块2id "data";
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
}
