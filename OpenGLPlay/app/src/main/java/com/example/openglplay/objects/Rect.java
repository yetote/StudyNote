package com.example.openglplay.objects;

import com.example.openglplay.data.VertexArray;
import com.example.openglplay.programs.RectProgram;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;

/**
 * @author yetote QQ:503779938
 * @name OpenGLPlay
 * @class name：com.example.openglplay.objects
 * @class describe
 * @time 2018/9/17 16:26
 * @change
 * @chang time
 * @class describe
 */
public class Rect {
    private static final int VERTEX_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    private static final int STRIDE = (VERTEX_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * 4;
    private VertexArray vertexArray;

    public Rect() {
        //x,y,u,v
        float[] vertexData = {
                //x,y,s,t
                -1, -1, 0, 0,
                1, -1, 1, 0,
                1, 1, 1, 1,

                1, 1, 1, 1,
                -1, 1, 0, 1,
                -1, -1, 0, 0
        };
        vertexArray = new VertexArray(vertexData);
    }

    public void bindData(RectProgram program) {
        vertexArray.setVertexAttributePointer(0, program.getAttrPositionLocation(), VERTEX_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttributePointer(VERTEX_COMPONENT_COUNT, program.getAttrTexCoordLocation(), TEXTURE_COMPONENT_COUNT, STRIDE);
    }

    public void onDraw() {
        glDrawArrays(GL_TRIANGLES, 0, 1);
        glDrawArrays(GL_TRIANGLES, 3, 1);

    }
}
