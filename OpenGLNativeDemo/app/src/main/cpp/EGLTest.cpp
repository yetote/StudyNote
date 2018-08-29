//
// Created by ether on 2018/8/28.
//

#include "../jni/com_example_ether_openglnativedemo_EGLCRenderer.h"
#include "Triangle.h"
#include "EGLUtil.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>

EGLUtil EGLUtil;
Triangle triangle;
EGLDisplay eglDisplay;
EGLContext eglContext;
EGLConfig eglConfig;

JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_EGLCRenderer_createEGL
        (JNIEnv *, jobject) {
    eglDisplay = EGLUtil.initDisplay();
    eglConfig = EGLUtil.addConfig(eglDisplay);
    eglContext = EGLUtil.createContext(eglDisplay, eglConfig);
}


JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_EGLCRenderer_destroyEGL
        (JNIEnv *, jobject) {
    EGLUtil.destroyEGL(eglDisplay, eglContext);
}

JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_EGLCRenderer_draw
        (JNIEnv *env, jobject obj, jobject surface, jint width, jint height, jstring vertexCode,
         jstring fragmentCode) {
    int *surfaceAttributes = new int[1]{EGL_NONE};
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window,
                                                   surfaceAttributes);
    eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    const char *vertexShaderCode = env->GetStringUTFChars(vertexCode, NULL);
    const char *fragmentShaderCode = env->GetStringUTFChars(fragmentCode, NULL);
    triangle.initGL(vertexShaderCode, fragmentShaderCode);
    env->ReleaseStringUTFChars(vertexCode, vertexShaderCode);
    env->ReleaseStringUTFChars(fragmentCode, fragmentShaderCode);
    glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    glDisable(GL_DEPTH_TEST);
    triangle.draw();
    eglSwapBuffers(eglDisplay, eglSurface);
    eglDestroySurface(eglDisplay, eglSurface);
}