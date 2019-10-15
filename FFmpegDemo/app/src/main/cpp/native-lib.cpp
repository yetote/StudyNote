#include <jni.h>
#include <string>
#include <vector>
#include "Decode.h"

Decode *decode;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_ffmpegdemo_Player_prepare(JNIEnv *env, jobject thiz, jstring path_) {
    std::string path = env->GetStringUTFChars(path_, JNI_FALSE);
    decode = new Decode();
    decode->prepare(path);

    env->ReleaseStringUTFChars(path_, path.c_str());
}




