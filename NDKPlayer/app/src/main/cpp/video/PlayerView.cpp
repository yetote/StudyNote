//
// Created by ether on 2018/10/29.
//


#include "PlayerView.h"

#define LOG_TAG "PlayerView"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define null NULL

GLUtil *glUtil;
EGLUtil *eglUtil;

void PlayerView::initVertex() {
//    @formatter:off
//    vertex = new GLfloat[24]{
////             x,     y,    s,    w
//            1.0f, 1.0f, 1.0f, 0.0f,
//            -1.0f, -1.0f, 0.0f, 0.0f,
//            -1.0f, -1.0f, 0.0f, 1.0f,
//
//            -1.0f, -1.0f, 0.0f, 1.0f,
//            1.0f, -1.0f, 1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f, 0.0f
//    };
    vertexArray = new GLfloat[12]{
            0.5f, 0.5f,
            -0.5f, 0.5f,
            -0.5f, -0.5f,

            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
    };
    textureArray = new GLfloat[12]{
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,

            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
//    textureArray = new GLfloat[18]{
//            1.0f, 0.0f, 0.0f,
//            0.0f, 1.0f, 0.0f,
//            0.0f, 0.0f, 1.0f,
//
//            1.0f, 0.0f, 0.0f,
//            1.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f
//    };
//    @formatter:on

    delete[] vertex;
}

void PlayerView::initLocation(const char *vertexCode, const char *fragCode) {
    glUtil = new GLUtil;
    glUtil->createProgram(vertexCode, fragCode);
    textureArr = glUtil->createTexture();
//    LOGE("%d", textureArr[0]);
    aPosition = glGetAttribLocation(glUtil->program, "a_Position");
//    aColor = glGetAttribLocation(glUtil->program, "a_Color");
    aTextureCoordinates = glGetAttribLocation(glUtil->program, "a_TextureCoordinates");
    uTexY = glGetUniformLocation(glUtil->program, "u_TexY");
    uTexU = glGetUniformLocation(glUtil->program, "u_TexU");
    uTexV = glGetUniformLocation(glUtil->program, "u_TexV");
    glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
}

void
PlayerView::play(BlockQueue<AVFrame *> &blackQueue, const char *vertexCode, const char *fragCode,
                 ANativeWindow *window, int w, int h) {
    initEGL(window);
    eglMakeCurrent(eglUtil->eglDisplay, eglUtil->eglSurface, eglUtil->eglSurface,
                   eglUtil->eglContext);
    initVertex();
    initLocation(vertexCode, fragCode);
    glViewport(0, 0, w, h);
    popResult popResult;
    AVFrame *avFrame;
    while (true) {
        popResult = blackQueue.pop(avFrame);
        if (popResult == POP_STOP) break;
        if (popResult == POP_UNEXPECTED) continue;
        draw(avFrame);
    }
    eglUtil->destroyCtx();
}

void PlayerView::draw(AVFrame *frame) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(glUtil->program);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureArr[0]);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_LUMINANCE,
                 320,
                 240,
                 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data[0]);
    glUniform1i(uTexY, 0);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, textureArr[1]);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_LUMINANCE,
                 320 / 2,
                 240 / 2,
                 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data[1]);
    glUniform1i(uTexU, 1);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, textureArr[2]);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_LUMINANCE,
                 320 / 2,
                 240 / 2, 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data[2]);
    glUniform1i(uTexV, 2);

    glVertexAttribPointer(aPosition, 2, GL_FLOAT, GL_FALSE, 0, vertexArray);
    glEnableVertexAttribArray(aPosition);
    glVertexAttribPointer(aTextureCoordinates, 2, GL_FLOAT, GL_FALSE, 0,
                          textureArray);
    glEnableVertexAttribArray(aTextureCoordinates);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    eglSwapBuffers(eglUtil->eglDisplay, eglUtil->eglSurface);
}

void PlayerView::initEGL(ANativeWindow *window) {
    eglUtil = new EGLUtil;
    eglUtil->createCtx();
    eglUtil->createSurface(window);
}
