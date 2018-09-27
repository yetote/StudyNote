//
// Created by ether on 2018/9/26.
//

#include "EGLUtil.h"
#include <android/log.h>

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);

void EGLUtil::createContext() {
    LOGE("执行构造方法");
    display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        LOGE("获取egldisplay失败,error code:%d", eglGetError());
        return;
    }
    if (eglInitialize(display, NULL, NULL) != EGL_TRUE) {
        LOGE("初始化啊egldisplay失败,error code:%d", eglGetError());
        return;
    }
    EGLint *configNum = new EGLint[1];
    int configAttr[] = {
            EGL_BUFFER_SIZE, 32,
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };
    EGLConfig *eglConfig = new EGLConfig[1];
    if (eglChooseConfig(display, configAttr, eglConfig, sizeof(configNum) / sizeof(configNum[0]),
                        configNum) != EGL_TRUE) {
        LOGE("配置EGL属性失败,error code:%d", eglGetError());
        return;
    }
    config = eglConfig[0];
    delete[] eglConfig;
    EGLint contextAttr[] = {
            EGL_CONTEXT_CLIENT_VERSION,
            2,
            EGL_NONE
    };
    context = eglCreateContext(display, config, EGL_NO_CONTEXT, contextAttr);
    if (context == EGL_NO_CONTEXT) {
        LOGE("配置EGL环境失败,error code:%d", eglGetError());
        return;
    }
    LOGE("配置EGL环境成功,hey man!!!")
}

void EGLUtil::destroyContext() {
    eglDestroyContext(display, context);
    context = EGL_NO_CONTEXT;
    display = EGL_NO_DISPLAY;
}

EGLUtil::EGLUtil() {
//    createContext();
    LOGE("执行构造方法造方法~~~");
}

EGLUtil::~EGLUtil() {
//    destroyContext();
    LOGE("执行了析构方法");
}