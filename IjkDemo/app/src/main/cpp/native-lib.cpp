#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_yetote_ijkdemo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    mallocz();
    return env->NewStringUTF(hello.c_str());

}
