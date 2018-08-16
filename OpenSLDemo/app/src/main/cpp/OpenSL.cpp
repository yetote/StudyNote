//
// Created by ether on 2018/8/14.
//

#include "../jni/OpenSL.h"
#include <SLES/OpenSLES_Android.h>
#include <SLES/OpenSLES.h>
#include <android/log.h>
#include <stdio.h>

#define LOG_TAG "OpenSL.cpp"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__);
OpenSL openSL;
SLObjectItf engineObject;
SLresult result;
SLEngineItf engineEngine;


SLEngineItf OpenSL::createEngine() {
    SLresult result;
    SLEngineItf engine;
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engine);
    return engine;
}

void OpenSL::pcmCall(SLAndroidSimpleBufferQueueItf bf, void *context, const char *pcmPath) {
    LOGE("pcmCall");
    FILE *fp = NULL;
    char *buf = NULL;
    if (!buf) {
        buf = new char[1024 * 1024];
    }
    if (!fp) {
        fp = fopen(pcmPath, "rb");
    }
    if (!fp) return;
    if (feof(fp) == 0) {
        int len = fread(buf, 1, 1024, fp);
        if (bf > 0) {
            (*bf)->Enqueue(bf, buf, len);
        }
    }

}

OpenSL::~OpenSL() {

}