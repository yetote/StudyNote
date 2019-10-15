//
// Created by ether on 2019/10/14.
//

#ifndef FFMPEGDEMO_DECODE_H
#define FFMPEGDEMO_DECODE_H

#include <string>
#include <android/log.h>

extern "C" {
#include "includes/libavcodec/avcodec.h"
#include "includes/libavformat/avformat.h"
};
#define LOGE(LOG_TAG, ...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define Decode_TAG "Decode"

class Decode {
public:
    void prepare(std::string path);

    Decode();

    virtual ~Decode();

private:

    AVFormatContext *pFmtCtx;
    AVStream *pStream;
    AVCodec *pCodec;
    AVCodecContext *pCodecCtx;

    void showErr(int errocde);
};


#endif //FFMPEGDEMO_DECODE_H
