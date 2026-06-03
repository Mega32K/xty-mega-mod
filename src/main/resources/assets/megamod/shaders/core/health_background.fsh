#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float _ProgramTime;
uniform vec4 Color;

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

mat2 rotate(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

void main() {
    float t = _ProgramTime * 0.2;
    vec2 uv = (texCoord0 * 8.0 + 10.0) * rotate(t * -0.1);
    float id = mod(floor(uv.x) + floor(uv.y), 2.0);
    float f = smoothstep(-0.6, 0.6, cos(fract(t * (id * 2.0 - 1.0) + id * 0.5) * 3.1415));
    vec2 guv = (fract(uv) - 0.5) * (cos(fract(t + id * 0.5) * 6.282) * 0.5 + 1.5) * rotate(f * 1.5707);
    fragColor = Color * (length(max(abs(guv) - 0.25, 0.0)) < 0.1 ? 1.0 : 0.6);
}
