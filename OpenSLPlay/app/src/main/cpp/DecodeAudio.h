//
// Created by ether on 2018/10/12.
//

#ifndef OPENSLPLAY_DECODEAUDIO_H
#define OPENSLPLAY_DECODEAUDIO_H

#include <android/log.h>
#include "AudioUtil.h"
#include "BlockQueue.h"
extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/frame.h>
#include <libswresample/swresample.h>
};

class DecodeAudio {

public:
    void decode(const char *audioPath, const char *outPath,BlockQueue<audioType> &blockQueue);

private:
    AVFormatContext *pFmtCtx;
    AVCodec *pCodec;
    AVStream *pStream;
    AVCodecContext *pCodecCtx;
    AVPacket *pPacket;
    AVFrame *pFrame;
};


#endif //OPENSLPLAY_DECODEAUDIO_H
