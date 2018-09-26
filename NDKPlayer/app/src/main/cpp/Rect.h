//
// Created by ether on 2018/9/26.
//

#ifndef NDKPLAYER_RECT_H
#define NDKPLAYER_RECT_H


#include <GLES2/gl2.h>

class Rect {
public:
    GLuint aPositionLocation, aTexCoordLocation;
    GLuint uTexYLocation, uTexULocation, uTexVLocation;
    GLfloat vertexData[] = {
            //x,y,s,t
            1f, 1f, 1f, 0f,
            -1f, -1f, 0f, 0f,
            -1f, -1f, 0f, 1f,
            -1f, -1f, 0f, 1f,
            1f, -1f, 1f, 1f,
            1f, 1f, 1f, 0f
    };
    void bindData(const GLchar *vertexShaderCode, const GLchar *fragShaderCode);

    void setUniform(int textureID, GLbyte buffer[], int type);

    void draw();

    Rect();

    ~Rect();

private:
};


#endif //NDKPLAYER_RECT_H
