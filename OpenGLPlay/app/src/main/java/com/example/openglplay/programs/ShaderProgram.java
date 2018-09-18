package com.example.openglplay.programs;

import android.content.Context;

import com.example.openglplay.utils.ShaderHelper;
import com.example.openglplay.utils.TextRecourseReader;

import static android.opengl.GLES20.glUseProgram;

public class ShaderProgram {
    public static final String U_COLOR="u_Color";
    public static final String U_TIME="u_Time";
    public static final String U_MATRIX="u_Matrix";
    public static final String U_TEXTURE_UNIT="u_TextureUnit";
    public static final String U_TEXY="u_TexY";
    public static final String U_TEXU="u_TexU";
    public static final String U_TEXV="u_TexV";

    public static final String A_POSITION="a_Position";
    public static final String A_TEXTURE_COORDINATES="a_TextureCoordinates";
    public static final String A_COLOR="a_Color";
    public static final String A_DIRECTION_VECTOR="a_DirectionVector";
    public static final String A_PARTICLE_START_TIME="a_ParticleStartTime";


    public  final int program;

    public ShaderProgram(Context context,int vertexShaderRecourseId,int fragmentShaderRecourseId) {
        program= ShaderHelper.buildProgram(TextRecourseReader.readTextFileFromResource(context,vertexShaderRecourseId),
                TextRecourseReader.readTextFileFromResource(context,fragmentShaderRecourseId));

    }
    public void useProgram(){
        glUseProgram(program);
    }
}
