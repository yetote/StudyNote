#include <jni.h>
#include <string>
#include "DecodeAudio.h"
#include <thread>
#include <android/log.h>

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"native-lib",FORMAT,##__VA_ARGS__)

DecodeAudio decodeAudio;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_openslplay_AudioPlayer_play(JNIEnv *env, jclass type, jstring audioPath_,
                                                   jstring outPath_) {
    const char *audioPath = env->GetStringUTFChars(audioPath_, 0);
    const char *outPath = env->GetStringUTFChars(outPath_, 0);

    std::thread decodeThread(&DecodeAudio::decode, decodeAudio, audioPath, outPath);
    decodeThread.join();
    LOGE("解码完成");
    env->ReleaseStringUTFChars(audioPath_, audioPath);
    env->ReleaseStringUTFChars(outPath_, outPath);
}