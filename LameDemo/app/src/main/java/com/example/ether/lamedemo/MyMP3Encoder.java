
package com.example.ether.lamedemo;

public class MyMP3Encoder {
    static {
        System.loadLibrary("lame-lib");
    }

    /**
     * @param pcmPath pcm文件路径
     * @param audioChannels 声道数
     * @param bitRate 比特率
     * @param SampleRate 采样率
     * @param mapPath 生成的MP3路径
     * @return
     */
    public native int init(String pcmPath,int audioChannels,int bitRate,int SampleRate,String mapPath);

    public native void encode();

    public native void destroy();
}
