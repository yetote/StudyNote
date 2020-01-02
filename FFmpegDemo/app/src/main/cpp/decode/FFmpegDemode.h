//
// Created by ether on 2019/12/26.
//

#ifndef FFMPEGDEMO_FFMPEGDEMODE_H
#define FFMPEGDEMO_FFMPEGDEMODE_H

#include <string>
class FFmpegDemode {
public:
    FFmpegDemode();

    void prepare(std::string path);

    virtual ~FFmpegDemode();
};


#endif //FFMPEGDEMO_FFMPEGDEMODE_H
