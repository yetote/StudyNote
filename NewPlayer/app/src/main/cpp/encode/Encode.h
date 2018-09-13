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
     * @return 找到的数据流索引
     */
    int init(char *inputPath);


    /**
     * 开始解码
     * @param streamIdx 流索引
     */
    void startEncode(int streamIdx);

    ~Encode();

private:
    AVMediaType streamType;
    AVFormatContext *pFmtCtx;
    AVStream *pStream;
    AVCodec *pCodec;
    AVCodecContext *pCodecCtx;
    AVPacket *pPacket;
    AVFrame *pFrame, *pEncodeFrame;

    /**
     * 音频解码
     * @param audioIdx 音频流索引
     */
    void audioEncode(int audioIdx);

    /**
     * 视频解码
     * @param videoIdx 视频流索引
     */
    void videoEncode(int videoIdx);
};


#endif //NEWPLAYER_ENCODE_H
