//
// Created by ether on 2018/8/21.
//

#include <cmath>
#include "Triangle.h"
#include <GLES2/>
void Triangle::initVertex() {
    vertexArray = new GLfloat[5 * 8];
    int i = 0, j = 0;
    vertexArray[i++]=0;
    vertexArray[i++]=0;

    vertexArray[i++]=1;
    vertexArray[i++]=1;
    vertexArray[i++]=1;

    for (int angle = 0; angle <= 360; angle += 60) {
        vertexArray[i++] = (GLfloat) (cos(M_PI * angle / 180));
        vertexArray[i++] = (GLfloat) (sin(M_PI * angle / 180));

        vertexArray[i++] = 1;
        vertexArray[i++] = 0;
        vertexArray[i++] = 0;
    }    

}

void Triangle::initGL() {

}

void Triangle::draw(float *mvpMatrix) {

}