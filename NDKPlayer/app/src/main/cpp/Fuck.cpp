//
// Created by ether on 2018/9/28.
//

#include "../jni/com_example_ether_ndkplayer_EGLCRenderer.h"
#include "Triangle.h"
#include "EGLUtil.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>

EGLUtil EGLUtil;
Triangle triangle;
EGLDisplay eglDisplay;
EGLContext eglContext;
EGLConfig eglConfig;

JNIEXPORT void JNICALL Java_com_example_ether_ndkplayer_EGLCRenderer_configEGLContext
        (JNIEnv *, jobject) {
    eglDisplay = EGLUtil.initDisplay();
    eglConfig = EGLUtil.addConfig(eglDisplay);
    eglContext = EGLUtil.createContext(eglDisplay, eglConfig);
}


JNIEXPORT void JNICALL Java_com_example_ether_ndkplayer_EGLCRenderer_destroyEGLContext
        (JNIEnv *, jobject) { EGLUtil.destroyEGL(eglDisplay, eglContext); }


JNIEXPORT jint JNICALL Java_com_example_ether_ndkplayer_EGLCRenderer_draw
        (JNIEnv *env, jobject, jstring, jstring vertexCode,
         jstring fragmentCode, jobject surface, jint width, jint height) {
    const char *vertexShaderCode = env->GetStringUTFChars(vertexCode, NULL);
    const char *fragmentShaderCode = env->GetStringUTFChars(fragmentCode, NULL);

    int *surfaceAttributes = new int[1]{EGL_NONE};
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window,
                                                   surfaceAttributes);
    eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    triangle.initGL(vertexShaderCode, fragmentShaderCode);

    glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    glDisable(GL_DEPTH_TEST);
    triangle.draw();
    eglSwapBuffers(eglDisplay, eglSurface);
    eglDestroySurface(eglDisplay, eglSurface);
    env->ReleaseStringUTFChars(fragmentCode, fragmentShaderCode);
    env->ReleaseStringUTFChars(vertexCode, vertexShaderCode);
    return 0;
}