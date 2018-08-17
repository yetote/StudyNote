//
// Created by ether on 2018/8/14.
//

#include <jni.h>
#include <SLES/OpenSLES.h>
#include "../jni/com_example_ether_opensldemo_OpenSLTest.h"
#include "../jni/OpenSL.h"
#include <android/log.h>
#include <stdio.h>

#define LOG_TAG "OpenSLTest.cpp"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
SLObjectItf engineObject;
SLresult result;
SLEngineItf engineEngine;
const char *pcmPath;

SLEngineItf createEngine() {
    SLresult result;
    SLEngineItf engine;
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engine);
    return engine;
}

void pcmCall(SLAndroidSimpleBufferQueueItf bf, void *context) {
    LOGE("pcmCall");
    FILE *fp = NULL;
    char *buf = NULL;
    if (!buf) {
        buf = new char[44100 * 2 * 2];
    }
    if (!fp) {
        fp = fopen(pcmPath, "rb");
    }
    if (!fp) return;
    if (feof(fp) == 0) {
        int len = fread(buf, 44100 * 2 * 2, 1, fp);
        if (len > 0) {
            (*bf)->Enqueue(bf, buf, len);
        }
    }

}

JNIEXPORT jint JNICALL Java_com_example_ether_opensldemo_OpenSLTest_create
        (JNIEnv *, jobject) {

}

JNIEXPORT void JNICALL Java_com_example_ether_opensldemo_OpenSLTest_play
        (JNIEnv *env, jobject, jstring pcmPathParam) {
    SLEngineItf engine = createEngine();
    pcmPath = env->GetStringUTFChars(pcmPathParam, NULL);
    if (engine == NULL) LOGE("创建引擎失败");
    SLObjectItf mix = NULL;
    SLresult result = 0;
    result = (*engine)->CreateOutputMix(engine, &mix, 0, 0, 0);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("创建混音器失败");
    }
    result = (*mix)->Realize(mix, SL_BOOLEAN_FALSE);
    SLDataLocator_OutputMix outMix = {SL_DATALOCATOR_OUTPUTMIX, mix};
    SLDataSink audioSink = {&outMix, 0};

    SLDataLocator_AndroidSimpleBufferQueue que = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 10};
    SLDataFormat_PCM pcmInfo = {
            SL_DATAFORMAT_PCM,
            2,
            SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN
    };
    SLDataSource source = {&que, &pcmInfo};

    SLObjectItf player = NULL;
    SLPlayItf playItf = NULL;
    SLAndroidSimpleBufferQueueItf pcmQueue = NULL;
    const SLInterfaceID ids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engine)->CreateAudioPlayer(engine, &player, &source, &audioSink,
                                          sizeof(ids) / sizeof(SLInterfaceID), ids, req);
    if (result != SL_RESULT_SUCCESS) LOGE("创建播放器失败");
    result = (*player)->Realize(player, SL_BOOLEAN_FALSE);
    result = (*player)->GetInterface(player, SL_IID_PLAY, &playItf);
    result = (*player)->GetInterface(player, SL_IID_BUFFERQUEUE, &pcmQueue);
    (*pcmQueue)->RegisterCallback(pcmQueue, pcmCall, 0);
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    (*pcmQueue)->Enqueue(pcmQueue, "", 1);
}


