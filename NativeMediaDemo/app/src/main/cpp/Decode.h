//
// Created by ether on 2019/9/12.
//

#ifndef NATIVEMEDIADEMO_DECODE_H
#define NATIVEMEDIADEMO_DECODE_H

#include <string>
#include <android/log.h>
#include <thread>
#include "MediaInfo.h"
#include "audio/AudioPlay.h"

#define Decode_TAG "Decode"
#define LOGE(LOG_TAG, ...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

class Decode {
public:
    bool init(std::string path, ANativeWindow *window);

    void play();

    Decode();

    virtual ~Decode();

private:
    std::shared_ptr<MediaInfo> audioPtr, videoPtr;
    std::shared_ptr <AudioPlay> audioPlay;

    void doDecode(std::shared_ptr<MediaInfo>);

    void playAudio();

    void playVideo();
};


#endif //NATIVEMEDIADEMO_DECODE_H
