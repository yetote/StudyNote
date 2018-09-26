//
// Created by ether on 2018/9/26.
//

#ifndef NDKPLAYER_DECODEVIDEO_H
#define NDKPLAYER_DECODEVIDEO_H

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/imgutils.h>
class DecodeVideo {
    AVFrame *pFrame;
    AVCodecContext *pCodecCtx;
    AVFormatContext *pFmtCtx;
    AVCodec *pCodec;
    AVPacket *pPacket;
    AVStream *pStream;
    void decode();

};


#endif //NDKPLAYER_DECODEVIDEO_H
