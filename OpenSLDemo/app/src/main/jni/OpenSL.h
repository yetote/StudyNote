//
// Created by ether on 2018/8/14.
//

#ifndef OPENSLDEMO_OPENSL_H
#define OPENSLDEMO_OPENSL_H


#include <SLES/OpenSLES_Android.h>

class OpenSL {
public:
    /**
     * 创建引擎
     */
    SLEngineItf createEngine();

    /**
     * pcm接口回调
     * @param bf 缓冲数据
     * @param context 。。。
     */
    void pcmCall(SLAndroidSimpleBufferQueueItf bf, void *context, const char* pcmPath);

    /**
     * 析构函数
     */
    ~OpenSL();
};


#endif //OPENSLDEMO_OPENSL_H
