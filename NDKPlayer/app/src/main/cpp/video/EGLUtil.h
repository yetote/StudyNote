//
// Created by ether on 2018/10/20.
//

#ifndef BAMBOO_EGLUTIL_H
#define BAMBOO_EGLUTIL_H

#include <egl/egl.h>
#include <egl/eglext.h>
#include <android/log.h>

class EGLUtil {
public:
    EGLContext eglContext;
    EGLDisplay eglDisplay;
    EGLConfig eglConfig;
    EGLSurface eglSurface;

    void createCtx();

    void createSurface(ANativeWindow *window);

    void destroyCtx();

private:

};


#endif //BAMBOO_EGLUTIL_H
