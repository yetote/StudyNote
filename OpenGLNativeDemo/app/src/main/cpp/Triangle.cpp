//
// Created by ether on 2018/8/21.
//

#include <cmath>
#include "Triangle.h"
#include "GLUtil.h"
#include <GLES2/gl2.h>

void Triangle::initVertex() {
    vertexArray = new GLfloat[15]{
            -0.5f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.0f, 0.0f, 0.0f, 1.0f

    };
//    int i = 0, j = 0;
//    vertexArray[i++] = 0;
//    vertexArray[i++] = 0;
//
//    vertexArray[i++] = 1;
//    vertexArray[i++] = 1;
//    vertexArray[i++] = 1;
//
//    for (int angle = 0; angle <= 360; angle += 60) {
//        vertexArray[i++] = (GLfloat) (cos(M_PI * angle / 180));
//        vertexArray[i++] = (GLfloat) (sin(M_PI * angle / 180));
//
//        vertexArray[i++] = 1;
//        vertexArray[i++] = 0;
//        vertexArray[i++] = 0;
//    }

}

void Triangle::initGL(const char *vertexShaderCode, const char *fragmentShaderCode) {
    program = GLUtil::createProgram(vertexShaderCode, fragmentShaderCode);
    uMVPMatrix = glGetUniformLocation(program, "uMVPMatrix");
    aPosition = glGetAttribLocation(program, "a_Position");
    aColor = glGetAttribLocation(program, "a_Color");

}

void Triangle::draw() {
    glUseProgram(program);
    glVertexAttribPointer(aPosition, 2, GL_FLOAT, GL_FALSE, 5 * 4, vertexArray);
    glVertexAttribPointer(aColor, 3, GL_FLOAT, GL_FALSE, 5 * 4, vertexArray);
    glEnableVertexAttribArray(aPosition);
    glEnableVertexAttribArray(aColor);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 3);
}

Triangle::Triangle() {
    initVertex();
}

Triangle::~Triangle() {
    delete[] vertexArray;
}