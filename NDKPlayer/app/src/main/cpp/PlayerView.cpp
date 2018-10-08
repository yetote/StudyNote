
//
// Created by ether on 2018/10/8.
//

#include <EGL/egl.h>
#include "PlayerView.h"
#include "GLUtil.h"
#include <android/log.h>

#define LOG_TAG "PlayerView"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

void PlayerView::initVertex() {
    vertexArray = new GLfloat[24]{
            0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 1.0f, 0.0f
    };
}

void PlayerView::initLocation(const char *vertexCode, const char *fragmentCode) {
    program = GLUtil::createProgram(vertexCode, fragmentCode);

    textureY = GLUtil::createTexture()[0];
    textureU = GLUtil::createTexture()[1];
    textureV = GLUtil::createTexture()[2];
    aPosition = glGetAttribLocation(program, "a_Position");
    aTextureCoordinates = glGetAttribLocation(program, "a_TextureCoordinates");
    uTexY = glGetUniformLocation(program, "u_TexY");
    uTexU = glGetUniformLocation(program, "u_TexU");
    uTexV = glGetUniformLocation(program, "u_TexV");
}

void PlayerView::draw(AVFrame *frame, EGLDisplay eglDisplay, EGLSurface eglSurface,
                      EGLContext eglContext) {
    glUseProgram(program);

    eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureY);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320, 240, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                 frame->data[0]);
    glUniform1i(uTexY, 0);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, textureU);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320 / 2, 240 / 2, 0, GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data[1]);
    glUniform1i(uTexU, 1);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, textureV);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 320 / 2, 240 / 2, 0, GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data[2]);
    glUniform1i(uTexV, 2);
    glVertexAttribPointer(aPosition, 2, GL_FLOAT, GL_FALSE, 4 * 4, vertexArray);
    glEnableVertexAttribArray(aPosition);
    glVertexAttribPointer(aTextureCoordinates, 2, GL_FLOAT, GL_FALSE, 4 * 4, vertexArray);
    glEnableVertexAttribArray(aTextureCoordinates);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    eglSwapBuffers(eglDisplay, eglSurface);
    LOGE("a");
}

void PlayerView::threadDraw(AVFrame *frame, EGLDisplay eglDisplay, EGLSurface eglSurface) {
//    std::thread t(draw,frame,eglDisplay,eglSurface);
//    t.join();
}