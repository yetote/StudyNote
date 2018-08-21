//
// Created by ether on 2018/8/21.
//

#ifndef OPENGLNATIVEDEMO_GLUTIL_H
#define OPENGLNATIVEDEMO_GLUTIL_H


class GLUtil {
public:
    static int compileShader(int type, const char *shaderCode);

    static int createProgram(const char *vertexShaderCode, const char *fragmentShaderCode);
};


#endif //OPENGLNATIVEDEMO_GLUTIL_H
