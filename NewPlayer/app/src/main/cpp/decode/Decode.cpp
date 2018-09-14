//
// Created by ether on 2018/9/12.
//

#include "Decode.h"
#include "../includes/libavcodec/avcodec.h"
#include "../includes/libavutil/avutil.h"
#include "../includes/libavutil/imgutils.h"
#include <android/log.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wuhuannan",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);
#define MAX_AUDIO_FRAME_SIZE 44100*4

Decode::Decode(AVMediaType type) {
    streamType = type;
}

int Decode::init(char *inputPath) {

    //初始化
    av_register_all();
    pFmtCtx = avformat_alloc_context();
    //打开文件
    if (avformat_open_input(&pFmtCtx, inputPath, NULL, NULL) != 0) {
        LOGE("无法打开输入文件");
        return -1;
    }
    //获取输入文件信息
    if (avformat_find_stream_info(pFmtCtx, NULL) < 0) {
        LOGE("无法获取输入文件信息");
        return -1;
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
        return -1;
    }
    //获取解码器
    pStream = pFmtCtx->streams[streamIdx];
    pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    if (pCodec == NULL) {
        LOGE("找不到解码器");
        return -1;
    }
    //打开解码器
    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("无法打开解码器");
        return -1;
    }
    LOGE("ffmpeg初始化完成")
    return streamIdx;
}

void Decode::startDecode(int streamIdx) {
    //为数据包分配内存
    pPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    //为解码帧分配内存
    pFrame = av_frame_alloc();
    //为解码完成后的帧分配内存
    pDecodeFrame = av_frame_alloc();
    switch (streamType) {
        case AVMEDIA_TYPE_VIDEO :
            videoDecode(streamIdx);
            break;
        case AVMEDIA_TYPE_AUDIO:
            audioDecode(streamIdx);
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

void Decode::audioDecode(int audioIdx) {
    //声明重采样结构体
    SwrContext *pSwrCtx = swr_alloc();
    //声名输入输出采样格式
    enum AVSampleFormat inSampleFmt = pCodecCtx->sample_fmt;
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_S16P;
    //声明输入输入采样率
    int inSampleRate = pCodecCtx->sample_rate;
    int outSampleRate = 44100;
    //声明输入输出声道数
    int inSampleChannel = pCodecCtx->channels;
    int outSampleChannel = AV_CH_LAYOUT_STEREO;
    //将音频数据已添加至重采样结构体,参数说明见源码
    swr_alloc_set_opts(pSwrCtx,
                       outSampleChannel,
                       outSampleFmt,
                       outSampleRate,
                       inSampleChannel,
                       inSampleFmt,
                       inSampleRate,
                       0,
                       NULL);
    //初始化重采样结构体
    swr_init(pSwrCtx);
    //获取输出声道数量
    int outChannelNum = av_get_channel_layout_nb_channels(static_cast<uint64_t>(outSampleChannel));
    //接下来代码和解码视频一致了
    int ret = -1;
    int outDataLineSize;
    uint8_t **outBuffer;
    AVFrame *pDecodeFrame;
    pDecodeFrame = av_frame_alloc();
    while (av_read_frame(pFmtCtx, pPacket) >= 0) {
        if (pPacket->stream_index == audioIdx) {
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                ret = avcodec_receive_frame(pCodecCtx, pDecodeFrame);
                LOGE("%d", ret);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    break;
                } else if (ret == AVERROR_EOF) {
                    LOGE("%s", "解码完成");
                    break;
                } else if (ret < 0) {
                    LOGE("%s", "解码出错");
                    break;
                }
                //为重采样缓冲区分配空间并装填数据，参数见源码
                av_samples_alloc_array_and_samples(&outBuffer,
                                                   &outDataLineSize,
                                                   MAX_AUDIO_FRAME_SIZE,
                                                   outChannelNum,
                                                   outSampleFmt,
                                                   0);
                //重采样，参数见源码
                swr_convert(pSwrCtx,
                            outBuffer,
                            MAX_AUDIO_FRAME_SIZE,
                            (const uint8_t **) (pDecodeFrame->data),
                            pDecodeFrame->nb_samples);
                //获取重采样数据的代销
                int outBufferSize = av_samples_get_buffer_size(&outDataLineSize, outChannelNum,
                                                               pDecodeFrame->nb_samples,
                                                               outSampleFmt, 1);
                //todo 声道重采样数据 大小为总数/声道数
                outBuffer[0];
                outBuffer[1];

            }
        }

    }
    swr_free(&pSwrCtx);
}

void Decode::videoDecode(int videoIdx) {
    //计算一帧yuv420p数据所需要的大小
    u_int8_t *outBuffer = static_cast<u_int8_t *>(av_malloc(
            static_cast<size_t>(av_image_get_buffer_size(AV_PIX_FMT_YUV420P, pCodecCtx->width,
                                                         pCodecCtx->height, 1))));
    //向缓冲区装填数据，参数含义写在ffmpeg源码的方法内
    av_image_fill_arrays(pFrame->data, pFrame->linesize, outBuffer, AV_PIX_FMT_YUV420P,
                         pCodecCtx->width, pCodecCtx->height, 1);
    //声明一个结构体用来存储转码的参数，参数说明在源码中
    struct SwsContext *pSwsCtx = sws_getContext(pCodecCtx->width,
                                                pCodecCtx->height,
                                                pCodecCtx->pix_fmt,
                                                pCodecCtx->width,
                                                pCodecCtx->height,
                                                AV_PIX_FMT_YUV420P,
                                                SWS_BICUBIC, NULL, NULL, NULL
    );
    int ret;
    while (av_read_frame(pFmtCtx, pPacket)) {
        if (pPacket->stream_index == videoIdx) {
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                ret = avcodec_receive_frame(pCodecCtx, pDecodeFrame);
                LOGE("%d", ret);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    break;
                } else if (ret == AVERROR_EOF) {
                    LOGE("%s", "解码完成");
                    break;
                } else if (ret < 0) {
                    LOGE("%s", "解码出错");
                    break;
                }
                //执行视频格式转换,参数说明见源码
                sws_scale(pSwsCtx, pDecodeFrame->data,
                          pDecodeFrame->linesize,
                          0,
                          pCodecCtx->height,
                          pDecodeFrame->data,
                          pDecodeFrame->linesize);
                //输出YUV数据
                //data解码后的图像像素数据（音频采样数据）
                //Y 亮度 UV 色度（压缩了） 人对亮度更加敏感
                //U V 个数是Y的1/4
                int y_size = pCodecCtx->width * pCodecCtx->height;
                //todo yuv数据 大小为y_size,y_size/4,y_size/4
                pDecodeFrame->data[0];
                pDecodeFrame->data[1];
                pDecodeFrame->data[2];

            }
        }
    }
    sws_freeContext(pSwsCtx);
}

Decode::~Decode() {
    av_packet_unref(pPacket);
    av_frame_free(&pFrame);
    av_frame_free(&pDecodeFrame);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFmtCtx);
}