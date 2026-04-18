#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float _ProgramTime;
uniform vec3 OutlineColor;

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

vec4 fadey(float alpha) {
    //normalized device coordinates from -1 to 1
    vec2 uv = /*(pos.xy / ScreenSize)*/ texCoord0 * 2.0 - vec2(1.0);
    float time = _ProgramTime * 10.0;
    vec3 color = vec3(uv,0.5+0.5*sin(time*0.4));

    color.xyz *= 0.3;
    color.xyz += 0.8;
    return vec4(color, alpha) * ColorModulator;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.rgb == OutlineColor.rgb) {
        fragColor = fadey(color.a);
    } else fragColor = color * ColorModulator;
}