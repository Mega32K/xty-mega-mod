#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;

out vec4 fragColor;
const vec2 v1 = vec2(-1.0, 1.0);
const vec2 v2 = vec2(1.0, 0.0);

void main() {
    vec4 color = texture(Sampler0, texCoord0 * v1 + v2);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
