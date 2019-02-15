//
// Created by ether on 2018/10/24.
//

#ifndef BAMBOO_GLUTIL_H
#define BAMBOO_GLUTIL_H

#include <GLES2/gl2.h>
#include <android/log.h>

class GLUtil {
public:
    GLuint program;

    void createProgram(const char *vertexShaderCode, const char *fragShaderCode);

    GLuint *createTexture();
    void destroy();
private:
    GLuint loadShader(GLenum type, const char *shaderCode);
};


#endif //BAMBOO_GLUTIL_H
