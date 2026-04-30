#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float TotalTime;

out vec4 fragColor;

float easeOutCubic(float value) {
    float x = clamp(value, 0.0, 1.0);
    float inverse = 1.0 - x;
    return 1.0 - inverse * inverse * inverse;
}

float easeInOutSine(float value) {
    float x = clamp(value, 0.0, 1.0);
    return -(cos(3.14159265 * x) - 1.0) / 2.0;
}

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
    float fade = easeOutCubic(TotalTime / 0.45);
    float desaturationFade = smoothstep(0.0, 4.0, TotalTime);
    float blackProgress = easeInOutSine((TotalTime - 1.4) / 1.0);

    float saturation = mix(1.0, 0.126, desaturationFade) * mix(1.0, 0.85, fade);
    saturation = mix(saturation, 0.0, blackProgress);
    vec4 desaturated = saturationMatrix(saturation) * currTexel;
    vec3 darkened = desaturated.rgb * mix(1.0, 0.58, fade);

    vec2 centered = texCoord * 2.0 - 1.0;
    //centered.x *= InSize.x / max(InSize.y, 1.0);
    float edge = smoothstep(0.88, 1.18, length(centered) * (1.1 + min(TotalTime / 3.0F, 0.4)));

    vec3 deepRed = vec3(0.34, 0.0, 0.025);
    float redAlpha = mix(0.18, 0.48, edge) * fade;
    vec3 color = mix(darkened, deepRed, redAlpha);
    color *= mix(1.0, 0.55, edge * fade);

    float vignetteMask = smoothstep(0.56, 1.22, length(centered));
    color = mix(color, vec3(0.0), vignetteMask * mix(0.24, 0.42, fade));

    float blackRadius = mix(1.42, -0.12, blackProgress);
    float spreadingBlackMask = smoothstep(blackRadius - 0.36, blackRadius + 0.1, length(centered));
    float blackMask = max(vignetteMask * blackProgress, spreadingBlackMask);
    color = mix(color, vec3(0.0), blackMask);

    fragColor = vec4(color, currTexel.a);
}
