#include <jni.h>
#include <string>
#include <memory>
#include "Decode.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>

#define NATIVE_TAG "native-lib"
#define LOGE(LOG_TAG, ...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
Decode *decode;
ANativeWindow *pWindow;
extern "C"
JNIEXPORT void JNICALL
Java_com_yetote_nativemediademo_MainActivity_init(JNIEnv *env, jobject thiz, jstring _path,
                                                  jobject _surface) {
    auto path = env->GetStringUTFChars(_path, JNI_FALSE);
    if (!decode) {
        decode = new Decode();
    }
    pWindow = ANativeWindow_fromSurface(env, _surface);
    decode->init(path, pWindow);
    env->ReleaseStringUTFChars(_path, path);
}extern "C"
JNIEXPORT void JNICALL
Java_com_yetote_nativemediademo_MainActivity_play(JNIEnv *env, jobject thiz) {
    // TODO: implement play()
    if (decode) {
        decode->play();
    }
}extern "C"
JNIEXPORT void JNICALL
Java_com_yetote_nativemediademo_MainActivity_destroy(JNIEnv *env, jobject thiz) {

    if (decode) {
        delete decode;
        decode = nullptr;
    }
    ANativeWindow_release(pWindow);
}