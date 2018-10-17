//
// Created by ether on 2018/10/15.
//

#ifndef OPENSLPLAY_PLAYAUDIO_H
#define OPENSLPLAY_PLAYAUDIO_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <cstdio>
#include "AudioUtil.h"
#include "BlockQueue.h"
#include <android/log.h>
#include <cstddef>
#include <cstdlib>
#include <unistd.h>

class PlayAudio {
public:

    void play(BlockQueue<audioType> &blockQueue);


private:
    SLObjectItf engineObj, outputMixObj, bqPlayerObj;
    SLEngineItf engineEngine;
    SLEnvironmentalReverbItf environmentalReverb;
    SLEnvironmentalReverbSettings reverbSettings;
    SLPlayItf bqPlayer;
    SLVolumeItf bqPlayerVolume;
    SLAndroidSimpleBufferQueueItf bufferQueueObj;
    SLEffectSendItf bqPlayerEffectSend;

    void start();

    void setDataSource(const char *pcmPath);

    void prepare();
};


#endif //OPENSLPLAY_PLAYAUDIO_H
