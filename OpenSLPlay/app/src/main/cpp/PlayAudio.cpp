//
// Created by ether on 2018/10/15.
//


#include <cstddef>
#include "PlayAudio.h"
#include <android/log.h>
#include <cstdlib>

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"openSLPlay",FORMAT,##__VA_ARGS__)
#define false SL_BOOLEAN_FALSE
#define true SL_BOOLEAN_TRUE
#define success SL_RESULT_SUCCESS
#define null NULL
FILE *pcmFile;
uint8_t *outBuffer;

size_t getPCMData(FILE *file, uint8_t *outBuffer) {
    while (!feof(file)) {
        size_t size = fread(outBuffer, 1, 44100 * 2 * 2, file);
        LOGE("读取了%ld数据", size);
        return size;
    }
    return 0;
}

void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bf, void *context) {
    size_t size = getPCMData(pcmFile, outBuffer);
    if (size == 0) {
        LOGE("读取失败");
        return;
    }
    if (outBuffer != null && size > 0) {
//        SLresult result=(*bf)->Enqueue(bf,outBuffer,size);
        SLresult result = (*bf)->Enqueue(bf, outBuffer, size);
    }
}

void PlayAudio::prepare() {
//    reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
    SLresult result;
    result = slCreateEngine(&engineObj, 0, null, 0, null, null);
    if (result != success) {
        LOGE("创建引擎对象失败");
        return;
    }
    result = (*engineObj)->Realize(engineObj, false);
    if (result != success) {
        LOGE("引擎对象实现失败");
        return;
    }
    result = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engineEngine);
    if (result != success) {
        LOGE("获取引擎接口失败");
        return;
    }
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {false};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObj, 1, ids, req);
    if (result != success) {
        LOGE("创建混音器失败");
        return;
    }
    result = (*outputMixObj)->Realize(outputMixObj, SL_BOOLEAN_FALSE);
    if (result != success) {
        LOGE("混音器实现失败");
        return;
    }
    result = (*outputMixObj)->GetInterface(outputMixObj, SL_IID_ENVIRONMENTALREVERB,
                                           &environmentalReverb);
    if (result != success) {
        LOGE("获取环境混音器接口失败,请检查MODIFY_AUDIO_SETTINGS权限是否开启");
        return;
    }
//    else {
//        result = (*environmentalReverb)->SetEnvironmentalReverbProperties(environmentalReverb,
//                                                                          &reverbSettings);
//        if (result != success) {
//            LOGE("设置环境混响音效失败");
//        }
//    }


    outBuffer = static_cast<uint8_t *>(malloc(44100 * 2 * 2));
    SLDataLocator_AndroidSimpleBufferQueue bufferQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                          2};
    SLDataFormat_PCM fmt = {SL_DATAFORMAT_PCM,
                            2,
                            SL_SAMPLINGRATE_44_1,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&bufferQueue, &fmt};
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObj};
    SLDataSink audioSink = {&outputMix, null};
    const SLInterfaceID mids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean mreq[1] = {true};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine,
                                                &bqPlayerObj,
                                                &audioSrc,
                                                &audioSink,
                                                1,
                                                mids,
                                                mreq);
    if (result != success) {
        LOGE("创建播放器失败");
        return;
    }
    result = (*bqPlayerObj)->Realize(bqPlayerObj, false);
    if (result != success) {
        LOGE("播放器实现失败");
        return;
    }
    result = (*bqPlayerObj)->GetInterface(bqPlayerObj, SL_IID_PLAY, &bqPlayer);
    if (result != success) {
        LOGE("播放器接口获取失败");
        return;
    }
    result = (*bqPlayerObj)->GetInterface(bqPlayerObj, SL_IID_BUFFERQUEUE, &bufferQueueObj);
    if (result != success) {
        LOGE("队列接口获取失败");
        return;
    }
    result = (*bufferQueueObj)->RegisterCallback(bufferQueueObj, bqPlayerCallback, this);
    if (result != success) {
        LOGE("回调接口失败");
        return;
    }
    bqPlayerEffectSend = null;
//    result=
    result = (*bqPlayerObj)->GetInterface(bqPlayerObj, SL_IID_VOLUME, &bqPlayerVolume);
    if (result != success) {
        LOGE("音量接口获取失败");
        return;
    }
}

void PlayAudio::setDataSource(const char *pcmPath) {
    pcmFile = fopen(pcmPath, "r");
    if (pcmFile == null) {
        LOGE("打开文件失败");
        return;
    }
}


void PlayAudio::start() {
    SLresult result = (*bqPlayer)->SetPlayState(bqPlayer, SL_PLAYSTATE_PLAYING);
    if (result != success) {
        LOGE("设置播放状态失败");
        return;
    }
    bqPlayerCallback(bufferQueueObj, this);
}

void PlayAudio::play(const char *pcmPath) {
    setDataSource(pcmPath);
    prepare();
    start();
}



