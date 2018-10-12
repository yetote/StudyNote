
//
// Created by ether on 2018/10/8.
//

#include <EGL/egl.h>
#include "PlayerView.h"
#include "GLUtil.h"
#include <android/log.h>
#include <unistd.h>

#define LOG_TAG "PlayerView"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
EGLUtil eglUtil;

void PlayerView::initVertex() {
    vertexArray = new GLfloat[12]{
            1.0f, 1.0f,
            -1.0f, 1.0f,
            -1.0f, -1.0f,

            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
    };
    textureArray = new GLfloat[12]{
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,

            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
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


void PlayerView::draw(AVFrame *frame) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(program);
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
    glVertexAttribPointer(aPosition, 2, GL_FLOAT, GL_FALSE, 0, vertexArray);
    glEnableVertexAttribArray(aPosition);
    glVertexAttribPointer(aTextureCoordinates, 2, GL_FLOAT, GL_FALSE, 0, textureArray);
    glEnableVertexAttribArray(aTextureCoordinates);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    eglSwapBuffers(eglDisplay, eglSurface);
}


void PlayerView::initEGL() {
    this->eglDisplay = eglUtil.initDisplay();
    this->eglConfig = eglUtil.addConfig(this->eglDisplay);
    this->eglContext = eglUtil.createContext(this->eglDisplay,
                                             this->eglConfig);
    int *surfaceAttributes = new int[1]{EGL_NONE};
    this->eglSurface = eglCreateWindowSurface(this->eglDisplay,
                                              this->eglConfig,
                                              this->window,
                                              surfaceAttributes);
    eglMakeCurrent(this->eglDisplay, this->eglSurface, this->eglSurface, this->eglContext);
}

void
PlayerView::play(ANativeWindow *window, BlockQueue<AVFrame *> &blockQueue, const char *vertexCode,
                 const char *fragCode, int w, int h) {
    this->window = window;
    initEGL();

    initVertex();
    initLocation(vertexCode, fragCode);
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glViewport(0, 0, w, h);

    AVFrame *avFrame;
    while (true) {
        popResult res = blockQueue.pop(avFrame);
        LOGE("A");
        if (res == POP_STOP) break;
        if (res == POP_UNEXPECTED) continue;
        draw(avFrame);

    }
    eglDestroySurface(this->eglDisplay, this->eglSurface);
    eglUtil.destroyEGL(this->eglDisplay, this->eglContext);
}
