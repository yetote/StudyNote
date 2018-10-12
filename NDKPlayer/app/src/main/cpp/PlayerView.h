//
// Created by ether on 2018/10/8.
//

#ifndef NDKPLAYER_PLAYERVIEW_H
#define NDKPLAYER_PLAYERVIEW_H


#include <GLES2/gl2.h>
#include "EGLUtil.h"
#include "BlockQueue.h"

extern "C" {
#include <libavutil/frame.h>
};

class PlayerView {
public:
    void play(ANativeWindow *window, BlockQueue<AVFrame *> &blockQueue, const char *vertexCode,
              const char *fragCode, int w, int h);

private:
    EGLContext eglContext;
    EGLConfig eglConfig;
    EGLDisplay eglDisplay;
    EGLSurface eglSurface;
    GLfloat *vertexArray;
    GLfloat *textureArray;
    GLint aPosition, aTextureCoordinates;
    GLint uTexY, uTexU, uTexV;
    GLuint program;
    GLint textureY, textureU, textureV;
    ANativeWindow *window;

    void initLocation(const char *vertexCode, const char *fragmentCode);

    void draw(AVFrame *frame);

    void initEGL();

    void initVertex();
};


#endif //NDKPLAYER_PLAYERVIEW_H
