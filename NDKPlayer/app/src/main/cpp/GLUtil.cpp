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
        char errlog[1024] = {0};
        GLsizei logLen = 0;
        glGetShaderInfoLog(shader, 1024, &logLen, errlog);
        LOGE("加载着色器代码失败error code :%s", errlog);
        glDeleteShader(shader);
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

void GLUtil::textureHelper(GLuint *textureArr) {
    int length = sizeof(textureArr) / sizeof(textureArr[0]);
    if (length <= 0) {
        LOGE("纹理数组长度不正确");
        return;
    }
    glGenTextures(length, textureArr);
    if (textureArr[0] == 0) {
        LOGE("无法创建纹理");
        return;
    }
    for (int i = 0; i < length; ++i) {
        glActiveTexture(GL_TEXTURE_2D + i);
        glBindTexture(GL_TEXTURE_2D, textureArr[i]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
    glBindTexture(GL_TEXTURE_2D, 0);
    LOGE("纹理创建成功 hey man!!!");
}
