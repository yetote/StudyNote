//
// Created by ether on 2018/10/12.
//



#include "DecodeAudio.h"


#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__)
#define MAX_AUDIO_FRAME_SIZE 44100*4
audioType audioData;

void
DecodeAudio::decode(const char *audioPath, const char *outPath, BlockQueue<audioType> &blockQueue) {
    av_register_all();
    this->pFmtCtx = avformat_alloc_context();
    if (avformat_open_input(&pFmtCtx, audioPath, NULL, NULL) != 0) {
        LOGE("打开文件失败");
        return;
    }
    if (avformat_find_stream_info(pFmtCtx, NULL) < 0) {
        LOGE("获取流信息失败");
        return;
    }
    int audioIdx = -1;
    for (int i = 0; i < pFmtCtx->nb_streams; ++i) {
        if (pFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioIdx = i;
            break;
        }
    }
    if (audioIdx == -1) {
        LOGE("未找到音频流");
        return;
    }
    pStream = pFmtCtx->streams[audioIdx];
    pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    if (pCodec == NULL) {
        LOGE("未找到对应的解码器");
        return;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    avcodec_parameters_to_context(pCodecCtx, pStream->codecpar);
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("打开解码器失败");
        return;
    }
    pPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    pFrame = av_frame_alloc();
    SwrContext *pSwrCtx = swr_alloc();
    enum AVSampleFormat inSampleFmt = pCodecCtx->sample_fmt;
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_S16;

    int inSampleRate = pCodecCtx->sample_rate;
    int outSampleRate = 44100;

    uint64_t inSampleChannel = pCodecCtx->channel_layout;
    uint64_t outSampleChannel = AV_CH_LAYOUT_STEREO;

    swr_alloc_set_opts(pSwrCtx,
                       outSampleChannel,
                       outSampleFmt,
                       outSampleRate,
                       inSampleChannel,
                       inSampleFmt,
                       inSampleRate,
                       0,
                       NULL);
    swr_init(pSwrCtx);
    int outChannelNum = av_get_channel_layout_nb_channels(outSampleChannel);
    int ret = -1;
    int dataSize;
    FILE *outFile = fopen(outPath, "wb+");
//    pDecodeFrame = av_frame_alloc();
    uint8_t *outBuffer = reinterpret_cast<uint8_t *>(static_cast<int *>(av_malloc(
            MAX_AUDIO_FRAME_SIZE)));
    while (av_read_frame(pFmtCtx, pPacket) >= 0) {
        if (pPacket->stream_index == audioIdx) {
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                ret = avcodec_receive_frame(pCodecCtx, pFrame);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    break;
                } else if (ret == AVERROR_EOF) {
                    LOGE("%s", "解码完成");
                    fclose(outFile);
                    break;
                } else if (ret < 0) {
                    LOGE("%s", "解码出错");
                    break;
                }
//                int outBufferSize = av_samples_get_buffer_size(NULL, outChannelNum, rst,
//                                                               outSampleFmt, 1);
                int outBufferSize = av_samples_get_buffer_size(pFrame->linesize,
                                                               pCodecCtx->channels,
                                                               pCodecCtx->frame_size,
                                                               pCodecCtx->sample_fmt, 1);
                int rst = swr_convert(pSwrCtx,
                                      &outBuffer,
                                      outBufferSize,
                                      (const uint8_t **) (pFrame->data),
                                      pFrame->nb_samples);
//                fwrite(outBuffer, 1, outBufferSize, outFile);
                audioData = {outBuffer, outBufferSize};
                blockQueue.push(audioData);
            }
        }
        av_packet_unref(pPacket);
    }
    fclose(outFile);
    av_frame_free(&pFrame);
    swr_free(&pSwrCtx);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFmtCtx);
}
