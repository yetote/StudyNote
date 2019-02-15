#include <jni.h>
#include <string>
#include "video/PlayerView.h"
#include <thread>
#include "decode/Decode.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>

using namespace std;
PlayerView playerView;
Decode decode;
BlockQueue<AVFrame *> blockQueue;


extern "C"
JNIEXPORT void JNICALL
Java_com_example_bamboo_util_PlayerView_play(JNIEnv *env, jobject instance, jstring path_,
                                             jstring vertexCode_, jstring fragCode_,
                                             jobject surface, jint w, jint h) {
    const char *path = env->GetStringUTFChars(path_, 0);
    const char *vertexCode = env->GetStringUTFChars(vertexCode_, 0);
    const char *fragCode = env->GetStringUTFChars(fragCode_, 0);

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);

    std::thread decodeThread(&Decode::decode, decode, path, DECODE_VIDEO, std::ref(blockQueue));
    std::thread playThread(&PlayerView::play, playerView, std::ref(blockQueue), vertexCode,
                           fragCode, window, w, h);

    decodeThread.join();
    blockQueue.stop();
    playThread.join();

    env->ReleaseStringUTFChars(path_, path);
    env->ReleaseStringUTFChars(vertexCode_, vertexCode);
    env->ReleaseStringUTFChars(fragCode_, fragCode);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bamboo_util_PlayerView_configEGLContext(JNIEnv *env, jobject instance) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bamboo_util_PlayerView_destroyEGLContext(JNIEnv *env, jobject instance) {

    // TODO

}