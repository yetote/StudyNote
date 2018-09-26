//
// Created by ether on 2018/9/26.
//

#ifndef NDKPLAYER_EGLUTIL_H
#define NDKPLAYER_EGLUTIL_H

#include <egl/egl.h>

class EGLUtil {
public:
    EGLContext context;
    EGLConfig config;
    EGLDisplay display;
    EGLSurface surface;

    void createContext();

    void destroyContext();

    EGLUtil();

    ~EGLUtil();

private:

};


#endif //NDKPLAYER_EGLUTIL_H
