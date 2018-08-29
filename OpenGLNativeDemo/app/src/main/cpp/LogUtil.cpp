//
// Created by ether on 2018/8/28.
//

#include "LogUtil.h"
#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
LogUtil LogUtil;

void LogUtil::loge(char * tag,char *info) {
//    __android_log_print(ANDROID_LOG_ERROR, tag, info);
}