//
// Created by ether on 2018/9/12.
//

#ifndef NEWPLAYER_ENCODE_H
#define NEWPLAYER_ENCODE_H



extern "C" {
#include "../includes/libavformat/avformat.h"
#include "../includes/libavutil/avutil.h"
#include "../includes/libavcodec/avcodec.h"
#include "../includes/libavutil/frame.h"
};

class Encode {
public:
    Encode(AVMediaType type);

    /**
     * 初始化ffmpeg
     * @param inputPath  解码文件路径
     */
    void init(char *inputPath);


    /**
     * 开始解码
     */
    void startEncode();

    ~Encode();

private:
    AVMediaType streamType;
    AVFormatContext *pFmtCtx;
    AVStream *pStream;
    AVCodec *pCodec;
    AVCodecContext *pCodecCtx;
    AVPacket *pPacket;
    AVFrame *pFrame,*pEncodeFrame;
    /**
     * 音频解码
     */
    void audioEncode();

    /**
     * 视频解码
     */
    void videoEncode();
};


#endif //NEWPLAYER_ENCODE_H
