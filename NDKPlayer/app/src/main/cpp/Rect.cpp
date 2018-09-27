//
// Created by ether on 2018/9/26.
//

#include <GLES2/gl2.h>
#include <android/log.h>
#include <EGL/egl.h>
#include "Rect.h"
#include "GLUtil.h"

#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);

GLUtil *glUtil;
GLuint textureLocationArr[3];

void Rect::bindData(const GLchar *vertexShaderCode, const GLchar *fragShaderCode) {
    GLuint POSITION_COUNT = 2;
    GLuint TEXTURE_COUNT = 2;
    GLuint STRIDE = (POSITION_COUNT + TEXTURE_COUNT) * 4;
    glUtil->createProgram(vertexShaderCode, fragShaderCode);
    aPositionLocation = glGetAttribLocation(glUtil->program, "a_Position");
    aTexCoordLocation = glGetAttribLocation(glUtil->program, "a_TextureCoordinates");
    textureLocationArr[0] = glGetUniformLocation(glUtil->program, "u_TexY");
    textureLocationArr[1] = glGetUniformLocation(glUtil->program, "u_TexU");
    textureLocationArr[2] = glGetUniformLocation(glUtil->program, "u_TexV");
    glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT, GL_FALSE, STRIDE,
                          vertexData);
    glVertexAttribPointer(aTexCoordLocation, TEXTURE_COUNT, GL_FLOAT, GL_FALSE, STRIDE, vertexData);
    glEnableVertexAttribArray(aPositionLocation);
    glEnableVertexAttribArray(aTexCoordLocation);
    glUtil->textureHelper(textureArr);


}

void Rect::setUniform(AVFrame *avFream) {
    int length = sizeof(textureArr) / sizeof(textureArr[0]);
//    if (length != 3) {
//        LOGE("纹理id数组长度不对");
//        return;
//    }

    for (int i = 0; i < length; ++i) {
        glActiveTexture(GL_TEXTURE0 + i);
        glBindTexture(GL_TEXTURE_2D, textureArr[i]);
        if (i == 0) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320, 240, 0, GL_LUMINANCE,
                         GL_UNSIGNED_BYTE, avFream->data[i]);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320 / 2, 240 / 2, 0, GL_LUMINANCE,
                         GL_UNSIGNED_BYTE, avFream->data[i]);

        }
        glUniform1i(textureLocationArr[i], i);
    }

}

void Rect::draw(EGLDisplay display, EGLSurface eglSurface) {
    glUseProgram(glUtil->program);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    eglSwapBuffers(display, eglSurface);
}

Rect::Rect() {
    glUtil = new GLUtil();
}

Rect::~Rect() {
    delete glUtil;
}