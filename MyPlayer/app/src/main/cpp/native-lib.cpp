#include <jni.h>
#include <string>
#include "util/CommonTools.h"

#define LOG_TAG "native-lib"
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_ether_myplayer_PlayerController_prepare(JNIEnv *env, jobject instance,
                                                         jstring srcFilenameParam_,
                                                         jintArray maxAnalyzeDurations_, jint size,
                                                         jint probesize,
                                                         jboolean fpsProbesizeConfigured,
                                                         jfloat minBufferedDuration,
                                                         jfloat maxBufferedDuration, jint width,
                                                         jint height, jobject surface) {
    jint *maxAnalyzeDurations = env->GetIntArrayElements(maxAnalyzeDurations_, NULL);

    LOGE("初始化开始");
    //初始化一个java虚拟机对象
    JavaVM *g_jvm = NULL;
    //获取java虚拟机
    env->GetJavaVM(&g_jvm);
    //创建一个全局变量
    jobject g_ogj = env->NewGlobalRef(instance);
    const char *videoMergeFilePath = env->GetStringUTFChars(srcFilenameParam_, 0);


    env->ReleaseStringUTFChars(srcFilenameParam_, videoMergeFilePath);
    env->ReleaseIntArrayElements(maxAnalyzeDurations_, maxAnalyzeDurations, 0);
}


extern "C" JNIEXPORT jstring
JNICALL
Java_com_example_ether_myplayer_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_myplayer_PlayerController_onPause(JNIEnv *env, jobject instance) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_myplayer_PlayerController_onSurfaceCreated(JNIEnv *env, jobject instance,
                                                                  jobject surface) {


}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_myplayer_PlayerController_resetRenderSize(JNIEnv *env, jobject instance,
                                                                 jint left, jint top, jint width,
                                                                 jint height) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_myplayer_PlayerController_play(JNIEnv *env, jobject instance) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_myplayer_PlayerController_stop(JNIEnv *env, jobject instance) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_myplayer_PlayerController_onSurfaceDestroyed(JNIEnv *env, jobject instance,
                                                                    jobject surface) {

    // TODO

}