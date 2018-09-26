#include <jni.h>
#include "../cpp/EGLUtil.h"
#include <android/native_window_jni.h>
#include <android/native_window.h>

EGLUtil *eglUtil;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_configEGLContext(JNIEnv *env, jobject instance) {
    eglUtil->createContext();

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_destroyEGLContext(JNIEnv *env, jobject instance) {
    eglUtil->destroyContext();

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkplayer_PlayerView_draw(JNIEnv *env, jobject instance, jobject surface) {
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    EGLint surfaceAttr[] = {EGL_NONE};
    EGLSurface eglSurface = eglCreateWindowSurface(eglUtil->display, eglUtil->config, window,
                                                   surfaceAttr);
    eglMakeCurrent(eglUtil->display, eglSurface, eglSurface, eglUtil->context);
    //todo draw

    eglDestroySurface(eglUtil->display, eglSurface);

}