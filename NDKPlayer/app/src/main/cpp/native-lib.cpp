#include <jni.h>
#include "../cpp/EGLUtil.h"
#include "Rect.h"
#include "DecodeVideo.h"
#include <android/native_window_jni.h>
#include <android/native_window.h>

EGLUtil *eglUtil;
Rect *rect;
DecodeVideo *decodeVideo;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_configEGLContext(JNIEnv *env, jobject instance) {
    eglUtil = new EGLUtil();
    rect = new Rect();
    decodeVideo = new DecodeVideo();
    eglUtil->createContext();

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_destroyEGLContext(JNIEnv *env, jobject instance) {
    eglUtil->destroyContext();
    delete decodeVideo;
    delete rect;
    delete eglUtil;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ether_ndkplayer_PlayerView_draw(JNIEnv *env, jobject instance, jstring videoPath_,
                                                 jstring vertexShaderCode_, jstring fragShaderCode_,
                                                 jobject surface) {
    const char *vertexShaderCode = env->GetStringUTFChars(vertexShaderCode_, 0);
    const char *fragShaderCode = env->GetStringUTFChars(fragShaderCode_, 0);
    const char *videoPath = env->GetStringUTFChars(videoPath_, 0);

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    EGLint surfaceAttr[] = {EGL_NONE};
    EGLSurface eglSurface = eglCreateWindowSurface(eglUtil->display, eglUtil->config, window,
                                                   surfaceAttr);
    eglMakeCurrent(eglUtil->display, eglSurface, eglSurface, eglUtil->context);

    rect->bindData(vertexShaderCode, fragShaderCode);
    decodeVideo->decode(videoPath, eglUtil->display, eglSurface);
//    while (frame != 0) {
//        rect->setUniform(frame);
//        rect->draw();
//        eglSwapBuffers(eglUtil->display, eglSurface);
//        frame = decodeVideo->decode(videoPath);
//    }

    eglDestroySurface(eglUtil->display, eglSurface);


    env->ReleaseStringUTFChars(videoPath_, videoPath);
    env->ReleaseStringUTFChars(vertexShaderCode_, vertexShaderCode);
    env->ReleaseStringUTFChars(fragShaderCode_, fragShaderCode);
    return -1;
}