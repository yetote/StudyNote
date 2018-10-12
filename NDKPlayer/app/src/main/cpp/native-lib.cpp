#include <jni.h>
#include "../cpp/EGLUtil.h"
#include "DecodeVideo.h"
#include "Triangle.h"
#include "../jni/com_example_ether_ndkplayer_EGLCRenderer.h"
#include "PlayerView.h"
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <thread>
#include <android/log.h>

#define LOG_TAG "native-lib"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

EGLUtil EGLUtil;
EGLDisplay eglDisplay;
EGLContext eglContext;
EGLConfig eglConfig;
DecodeVideo decodeVideo;
BlockQueue<AVFrame*> blockQueue;
PlayerView playerView;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_configEGLContext(JNIEnv *env, jobject instance) {

    eglDisplay = EGLUtil.initDisplay();
    eglConfig = EGLUtil.addConfig(eglDisplay);
    eglContext = EGLUtil.createContext(eglDisplay, eglConfig);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_destroyEGLContext(JNIEnv *env, jobject instance) {

    EGLUtil.destroyEGL(eglDisplay, eglContext);

}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ether_ndkplayer_PlayerView_draw(JNIEnv *env, jobject instance, jstring videoPath_,
                                                 jstring vertexShaderCode_, jstring fragShaderCode_,
                                                 jobject surface, jint w, jint h) {
    const char *videoPath = env->GetStringUTFChars(videoPath_, 0);
    const char *vertexShaderCode = env->GetStringUTFChars(vertexShaderCode_, 0);
    const char *fragShaderCode = env->GetStringUTFChars(fragShaderCode_, 0);

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);

    std::thread decodeThread(&DecodeVideo::decode, decodeVideo, videoPath, std::ref(blockQueue));
    std::thread playThread(&PlayerView::play, playerView, window, std::ref(blockQueue), vertexShaderCode,
                           fragShaderCode, w, h);
    decodeThread.join();
    LOGE("解码完成");
    blockQueue.stop();
    LOGE("队列停止");
    playThread.join();
    LOGE("播放完成");
    env->ReleaseStringUTFChars(videoPath_, videoPath);
    env->ReleaseStringUTFChars(vertexShaderCode_, vertexShaderCode);
    env->ReleaseStringUTFChars(fragShaderCode_, fragShaderCode);
//    std::thread produceThread(produceTask);
//    std::thread consumeThread(consumeTask);
//    produceThread.join();
//    LOGE("生产者停止生产");
//    blockQueue.stop();
//    LOGE("队列接受停止通知");
//    consumeThread.join();
//    LOGE("消费者停止消费");
    return 0;
}
