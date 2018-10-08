//
// Created by ether on 2018/8/28.
//
#include <android/log.h>
#ifndef OPENGLNATIVEDEMO_LOGUTIL_H
#define OPENGLNATIVEDEMO_LOGUTIL_H
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

class LogUtil {
public:
    void loge(char *tag, char *info);
};


#endif //OPENGLNATIVEDEMO_LOGUTIL_H
