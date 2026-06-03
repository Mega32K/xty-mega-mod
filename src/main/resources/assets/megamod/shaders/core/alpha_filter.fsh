#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float Gray;

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    float f = color.a * Gray;
    fragColor = vec4(f, f, f, color.a) * ColorModulator;
}
