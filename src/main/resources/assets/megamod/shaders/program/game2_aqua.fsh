#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float TotalTime;

out vec4 fragColor;

vec3 applySaturation(vec3 color, float saturation) {
    float luminance = dot(color, vec3(0.3086, 0.6094, 0.0820));
    return mix(vec3(luminance), color, saturation);
}

void main() {
    vec4 currTexel = texture(DiffuseSampler, texCoord);
    vec3 filterColor = vec3(0.85, 0.85, 1.1);
    float filterAlpha = clamp(2.0 - TotalTime / 5.0, 0.0, 1.0);
    float saturation = clamp(3.2 - filterAlpha * 3.2, 0.2, 1.0);
    vec3 filter = mix(vec3(1.0), filterColor, filterAlpha);

    fragColor = vec4(applySaturation(currTexel.rgb, saturation) * filter, currTexel.a);
}
