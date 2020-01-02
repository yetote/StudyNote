#include <jni.h>
#include <string>
#include "decode/FFmpegDemode.h"

JavaVM *jvm;
FFmpegDemode *fFmpegDecode;


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    jvm = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    JNINativeMethod methods[]{

    };
    jclass jlz = env->FindClass("com/pet/ffmpegdemo/Player");
    env->RegisterNatives(jlz, methods, sizeof(methods) / sizeof(methods[0]));
    return JNI_VERSION_1_6;
}

