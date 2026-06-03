#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float currentAspect = InSize.x / max(InSize.y, 1.0);
    float targetAspect = 4.0 / 3.0;
    float horizontalScale = targetAspect / currentAspect;
    vec2 sampleUv = vec2(0.5 + (texCoord.x - 0.5) * horizontalScale, texCoord.y);
    sampleUv = clamp(sampleUv, vec2(0.0), vec2(1.0));
    fragColor = texture(DiffuseSampler, sampleUv);
}
