//
// Created by ether on 2018/9/26.
//

#ifndef NDKPLAYER_RECT_H
#define NDKPLAYER_RECT_H


#include <GLES2/gl2.h>
#include <libavutil/frame.h>

class Rect {
public:
    GLuint aPositionLocation, aTexCoordLocation;
    GLuint uTexYLocation, uTexULocation, uTexVLocation;
    GLfloat vertexData[24] = {
            //x,y,s,t
            1, 1, 1, 0,
            -1, -1, 0, 0,
            -1, -1, 0, 1,
            -1, -1, 0, 1,
            1, -1, 1, 1,
            1, 1, 1, 0
    };
    GLuint textureArr[3];

    void bindData(const GLchar *vertexShaderCode, const GLchar *fragShaderCode);

    void setUniform(AVFrame *avFrame);

    void draw(EGLDisplay display, EGLSurface eglSurface);

    Rect();

    ~Rect();

    static struct {
        int Y = 320;
        int U = (width.Y / 2);
        int V = width.U;
    } width;
    static struct {
        int Y = 240;
        int U = (height.Y / 2);
        int V = height.U;
    } height;
private:

};


#endif //NDKPLAYER_RECT_H
