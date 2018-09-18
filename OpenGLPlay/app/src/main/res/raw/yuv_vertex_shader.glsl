precision mediump float;
uniform sampler2D u_TexY;
uniform sampler2D u_TexU;
uniform sampler2D u_TexV;
varying vec2 v_TextureCoordinates;

void main() {
    vec3 yuv;
    vec3 rgb;
    yuv.r = texture2D(yTexture, v_TextureCoordinates).r;
    yuv.g = texture2D(uTexture, v_TextureCoordinates).r - 0.5;
    uv.b = texture2D(vTexture, v_TextureCoordinates).r - 0.5;
    gb = mat3(1.0, 1.0, 1.0,
              0.0, -0.39465, 2.03211,
              1.13983, -0.58060, 0.0) * yuv;
    gl_FragColor = vec4(rgb, 1.0);

}
