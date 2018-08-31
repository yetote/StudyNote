//
// Created by ether on 2018/8/31.
//

#ifndef MYPLAYER_EGL_CORE_H
#define MYPLAYER_EGL_CORE_H


#include <EGL/egl.h>

//不知道这货是干啥的
typedef EGLBoolean (EGLAPIENTRYP PFNEGLPRESENTATIONTIMEANDROIDPROC)(EGLDisplay display,
                                                                    EGLSurface surface,
                                                                    khronos_stime_nanoseconds_t time);

class EGLCore {
public:
    EGLCore();

    virtual ~EGLCore();

    bool init();

    bool init(EGLContext context);

    bool initWithSharedContext();

    /**
     * 将egl和屏幕绑定
     * @param _window 显示对象
     * @return 是否成功
     */
    EGLSurface createWindowSurface(ANativeWindow *_window);

    EGLSurface createOffscreenSurface(int width, int height);

    bool makeCurrent(EGLSurface eglSurface);

    void doneCurrent();

    bool swapBuffers(EGLSurface eglSurface);

    int querySurface(EGLSurface surface, int what);

    int setPresentationTime(EGLSurface surface, khronos_stime_nanoseconds_t nsecs);

    /**
     * 释放surface资源
     * @param eglSurface  surface
     */
    void releaseSurface(EGLSurface eglSurface);

    /**
     * 释放资源
     */
    void release();

    EGLContext getContext();

    EGLDisplay getDisplay();

    EGLConfig getConfig();

private:
    EGLDisplay display;
    EGLConfig config;
    EGLContext context;

    PFNEGLPRESENTATIONTIMEANDROIDPROC pfneglPresentationTimeANDROID;
};


#endif //MYPLAYER_EGL_CORE_H
