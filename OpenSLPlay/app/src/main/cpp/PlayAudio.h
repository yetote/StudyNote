//
// Created by ether on 2018/10/15.
//

#ifndef OPENSLPLAY_PLAYAUDIO_H
#define OPENSLPLAY_PLAYAUDIO_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <cstdio>

class PlayAudio {
public:

    void play(const char *pcmPath);



private:
    SLObjectItf engineObj, outputMixObj, bqPlayerObj;
    SLEngineItf engineEngine;
    SLEnvironmentalReverbItf environmentalReverb;
    static SLEnvironmentalReverbSettings reverbSettings;
    SLPlayItf bqPlayer;
    SLVolumeItf bqPlayerVolume;
    SLAndroidSimpleBufferQueueItf bufferQueueObj;
    SLEffectSendItf bqPlayerEffectSend;
    void start();
    void setDataSource(const char *pcmPath);
    void prepare();
};


#endif //OPENSLPLAY_PLAYAUDIO_H
