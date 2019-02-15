//
// Created by ether on 2018/10/29.
//

#ifndef BAMBOO_PLAYERVIEW_H
#define BAMBOO_PLAYERVIEW_H


#include "../util/BlockQueue.h"
#include "GLUtil.h"
#include "EGLUtil.h"
#include <GLES2/gl2ext.h>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <EGL/egl.h>

extern "C" {
#include "libavutil/frame.h"
};

class PlayerView {
public:
    GLfloat *vertex;
    GLfloat *vertexArray;
    GLfloat *textureArray;

    GLuint program;
    GLuint *textureArr;
    GLint aPosition, aColor;
    GLint aTextureCoordinates;
    GLint uTexY, uTexU, uTexV;


    void play(BlockQueue<AVFrame *> &blackQueue, const char *vertexCode, const char *fragCode,
              ANativeWindow *window, int w, int h);


private:
    void initEGL(ANativeWindow *window);

    void initVertex();

    void initLocation(const char *vertexCode, const char *fragCode);

    void draw(AVFrame *frame);
};


#endif //BAMBOO_PLAYERVIEW_H
