//
// Created by ether on 2018/7/31.
//

#ifndef LAMEDEMO_MP3ENCODER_H
#define LAMEDEMO_MP3ENCODER_H


#include <stdio.h>
#include "libmp3lame/lame.h"

class MP3encoder {
private:
    FILE *pcmFile;
    FILE *mp3File;
    lame_t lameClient;
public:
    MP3encoder();

    ~MP3encoder();

    int Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate, int channels,
             int bitRate);

    void Encode();

    void Destroy();


};


#endif //LAMEDEMO_MP3ENCODER_H
