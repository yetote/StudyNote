//
// Created by ether on 2019/9/16.
//

#ifndef NATIVEMEDIADEMO_MEDIAINFO_H
#define NATIVEMEDIADEMO_MEDIAINFO_H

#include "media/NdkMediaCodec.h"
#include "media/NdkMediaExtractor.h"
#include <android/log.h>

#define MediaInfo_TAG "MediaInfo"
#define LOGE(LOG_TAG, ...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

class MediaInfo {
public:
    enum MEDIA_TYPE {
        MEDIA_TYPE_AUDIO,
        MEDIA_TYPE_VIDEO
    };
    MEDIA_TYPE type;
    bool isInputEof = false;
    bool isOutputEof = false;
    AMediaCodec *codec = nullptr;
    AMediaExtractor *extractor = nullptr;
    int64_t renderStart = -1;
    bool isSuccess = false;

    MediaInfo(MEDIA_TYPE type);

    virtual ~MediaInfo();

private:

};


#endif //NATIVEMEDIADEMO_MEDIAINFO_H
