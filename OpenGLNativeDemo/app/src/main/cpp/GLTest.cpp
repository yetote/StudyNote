//
// Created by ether on 2018/8/21.
//

#include "../jni/com_example_ether_openglnativedemo_MyRenderer.h"
#include "../jni/glm/mat4x4.hpp"
#include "../jni/glm/ext.hpp"

glm::mat4 projection;
glm::mat4 view;

JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_MyRenderer_create
        (JNIEnv *env, jobject obj, jstring vertexShader, jstring fragmentShader) {
    const char *vertexShaderCode = env->GetStringUTFChars(vertexShader, NULL);
    const char *fragmentShaderCode = env->GetStringUTFChars(fragmentShader, NULL);

}

JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_MyRenderer_draw
        (JNIEnv *, jobject, jfloat, jfloat) {

}


JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_MyRenderer_change
        (JNIEnv *, jobject, jint, jint) {

}
