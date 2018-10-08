//
// Created by ether on 2018/8/21.
//

#ifndef OPENGLNATIVEDEMO_GLUTIL_H
#define OPENGLNATIVEDEMO_GLUTIL_H


class GLUtil {
public:
    static int compileShader(int type, const char *shaderCode);

    static GLint createProgram(const char *vertexShaderCode, const char *fragmentShaderCode);
    static GLuint* createTexture();
};


#endif //OPENGLNATIVEDEMO_GLUTIL_H
