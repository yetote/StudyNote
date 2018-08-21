//
// Created by ether on 2018/8/21.
//

#include <GLES2/gl2.h>
#include "GLUtil.h"
#include <android/log.h>

#define LOG_TAG "GLUtil"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

int GLUtil::compileShader(int type, const char *shaderCode) {
    int shader = glCreateShader(type);
    if (shader == 0) {
        LOGE("创建着色器失败");
    }
    glShaderSource(shader, 1, &shaderCode, NULL);
    glCompileShader(shader);
    GLint compileStatus = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
    if (!compileStatus) {
        glDeleteShader(shader);
        LOGE("加载着色器代码失败");
    }
    return shader;
}

int GLUtil::createProgram(const char *vertexShaderCode, const char *fragmentShaderCode) {
    GLint program=glCreateProgram();
    if (program){
        LOGE("创建连接程序失败");
    }

}