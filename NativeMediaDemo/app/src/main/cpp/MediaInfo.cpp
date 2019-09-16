//
// Created by ether on 2019/9/16.
//

#include "MediaInfo.h"

MediaInfo::MediaInfo(MediaInfo::MEDIA_TYPE type) : type(type) {}

MediaInfo::~MediaInfo() {
    if (codec != nullptr) {
        AMediaCodec_stop(codec);
        AMediaCodec_delete(codec);
        codec = nullptr;
    }

    if (extractor != nullptr) {
        AMediaExtractor_delete(extractor);
        extractor = nullptr;
    }
    LOGE(MediaInfo_TAG, "%s:销毁", __func__);
}
