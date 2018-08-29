//
// Created by ether on 2018/8/28.
//

#include "EGLUtil.h"
#include "LogUtil.cpp"
#include <EGL/egl.h>
#include <assert.h>
#include <EGL/eglext.h>

#define LOG_TAG "EGLUtil"

EGLUtil::EGLUtil() {

}

EGLDisplay EGLUtil::initDisplay() {
    bool result;
    EGLDisplay eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    assert(eglDisplay != EGL_NO_DISPLAY);

    if (!eglInitialize(eglDisplay, 0, 0)) {
        LOGE("初始化disPlay失败");
    }
    return eglDisplay;

}

EGLConfig EGLUtil::addConfig(EGLDisplay eglDisplay) {
    int configAttr[] = {
            EGL_BUFFER_SIZE, 32,
            EGL_ALPHA_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };
    int *numConfig = new int[1];
    EGLConfig *config = new EGLConfig[1];
    if (!eglChooseConfig(eglDisplay, configAttr, config, sizeof(numConfig) / sizeof(numConfig[0]),
                         numConfig)) {
        LOGE("createEGL:配置属性信息失败 ");
    }
    EGLConfig eglConfig = config[0];
    delete[] config;
    return eglConfig;
}

EGLContext EGLUtil::createContext(EGLDisplay eglDisplay, EGLConfig eglConfig) {
    int contextAttributes[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };
    EGLContext eglContext = eglCreateContext(eglDisplay, eglConfig, NULL, contextAttributes);
    if (eglContext == EGL_NO_CONTEXT) {
        LOGE("createEGL:获取上下文信息失败 ");
    }
    return eglContext;
}

void EGLUtil::destroyEGL(EGLDisplay eglDisplay, EGLContext eglContext) {
    eglDestroyContext(eglDisplay, eglContext);
    eglContext = EGL_NO_CONTEXT;
    eglDisplay = EGL_NO_DISPLAY;
}

EGLUtil::~EGLUtil() {
}