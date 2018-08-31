//
// Created by ether on 2018/8/31.
//

#ifndef MYPLAYER_VIDEOPLAYERCONTROLLER_H
#define MYPLAYER_VIDEOPLAYERCONTROLLER_H

#include <android/native_window.h>
#include <EGL/egl.h>
#include <jni.h>

class VideoPlayerController {
public:
    VideoPlayerController();

    ~VideoPlayerController();


    /**
     * 初始化播放器
     * @param srcFilenameParam 文件名
     * @param g_jvm 虚拟机实例
     * @param obj obj
     * @param max_analyze_duration 最大分析时间？？
     * @param analyzeCnt 分析的数量？？
     * @param probesize 莫名其妙的缩写
     * @param fpsProbeSizeConfigured
     * @param minBufferedDuration
     * @param maxBufferedDuration
     * @return 是否初始化成功
     */
    bool init(char *srcFilenameParam, JavaVM *g_jvm, jobject obj, int *max_analyze_duration,
              int analyzeCnt, int probesize, bool fpsProbeSizeConfigured, float minBufferedDuration,
              float maxBufferedDuration);

/**
 * 播放
 */
    void play();


    /** 暂停播放 **/
    void pause();

    /** 销毁播放器 **/
    virtual void destroy();

    void resetRenderSize(int left, int top, int width, int height);

private:
    ANativeWindow *window;
    EGLContext mSharedEGLContext;
    /** 整个movie是否在播放 **/
    bool isPlaying;


    /** 用于回调Java层 **/
    JavaVM *g_jvm;
    jobject obj;

    /** 当初始化完毕之后 回调给客户端 **/
    void setInitializedStatus(bool initCode);
};


#endif //MYPLAYER_VIDEOPLAYERCONTROLLER_H
