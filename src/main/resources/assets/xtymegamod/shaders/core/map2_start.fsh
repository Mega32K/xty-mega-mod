#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform vec2 Mouse;
uniform float _ProgramTime;

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

mat2 rotate(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c,-s,s,c);
}

void main() {
    float time = _ProgramTime * 0.2;
    vec2 uv = texCoord0;
    vec2 mouse = Mouse.xy;
    float degree = 2.5 + sin(time + time * 1.37) / 2.0;
    vec2 centerFactor = pow(abs((uv - 0.5) * 2.0), vec2(degree)) * 0.5;
    float centerFade = centerFactor.x + centerFactor.y;
    vec3 accentColor = vec3(0.59, 0.53, 0.38);
    float mouseEffectDistance = 0.75;
    float mouseDistanceModifier = pow(1.0 - min(distance(mouse, uv), mouseEffectDistance) / mouseEffectDistance, 3.0);
    vec2 mid1A = mod(uv * 2.0, 2.0) - 183.0;
    vec2 mid2A = vec2(mid1A);
    float intensityA = 0.003;
    float whimsyA = 1.0;
    vec2 mid1B = mod(uv * 3.0, 3.0) - 183.0;
    vec2 mid2B = vec2(mid1B);
    float intensityB = 0.002;
    float whimsyB = 1.0;
    for (int i = 0; i < 8; i++) {
        float timeA = (time * 0.1 + 23.0) * (1.0 - (3.5 / float(i + 1)));
        mid2A = mid1A + vec2(cos(timeA - mid2A.x) + sin(timeA + mid2A.y), sin(timeA - mid2A.y) + cos(timeA + mid2A.x));
        whimsyA += 1.0 / length(vec2(mid1A.x / (sin(mid2A.x + timeA) / intensityA), mid1A.y / (cos(mid2A.y + timeA) / intensityA)));

        float timeB = (time * 0.5 + 23.0) * (1.0 - (3.5 / float(i + 1)));
        mid2B = mid1B + vec2(cos(timeB - mid2B.x) + sin(timeB + mid2B.y), sin(timeB - mid2B.y) + cos(timeB + mid2B.x));
        whimsyB += 1.0 / length(vec2(mid1B.x / (sin(mid2B.x + timeB) / intensityB), mid1B.y / (cos(mid2B.y + timeB) / intensityB)));
    }
    whimsyA = 1.0 - pow(whimsyA / 8.0, 1.4);
    whimsyB = 1.2 - whimsyB / 8.0;
    vec3 restingColor = vec3(whimsyA * centerFade);
    vec3 activeColor = accentColor * vec3(clamp(whimsyB * mouseDistanceModifier, 0.0, 1.0));
    vec3 mixed = activeColor + restingColor;
    fragColor = vec4(mixed, 1.0) * ColorModulator;
}
