//
// Created by ether on 2018/8/9.
//

#include "com_example_ether_ffmpegdemo_FFmpegTest.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>


JNIEXPORT jstring JNICALL Java_com_example_ether_ffmpegdemo_FFmpegTest_avcodecInfo
        (JNIEnv *env, jobject) {
    av_register_all();
    return env->NewStringUTF(avcodec_configuration());
}

/*
 * Class:     com_example_ether_ffmpegdemo_FFmpegTest
 * Method:    avformatInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_example_ether_ffmpegdemo_FFmpegTest_avformatInfo
        (JNIEnv *env, jobject) {
    av_register_all();
    return env->NewStringUTF(avformat_configuration());
}

/*
 * Class:     com_example_ether_ffmpegdemo_FFmpegTest
 * Method:    avfilterInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_example_ether_ffmpegdemo_FFmpegTest_avfilterInfo
        (JNIEnv *env, jobject) {
    char info[40000] = {0};
    avfilter_register_all();
    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
    while (f_temp != NULL) {
        sprintf(info, "%s[%10s]\n", info, f_temp->name);
    }
    //LOGE("%s", info);

    return env->NewStringUTF(info);
}
#ifdef __cplusplus
}
#endif