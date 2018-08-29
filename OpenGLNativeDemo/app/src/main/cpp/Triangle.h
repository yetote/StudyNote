//
// Created by ether on 2018/8/21.
//

#ifndef OPENGLNATIVEDEMO_TRIANGLE_H
#define OPENGLNATIVEDEMO_TRIANGLE_H

#include <GLES2/gl2.h>

class Triangle {
public:
    GLint program;
    GLint uMVPMatrix;
    GLint aPosition;
    GLint aColor;
    GLfloat *vertexArray;
    GLfloat *colorArray;

    Triangle();

    void initVertex();

    void initGL(const char *vertexShaderCode, const char *fragmentShaderCode);

    void draw();

    ~Triangle();
};


#endif //OPENGLNATIVEDEMO_TRIANGLE_H
