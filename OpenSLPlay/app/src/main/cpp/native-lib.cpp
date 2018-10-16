#include <jni.h>
#include <string>
#include "DecodeAudio.h"
#include "PlayAudio.h"
#include "BlockQueue.h"
#include <thread>
#include <android/log.h>

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"native-lib",FORMAT,##__VA_ARGS__)
using namespace std;
DecodeAudio decodeAudio;
PlayAudio playAudio;

BlockQueue<audioType> blockQueue;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_openslplay_AudioPlayer_play(JNIEnv *env, jclass type, jstring audioPath_,
                                                   jstring outPath_) {
    const char *audioPath = env->GetStringUTFChars(audioPath_, 0);
    const char *outPath = env->GetStringUTFChars(outPath_, 0);

    std::thread decodeThread(&DecodeAudio::decode, decodeAudio, audioPath, outPath,
                             ref(blockQueue));

    std::thread playThread(&PlayAudio::play, playAudio,ref(blockQueue));
    decodeThread.join();
    playThread.join();
//    LOGE("播放完成");
    env->ReleaseStringUTFChars(audioPath_, audioPath);
    env->ReleaseStringUTFChars(outPath_, outPath);
}

