#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_newplayer_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_newplayer_Encode_videoEncode(JNIEnv *env, jobject instance, jstring inputPath_) {
    const char *inputPath = env->GetStringUTFChars(inputPath_, 0);



    env->ReleaseStringUTFChars(inputPath_, inputPath);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_newplayer_Encode_audioEncode(JNIEnv *env, jobject instance, jstring inputPath_) {
    const char *inputPath = env->GetStringUTFChars(inputPath_, 0);

    // TODO

    env->ReleaseStringUTFChars(inputPath_, inputPath);
}