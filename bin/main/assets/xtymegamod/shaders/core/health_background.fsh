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
    return mat2(c,-s,s,c);
}

void main() {
    float t = _ProgramTime * 0.2;
    vec2 uv = (texCoord0 * 8. + 10.) * rotate(t*-.1);
    float id = mod(floor(uv.x)+floor(uv.y),2.);
    float f = smoothstep(-.6,.6,cos(fract(t*(id*2.-1.)+id*.5)*3.1415));
    vec2 guv = (fract(uv)-.5)*(cos(fract(t+id*.5)*6.282)*.5+1.5)*rotate(f*1.5707);
    fragColor = Color*(length(max(abs(guv)-.25,0.)) < .1 ? 1. : .6);
}