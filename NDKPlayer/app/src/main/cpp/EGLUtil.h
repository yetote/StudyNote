//
// Created by ether on 2018/8/28.
//

#ifndef OPENGLNATIVEDEMO_EGLUTIL_H
#define OPENGLNATIVEDEMO_EGLUTIL_H

#include <EGL/egl.h>

class EGLUtil {
public:

    EGLUtil();

    /*
     * 初始化设备
     * */
    EGLDisplay initDisplay();

    EGLConfig addConfig(EGLDisplay eglDisplay);

    /*
     * 创建egl上下文
     * */
    EGLContext createContext(EGLDisplay eglDisplay, EGLConfig eglConfig);

    /*
     * 销毁egl
     * */
    void destroyEGL(EGLDisplay eglDisplay, EGLContext eglContext);

    ~EGLUtil();

};


#endif //OPENGLNATIVEDEMO_EGLUTIL_H
