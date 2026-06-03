#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    float luminance = dot(color.rgb, vec3(0.3086, 0.6094, 0.0820));
    vec3 desaturated = mix(vec3(luminance), color.rgb, 0.2);
    fragColor = vec4(desaturated, color.a) * ColorModulator;
}
