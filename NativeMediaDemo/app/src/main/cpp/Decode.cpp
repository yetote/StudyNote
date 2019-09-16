//
// Created by ether on 2019/9/12.
//

#include <unistd.h>
#include "Decode.h"

using namespace std;

int64_t systemnanotime() {
    timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return now.tv_sec * 1000000000LL + now.tv_nsec;
}

Decode::Decode() {
    audioPtr = make_shared<MediaInfo>(MediaInfo::MEDIA_TYPE_AUDIO);
    videoPtr = make_shared<MediaInfo>(MediaInfo::MEDIA_TYPE_VIDEO);
    audioPlay = make_shared<AudioPlay>();
    LOGE(Decode_TAG, "%s:audio=%d", __func__, audioPtr->type);
    LOGE(Decode_TAG, "%s:video=%d", __func__, videoPtr->type);
}

bool Decode::init(std::string path, ANativeWindow *window) {

    auto mediaExtractor = AMediaExtractor_new();
    if (!mediaExtractor) {
        LOGE(Decode_TAG, "%s:创建mediaextractor失败", __func__);
        return false;
    }
    auto rst = AMediaExtractor_setDataSource(mediaExtractor, path.c_str());
    if (rst != AMEDIA_OK) {
        LOGE(Decode_TAG, "%s:获取资源失败", __func__);
        return false;
    }
    auto numTracks = AMediaExtractor_getTrackCount(mediaExtractor);
    if (numTracks <= 0) {
        LOGE(Decode_TAG, "%s:获取的通道数不正确%d", __func__, numTracks);
        return false;
    }
    for (auto i = 0; i < numTracks; ++i) {
        auto pFmt = AMediaExtractor_getTrackFormat(mediaExtractor, i);
        auto fmtStr = AMediaFormat_toString(pFmt);
        LOGE(Decode_TAG, "%s:%s", __func__, fmtStr);
        const char *mime;
        if (!AMediaFormat_getString(pFmt, AMEDIAFORMAT_KEY_MIME, &mime)) {
            LOGE(Decode_TAG, "%s:未找到对应格式", __func__);
            return false;
        } else if (!strncmp(mime, "video/", 6)) {

            videoPtr->extractor = AMediaExtractor_new();
            AMediaExtractor_setDataSource(videoPtr->extractor, path.c_str());
            AMediaExtractor_selectTrack(videoPtr->extractor, i);
            videoPtr->codec = AMediaCodec_createDecoderByType(mime);
            auto rst = AMediaCodec_configure(videoPtr->codec, pFmt, window, nullptr, 0);
            if (rst != AMEDIA_OK) {
                LOGE(Decode_TAG, "%s:配置解码器失败", __func__);
                AMediaFormat_delete(pFmt);
                return false;
            }
            videoPtr->isSuccess = true;
        } else if (!strncmp(mime, "audio/", 6)) {
            audioPtr->extractor = AMediaExtractor_new();
            AMediaExtractor_setDataSource(audioPtr->extractor, path.c_str());
            AMediaExtractor_selectTrack(audioPtr->extractor, i);
            audioPtr->codec = AMediaCodec_createDecoderByType(mime);
            auto rst = AMediaCodec_configure(audioPtr->codec, pFmt, nullptr, nullptr, 0);
            if (rst != AMEDIA_OK) {
                LOGE(Decode_TAG, "%s:配置解码器失败", __func__);
                AMediaFormat_delete(pFmt);
                return false;
            }
            int32_t sampleRate;
            AMediaFormat_getInt32(pFmt, "sample-rate", &sampleRate);
            int32_t channelCount;
            AMediaFormat_getInt32(pFmt, "channel-count", &channelCount);
            audioPlay->init(sampleRate, channelCount);
            audioPtr->isSuccess = true;

        }
        AMediaFormat_delete(pFmt);

    }
    return true;
}

void Decode::doDecode(std::shared_ptr<MediaInfo> sp) {
    if (!sp->isSuccess) {
        LOGE(Decode_TAG, "%s:解码器未成功初始化", __func__);
        return;
    }
    int count = 0;
    while (true) {
        if (!sp->isInputEof) {
            auto index = AMediaCodec_dequeueInputBuffer(sp->codec, 2000);
            if (index > 0) {
//                LOGE(Decode_TAG, "%s:获取输入缓冲区失败%d", __func__, index);
//                continue;
                size_t size;
                auto inputBuffer = AMediaCodec_getInputBuffer(sp->codec, index, &size);
                auto readSize = AMediaExtractor_readSampleData(sp->extractor, inputBuffer, size);
                if (readSize < 0) {
                    readSize = 0;
                    sp->isInputEof = true;
                    LOGE(Decode_TAG, "%s:全部数据读取完毕", __func__);
                }
                auto presentationTimeUs = AMediaExtractor_getSampleTime(sp->extractor);
                AMediaCodec_queueInputBuffer(sp->codec, index, 0, readSize, presentationTimeUs,
                                             sp->isInputEof ? AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM
                                                            : 0);
                AMediaExtractor_advance(sp->extractor);
                LOGE(Decode_TAG, "%s:数组放入缓冲区", __func__);
            } else {
                LOGE(Decode_TAG, "%s:获取输入缓冲区失败%d", __func__, index);
            }
        }
        if (!sp->isOutputEof) {
            AMediaCodecBufferInfo info;
            auto index = AMediaCodec_dequeueOutputBuffer(sp->codec, &info, 0);
            if (index < 0) {
                continue;
            }
            if (info.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
                LOGE(Decode_TAG, "%s:全部数据解码完成", __func__);
                sp->isOutputEof = true;
            }

            if (sp->type == MediaInfo::MEDIA_TYPE_AUDIO) {
                auto readSize = info.size;
                size_t bufSize;
                uint8_t *buffer = AMediaCodec_getOutputBuffer(sp->codec, index, &bufSize);
                if (bufSize < 0) {
                    LOGE(Decode_TAG, "%s:未读出解码数据%d", __func__, bufSize);
//                    mutex.unlock();
                    continue;
                }
                uint8_t *data = new uint8_t[bufSize];
                memcpy(data, buffer + info.offset, info.size);
//                fwrite(data, info.size, 1, file);
                LOGE(Decode_TAG, "%s:size=%d", __func__, info.size);
                while (!audioPlay->canPush(info.size)) {
                    usleep(300000);
                    LOGE(Decode_TAG, "%s:休眠", __func__);
                }
                LOGE(Decode_TAG, "%s:开始填充数据", __func__);
                audioPlay->pushData(data, info.size);
                LOGE(Decode_TAG, "%s:数据填充完成", __func__);
                delete[] data;

            } else {
                int64_t presentationNano = info.presentationTimeUs * 1000;
                if (sp->renderStart < 0) {
                    sp->renderStart = systemnanotime() - presentationNano;
                }
                int64_t delay = (sp->renderStart + presentationNano) - systemnanotime();
                if (delay > 0) {
                    usleep(delay / 1000);
                }
            }
            AMediaCodec_releaseOutputBuffer(sp->codec, index, info.size != 0);
            count++;
            LOGE(Decode_TAG, "%s:解码了%d帧", __func__, count);
        }

        if (sp->isOutputEof && sp->isInputEof) {
            LOGE(Decode_TAG, "%s:解码完成", __func__);
            break;
        }
    }
    LOGE(Decode_TAG, "%s:退出解码", __func__);
}

void Decode::play() {

    playAudio();
    playVideo();
}

Decode::~Decode() {
    LOGE(Decode_TAG, "%s:销毁", __func__);

//    if (mediaCodec != nullptr) {
//        AMediaCodec_stop(mediaCodec);
//        AMediaCodec_delete(mediaCodec);
//        mediaCodec = nullptr;
//    }
//
//    if (mediaExtractor != nullptr) {
//        AMediaExtractor_delete(mediaExtractor);
//        mediaExtractor = nullptr;
//    }
}

void Decode::playAudio() {
    AMediaCodec_start(audioPtr->codec);
    std::thread decodeThread(&Decode::doDecode, this, audioPtr);
    decodeThread.detach();
    audioPlay->play();
}

void Decode::playVideo() {
    AMediaCodec_start(videoPtr->codec);
    std::thread decodeThread(&Decode::doDecode, this, videoPtr);
    decodeThread.detach();
}
