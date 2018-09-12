//
// Created by ether on 2018/9/12.
//

#include "Encode.h"
#include "../includes/libavcodec/avcodec.h"
#include "../includes/libavutil/avutil.h"
#include "../includes/libavutil/imgutils.h"
#include <android/log.h>

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wuhuannan",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);

Encode::Encode(AVMediaType type) {
    streamType = type;
}

void Encode::init(char *inputPath) {

    //初始化
    av_register_all();
    pFmtCtx = avformat_alloc_context();
    //打开文件
    if (avformat_open_input(&pFmtCtx, inputPath, NULL, NULL) != 0) {
        LOGE("无法打开输入文件");
        return;
    }
    //获取输入文件信息
    if (avformat_find_stream_info(pFmtCtx, NULL) < 0) {
        LOGE("无法获取输入文件信息");
        return;
    }
    int streamIdx = -1;
    //寻找对应的流
    for (int i = 0; i < pFmtCtx->nb_streams; ++i) {
        if (pFmtCtx->streams[i]->codecpar->codec_type == streamType) {
            streamIdx = i;
            break;
        }
    }
    if (streamIdx == -1) {
        LOGE("未找到对应的数据流");
        return;
    }
    //获取解码器
    pStream = pFmtCtx->streams[streamIdx];
    pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    if (pCodec == NULL) {
        LOGE("找不到解码器");
        return;
    }
    //打开解码器
    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("无法打开解码器");
        return;
    }
    LOGE("ffmpeg初始化完成")
}

void Encode::startEncode() {
    //为数据包分配内存
    pPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    //为解码帧分配内存
    pFrame = av_frame_alloc();
    //为解码完成后的帧分配内存
    pEncodeFrame = av_frame_alloc();
    switch (streamType) {
        case AVMEDIA_TYPE_VIDEO :
            videoEncode();
            break;
        case AVMEDIA_TYPE_AUDIO:
            audioEncode();
            break;
        case AVMEDIA_TYPE_UNKNOWN:
            LOGE("AVMEDIA_TYPE_UNKNOWN");
            break;
        case AVMEDIA_TYPE_DATA:
            LOGE("AVMEDIA_TYPE_DATA");
            break;
        case AVMEDIA_TYPE_SUBTITLE:
            LOGE("字幕流，暂不支持");
            break;
        case AVMEDIA_TYPE_ATTACHMENT:
            LOGE("AVMEDIA_TYPE_ATTACHMENT");
            break;
        case AVMEDIA_TYPE_NB:
            LOGE("AVMEDIA_TYPE_NB");
            break;
    }
}

void Encode::audioEncode() {

}

void Encode::videoEncode() {
    //计算一帧yuv420p数据所需要的大小
    u_int8_t *outBuffer = static_cast<u_int8_t *>(av_malloc(
            static_cast<size_t>(av_image_get_buffer_size(AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height, 1))));
    //向缓冲区装填数据，参数含义写在ffmpeg源码的方法内
    av_image_fill_arrays(pFrame->data,pFrame->linesize,outBuffer,AV_PIX_FMT_YUV420P,pCodecCtx->width,pCodecCtx->height,1);
}

Encode::~Encode() {

}