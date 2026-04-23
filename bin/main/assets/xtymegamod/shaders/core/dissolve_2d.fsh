#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float Dissolve;

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

void main() {
    if (Dissolve > 0.001 && (texture(Sampler1, texCoord0)).r < Dissolve){
        discard;
    }
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1)
        discard;
    fragColor = color * ColorModulator;
}
