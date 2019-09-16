//
// Created by ether on 2019/8/13.
//

#include "PlayStates.h"

PlayStates::PlayStates() {
    eof = false;
    pause = false;
    stop = false;
    LOGE(PlayStates_TAG, "%s:初始化", __func__);
}

bool PlayStates::isEof() const {
    return eof;
}

void PlayStates::setEof(bool eof) {
    PlayStates::eof = eof;
}

bool PlayStates::isPause() const {
    return pause;
}

void PlayStates::setPause(bool pause) {
    PlayStates::pause = pause;
}

bool PlayStates::isStop() const {
    return stop;
}

void PlayStates::setStop(bool stop) {
    PlayStates::stop = stop;
}

PlayStates::MEDIA_TYPE PlayStates::getMediaType() const {
    return mediaType;
}

void PlayStates::setMediaType(PlayStates::MEDIA_TYPE mediaType) {
    PlayStates::mediaType = mediaType;
}

bool PlayStates::isHardware() const {
    return hardware;
}

void PlayStates::setHardware(bool hardware) {
    PlayStates::hardware = hardware;
}


