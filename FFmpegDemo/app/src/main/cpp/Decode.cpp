//
// Created by ether on 2019/10/14.
//

#include "Decode.h"

Decode::Decode() {}

int readFrameCallBack(void *ctx);

int readFrameCallBack(void *ctx) {
    LOGE(Decode_TAG, "%s:读取数据无响应", __func__);
    return 1;
}


void Decode::prepare(std::string path) {
    av_register_all();
    avformat_network_init();
    pFmtCtx = avformat_alloc_context();
    pFmtCtx->interrupt_callback.callback = readFrameCallBack;
    pFmtCtx->interrupt_callback.opaque = this;
    int rst = -1;
    rst = avformat_open_input(&pFmtCtx, path.c_str(), nullptr, nullptr);
    if (rst != 0) {
        showErr(rst);
        return;
    }
    int audioIndex = -1;
    for (int i = 0; i < pFmtCtx->nb_streams; ++i) {
        if (pFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioIndex = i;
            break;
        }
    }
    if (audioIndex == -1) {
        LOGE(Decode_TAG, "%s:未找到流信息", __func__);
        return;
    }
    pStream = pFmtCtx->streams[audioIndex];
    pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    pCodecCtx = avcodec_alloc_context3(pCodec);
    avcodec_parameters_to_context(pCodecCtx, pStream->codecpar);
    avcodec_open2(pCodecCtx, pCodec, nullptr);
    LOGE(Decode_TAG, "%s:打开成功", __func__);

    AVPacket *packet = av_packet_alloc();
    AVFrame *pFrame = av_frame_alloc();

    if (av_read_frame(pFmtCtx, packet) == 0) {
        rst = avcodec_send_packet(pCodecCtx, packet);
        if (rst == 0) {
            avcodec_receive_frame(pCodecCtx, pFrame);
        } else {
            showErr(rst);
        }
    } else{
        LOGE(Decode_TAG,"%s:err",__func__);
    }
    LOGE(Decode_TAG,"%s:解码完成",__func__);
}


void Decode::showErr(int errorCode) {
    LOGE(Decode_TAG, "%s:errorCode=%d,errMsg=%s", __func__, errorCode, av_err2str(errorCode));
}

Decode::~Decode() {
    avformat_network_deinit();
    avformat_free_context(pFmtCtx);
}

