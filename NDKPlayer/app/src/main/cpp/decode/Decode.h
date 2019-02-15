//
// Created by ether on 2018/10/23.
//

#ifndef BAMBOO_DECODE_H
#define BAMBOO_DECODE_H


#include "../util/BlockQueue.h"
#include <android/log.h>
#include <cstdint>
#include <unistd.h>

extern "C" {
#include <libavutil/frame.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
};
enum DECODE_TYPE {
    DECODE_VIDEO, DECODE_AUDIO, DECODE_UNKNOWN
};

class Decode {
public:
    void decode(const char *path, DECODE_TYPE decode_type, BlockQueue<AVFrame *> &blockQueue);

    void destroy();

private:
    AVCodecContext *pCodecCtx;
    AVFormatContext *pFmtCtx;
    AVPacket *pPacket;
    int index;
    AVStream *pStream;
    AVCodec *pCodec;
    AVFrame *pFrame;

    void findIndex(DECODE_TYPE type);

    void audio(BlockQueue<AVFrame *> &blockQueue);

    void video(BlockQueue<AVFrame *> &blockQueue);
};


#endif //BAMBOO_DECODE_H
