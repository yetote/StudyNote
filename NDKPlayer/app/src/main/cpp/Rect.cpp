//
// Created by ether on 2018/9/26.
//

#include <GLES2/gl2.h>
#include "Rect.h"
#include "GLUtil.h"

GLUtil glUtil;

void Rect::bindData(const GLchar *vertexShaderCode, const GLchar *fragShaderCode) {
    GLuint POSITION_COUNT = 2;
    GLuint TEXTURE_COUNT = 2;
    GLuint STRIDE = (POSITION_COUNT + TEXTURE_COUNT) * 4;
    glUtil.createProgram(vertexShaderCode, fragShaderCode);
    aPositionLocation = glGetAttribLocation(glUtil.program, "a_Position");
    aTexCoordLocation = glGetAttribLocation(glUtil.program, "a_TextureCoordinates");
    uTexYLocation = glGetUniformLocation(glUtil.program, "u_TexY");
    uTexULocation = glGetUniformLocation(glUtil.program, "u_TexU");
    uTexVLocation = glGetUniformLocation(glUtil.program, "u_TexV");
    glVertexAttribPointer(0, POSITION_COUNT, GL_FLOAT, GL_FALSE, STRIDE, vertexData);
    glVertexAttribPointer(POSITION_COUNT, TEXTURE_COUNT, GL_FLOAT, GL_FALSE, STRIDE, vertexData);
    glEnableVertexAttribArray(aPositionLocation);
    glEnableVertexAttribArray(aTexCoordLocation);
}

void Rect::setUniform(int textureID, GLbyte buffer[], int type) {
//    glActiveTexture(GL_TEXTURE0 + type);
//    glBindTexture(GL_TEXTURE_2D, textureID);
//    if (type == 0) {
//        glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,)
//    } else {
//
//    }
}

void Rect::draw() {
    glUseProgram(glUtil.program);
    glDrawArrays(GL_TRIANGLES, 0, 6);
}

Rect::Rect() {

}