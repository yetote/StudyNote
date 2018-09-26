//
// Created by ether on 2018/9/26.
//
#include <GLES2/gl2.h>

#ifndef NDKPLAYER_GLUTIL_H
#define NDKPLAYER_GLUTIL_H


class GLUtil {
public:
    GLuint program;

    void createProgram(const GLchar *vertexShaderCode, const GLchar *fragmentShaderCode);

private:
    GLuint compileShader(GLenum type, const GLchar *shaderCode);

};


#endif //NDKPLAYER_GLUTIL_H
