//
// Created by ether on 2018/10/8.
//

#ifndef NDKPLAYER_PLAYERVIEW_H
#define NDKPLAYER_PLAYERVIEW_H


#include <GLES2/gl2.h>

extern "C" {
#include <libavutil/frame.h>
};

class PlayerView {
public:
    void initVertex();

    void initLocation(const char *vertexCode, const char *fragmentCode);

    void threadDraw(AVFrame *frame, EGLDisplay eglDisplay, EGLSurface eglSurface);

    void draw(AVFrame *frame, EGLDisplay eglDisplay, EGLSurface eglSurface, EGLContext eglContext);

private:
    GLfloat *vertexArray;
    GLint aPosition, aTextureCoordinates;
    GLint uTexY, uTexU, uTexV;
    GLuint program;
    GLint textureY, textureU, textureV;
};


#endif //NDKPLAYER_PLAYERVIEW_H
