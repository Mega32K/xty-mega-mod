#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float TotalTime;

out vec4 fragColor;

mat4 saturationMatrix(float saturation) {
    vec3 luminance = vec3(0.3086, 0.6094, 0.0820);
    float oneMinusSat = 1.0 - saturation;

    vec3 red = vec3(luminance.x * oneMinusSat);
    red += vec3(saturation, 0.0, 0.0);

    vec3 green = vec3(luminance.y * oneMinusSat);
    green += vec3(0.0, saturation, 0.0);

    vec3 blue = vec3(luminance.z * oneMinusSat);
    blue += vec3(0.0, 0.0, saturation);

    return mat4(
    red, 0.0,
    green, 0.0,
    blue, 0.0,
    0.0, 0.0, 0.0, 1.0
    );
}

void main() {
    vec4 currTexel = texture(DiffuseSampler, texCoord);
    vec3 filterColor = vec3(0.85, 0.85, 1.1);
    float filterAlpha = clamp(2.0 - TotalTime / 5.0, 0.0, 1.0);

    fragColor = saturationMatrix(clamp(3.2 - filterAlpha * 3.2, 0.2, 1.0)) * currTexel * vec4(mix(vec3(1.0), filterColor, filterAlpha), 1.0);
}
