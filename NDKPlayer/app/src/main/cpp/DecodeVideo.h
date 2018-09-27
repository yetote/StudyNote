//
// Created by ether on 2018/9/26.
//

#ifndef NDKPLAYER_DECODEVIDEO_H
#define NDKPLAYER_DECODEVIDEO_H

#include <EGL/egl.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
}

class DecodeVideo {
public:
    AVFrame *decode(const char *videoPath, EGLDisplay display, EGLSurface surface);

private:
    AVFrame *pFrame;
    AVCodecContext *pCodecCtx;
    AVFormatContext *pFmtCtx;
    AVCodec *pCodec;
    AVPacket *pPacket;
    AVStream *pStream;
};


#endif //NDKPLAYER_DECODEVIDEO_H
