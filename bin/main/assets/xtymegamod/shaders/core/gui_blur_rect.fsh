#version 150

#define MAX_RADIUS 32

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform vec2 TextureSize;
uniform vec2 RectMin;
uniform vec2 RectMax;
uniform vec4 RectColor;
uniform float BlurRadius;

in vec2 texCoord0;

out vec4 fragColor;

vec4 sampleBlurred(vec2 uv) {
    float radiusValue = clamp(BlurRadius, 0.0, float(MAX_RADIUS));
    int radius = int(radiusValue + 0.5);
    if (radius <= 0) {
        return texture(Sampler0, clamp(uv, RectMin, RectMax));
    }

    vec2 texel = 1.0 / TextureSize;
    float sigma = max(radiusValue * 0.5, 1.0);
    float denominator = 2.0 * sigma * sigma;
    vec4 sum = vec4(0.0);
    float weightSum = 0.0;

    for (int sampleY = -MAX_RADIUS; sampleY <= MAX_RADIUS; sampleY++) {
        for (int sampleX = -MAX_RADIUS; sampleX <= MAX_RADIUS; sampleX++) {
            if (abs(sampleX) <= radius && abs(sampleY) <= radius) {
                vec2 offset = vec2(float(sampleX), float(sampleY));
                float weight = exp(-dot(offset, offset) / denominator);
                vec2 sampleUv = clamp(uv + offset * texel, RectMin, RectMax);
                sum += texture(Sampler0, sampleUv) * weight;
                weightSum += weight;
            }
        }
    }

    return sum / max(weightSum, 0.0001);
}

void main() {
    vec4 tint = RectColor * ColorModulator;
    vec4 blurred = sampleBlurred(texCoord0);
    vec3 mixedColor = mix(blurred.rgb, tint.rgb, tint.a);
    fragColor = vec4(mixedColor, 1.0);
}
