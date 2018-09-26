//
// Created by ether on 2018/9/26.
//

#include <android/log.h>
#include "GLUtil.h"

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);

GLuint GLUtil::compileShader(GLenum type, const GLchar *shaderCode) {
    GLuint shader = glCreateShader(type);
    if (shader == 0) {
        LOGE("创建shader失败");
        return 0;
    }
    glShaderSource(shader, 1, &shaderCode, NULL);
    glCompileShader(shader);
    GLint compileStatus = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
    if (!compileStatus) {
        glDeleteShader(shader);
        LOGE("加载着色器代码失败");
        return 0;
    }
    LOGE("创建shader成功 hey man!!!");
    return shader;
}

void GLUtil::createProgram(const GLchar *vertexShaderCode, const GLchar *fragmentShaderCode) {
    program = glCreateProgram();
    if (program == 0) {
        LOGE("创建program失败");
        return;
    }
    GLuint vertexShaderID = compileShader(GL_VERTEX_SHADER, vertexShaderCode);
    GLuint fragShaderID = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode);
    glAttachShader(program, vertexShaderID);
    glAttachShader(program, fragShaderID);
    glLinkProgram(program);
    GLint linkStatus;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (linkStatus == 0) {
        glDeleteProgram(program);
        LOGE("程序连接失败");
        return;
    }
    LOGE("创建program成功 hey man!!!");
}

