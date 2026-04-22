#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float _ProgramTime;
uniform float GlitchStrength;

in vec2 texCoord0;

out vec4 fragColor;

float hash(float value) {
    return fract(sin(value * 127.1) * 43758.5453);
}

float bandNoise(float y, float time) {
    return hash(floor(y * 32.0) + floor(time * 18.0) * 7.0);
}

void main() {
    float strength = max(GlitchStrength, 0.0);
    float time = _ProgramTime;
    float band = bandNoise(texCoord0.y, time);
    float pulse = step(0.78, band) * sin(time * 28.0 + texCoord0.y * 80.0);
    float jitter = pulse * 0.008 * strength;
    float scan = step(0.985, fract(texCoord0.y * 48.0 + time * 8.0)) * 0.35 * strength;

    vec2 baseUv = clamp(texCoord0 + vec2(jitter, 0.0), vec2(0.0), vec2(1.0));
    vec4 base = texture(Sampler0, baseUv);
    vec4 redShift = texture(Sampler0, clamp(baseUv + vec2(0.004 * strength, 0.0), vec2(0.0), vec2(1.0)));
    vec4 blueShift = texture(Sampler0, clamp(baseUv - vec2(0.004 * strength, 0.0), vec2(0.0), vec2(1.0)));
    vec3 color = vec3(redShift.r, base.g, blueShift.b);

    color += scan * base.a;
    fragColor = vec4(color, base.a) * ColorModulator;
}
