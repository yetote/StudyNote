//
// Created by ether on 2018/9/26.
//

#include "DecodeVideo.h"
#include "Rect.h"
#include <android/log.h>


#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);

AVFrame *DecodeVideo::decode(const char *videoPath, EGLDisplay display, EGLSurface eglSurface) {
    av_register_all();
    pFmtCtx = avformat_alloc_context();
    if (avformat_open_input(&pFmtCtx, videoPath, NULL, NULL) != 0) {
        LOGE("打开视频资源文件失败");
        return 0;
    }
    if (avformat_find_stream_info(pFmtCtx, NULL) < 0) {
        LOGE("获取视频信息失败");
        return 0;
    }
    int videoIdx = -1;
    for (int i = 0; i < pFmtCtx->nb_streams; ++i) {
        if (pFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIdx = i;
            break;
        }
    }
    if (videoIdx == -1) {
        LOGE("未找到视频流");
        return 0;
    }
    pStream = pFmtCtx->streams[videoIdx];
    pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    if (pCodec == NULL) {
        LOGE("找不到对应的解码器：%d", pStream->codecpar->codec_id)
        return 0;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    avcodec_parameters_to_context(pCodecCtx, pStream->codecpar);
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("打开解码器失败");
        return 0;
    }
    pPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    pFrame = av_frame_alloc();
    AVFrame *decodeFrame = av_frame_alloc();
    uint8_t *outputBuffer = static_cast<uint8_t *>(av_malloc(
            av_image_get_buffer_size(AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height, 1)));
    av_image_fill_arrays(pFrame->data, pFrame->linesize, outputBuffer, AV_PIX_FMT_YUV420P,
                         pCodecCtx->width, pCodecCtx->height, 1);
    struct SwsContext *swsCtx = sws_getContext(pCodecCtx->width, pCodecCtx->height,
                                               pCodecCtx->pix_fmt, pCodecCtx->width,
                                               pCodecCtx->height, AV_PIX_FMT_YUV420P,
                                               SWS_BICUBIC, NULL, NULL, NULL);
    Rect *rect = new Rect();
    int ret;
    while (av_read_frame(pFmtCtx, pPacket) >= 0) {
        if (pPacket->stream_index == videoIdx) {
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                ret = avcodec_receive_frame(pCodecCtx, decodeFrame);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    break;
                } else if (ret == AVERROR_EOF) {
                    LOGE("%s", "解码完成");
                    return 0;
                } else if (ret < 0) {
                    LOGE("%s", "解码出错");
                    break;
                }
                sws_scale(swsCtx,
                          decodeFrame->data,
                          decodeFrame->linesize,
                          0,
                          pCodecCtx->height,
                          decodeFrame->data,
                          decodeFrame->linesize);
                LOGE("A");
                rect->setUniform(decodeFrame);
                rect->draw(display, eglSurface);

            }
        }
    }
}