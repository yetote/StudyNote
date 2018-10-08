#include <jni.h>
#include "../cpp/EGLUtil.h"
#include "DecodeVideo.h"
#include "Triangle.h"
#include "../jni/com_example_ether_ndkplayer_EGLCRenderer.h"
#include <android/native_window_jni.h>
#include <android/native_window.h>

EGLUtil EGLUtil;
EGLDisplay eglDisplay;
EGLContext eglContext;
EGLConfig eglConfig;
DecodeVideo decodeVideo;


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

}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ether_ndkplayer_PlayerView_draw(JNIEnv *env, jobject instance, jstring videoPath_,
                                                 jstring vertexShaderCode_, jstring fragShaderCode_,
                                                 jobject surface, jint w, jint h) {
    const char *videoPath = env->GetStringUTFChars(videoPath_, 0);
    const char *vertexShaderCode = env->GetStringUTFChars(vertexShaderCode_, 0);
    const char *fragShaderCode = env->GetStringUTFChars(fragShaderCode_, 0);

    int *surfaceAttributes = new int[1]{EGL_NONE};
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window,
                                                   surfaceAttributes);
    decodeVideo.decode(videoPath, vertexShaderCode, fragShaderCode, eglDisplay, eglSurface,
                       eglContext, w, h);

//    eglSwapBuffers(eglDisplay, eglSurface);
    eglDestroySurface(eglDisplay, eglSurface);

    env->ReleaseStringUTFChars(videoPath_, videoPath);
    env->ReleaseStringUTFChars(vertexShaderCode_, vertexShaderCode);
    env->ReleaseStringUTFChars(fragShaderCode_, fragShaderCode);
    return 0;
}