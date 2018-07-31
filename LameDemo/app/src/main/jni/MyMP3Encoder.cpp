//
// Created by ether on 2018/7/31.
//

#include "com_example_ether_lamedemo_MyMP3Encoder.h"
#include "MP3encoder.h"

MP3encoder *encoder = NULL;

JNIEXPORT jint JNICALL
Java_com_example_ether_lamedemo_MyMP3Encoder_init(JNIEnv *env, jobject obj, jstring pcmPathParams,
                                                  jint channels, jint bitRate, jint sampleRate,
                                                  jstring mp3PathParams) {
    const char *pcmPath = env->GetStringUTFChars(pcmPathParams, NULL);
    const char *mp3Path = env->GetStringUTFChars(mp3PathParams, NULL);
    encoder = new MP3encoder();
    encoder->Init(pcmPath, mp3Path, sampleRate, channels, bitRate);
    env->ReleaseStringUTFChars(mp3PathParams, mp3Path);
    env->ReleaseStringUTFChars(pcmPathParams, pcmPath);
    return 0;
}

JNIEXPORT void JNICALL Java_com_example_ether_lamedemo_MyMP3Encoder_encode
        (JNIEnv *, jobject) {
    if (encoder != NULL) {
        encoder->Encode();
    }
}

JNIEXPORT void JNICALL Java_com_example_ether_lamedemo_MyMP3Encoder_destroy
        (JNIEnv *, jobject) {
    if (NULL != encoder) {
        encoder->Destroy();
        delete encoder;
        encoder = NULL;
    }
}