//
// Created by ether on 2018/8/21.
//

#include "../jni/com_example_ether_openglnativedemo_MyRenderer.h"
#include "../jni/glm/mat4x4.hpp"
#include "../jni/glm/ext.hpp"
#include "Triangle.h"

glm::mat4 projection;
glm::mat4 view;
Triangle triangle;

JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_MyRenderer_create
        (JNIEnv *env, jobject obj, jstring vertexShader, jstring fragmentShader) {
    const char *vertexShaderCode = env->GetStringUTFChars(vertexShader, NULL);
    const char *fragmentShaderCode = env->GetStringUTFChars(fragmentShader, NULL);
    triangle.initGL(vertexShaderCode, fragmentShaderCode);
    env->ReleaseStringUTFChars(vertexShader, vertexShaderCode);
    env->ReleaseStringUTFChars(fragmentShader, fragmentShaderCode);
    glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
    glDisable(GL_DEPTH_TEST);
}

JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_MyRenderer_draw
        (JNIEnv *, jobject, jfloat, jfloat) {
    glClear(GL_COLOR_BUFFER_BIT);
    glm::mat4 mvpMatrix = projection * view;
    float *mvp = glm::value_ptr(mvpMatrix);
    triangle.draw(mvp);
}


JNIEXPORT void JNICALL Java_com_example_ether_openglnativedemo_MyRenderer_change
        (JNIEnv *, jobject, jint width, jint height) {
    projection = glm::ortho(-1.0f, 1.0f, -(float) (height / width), (float) (height / width), 5.0f,
                            7.0f);
//    view = glm::lookAt(glm::make_vec3(0.0f, 0.0f, 6.0f),
//                       glm::make_vec3(0.0f, 0.0f, 0.0f),
//                       glm::make_vec3(0.0f, 1.0f, 0.0f));
    glViewport(0, 0, width, height);
}
