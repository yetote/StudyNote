attribute vec2 a_Position;
attribute vec2 a_TextureCoordinates;
varying vec2 v_TextureCoordinates;

void main() {
    gl_Position =vec4(a_Position,0,0);
    v_TextureCoordinates=a_TextureCoordinates;
}
