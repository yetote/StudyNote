//
// Created by ether on 2018/8/31.
//

#include <android/native_window.h>
#include "egl_core.h"
#include "../../util/CommonTools.h"

#define LOG_TAG "EGLCore"

EGLCore::EGLCore() {
    pfneglPresentationTimeANDROID = 0;
    display = EGL_NO_DISPLAY;
    context = EGL_NO_CONTEXT;
}

EGLCore::~EGLCore() {

}

void EGLCore::release() {
    if (display != EGL_NO_DISPLAY && context != EGL_NO_CONTEXT) {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(display, context);
    }
    display = EGL_NO_DISPLAY;
    context = EGL_NO_CONTEXT;
}

void EGLCore::releaseSurface(EGLSurface eglSurface) {
    eglDestroySurface(display, eglSurface);
    eglSurface = EGL_NO_SURFACE;
}

EGLDisplay EGLCore::getDisplay() {
    return display;
}

EGLConfig EGLCore::getConfig() {
    return config;
}

EGLSurface EGLCore::createWindowSurface(ANativeWindow *_window) {
    EGLSurface eglSurface = NULL;
    EGLint format;
    if (_window == NULL) {
        LOGE("EGLCore::createWindowSurface  显示设备为空");
        return NULL;
    }
    if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
        LOGE("eglGetConfigAttrib() returned error %d", eglGetError());
        release();
        return eglSurface;
    }
    ANativeWindow_setBuffersGeometry(_window, 0, 0, format);
    if (!(eglSurface = eglCreateWindowSurface(display, config, _window, 0))) {
        LOGE("eglCreateWindowSurface() returned error %d", eglGetError());
    }
    return eglSurface;
}
