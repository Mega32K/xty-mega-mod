#version 150
#define layers 5 //int how many layers
#define speed 0.22 //float speed multiplyer
#define scale 1.7 //float scale multiplyer
#define lineScale 1.5 //line effect scale
#define lineSpeed -0.035 //line effect speed
//#define totalLine 30.0; //approximate number of lines
//#define blackWhiteProportion 1.5; //blackline scale / whiteline scale;
//#define lineWidth 1.0; //width of white line;

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float _ProgramTime;

const vec3 v3_000 = vec3(0.0,0.0,0.0);
const vec3 v3_001 = vec3(0.0,0.0,1.0);
const vec3 v3_010 = vec3(0.0,1.0,0.0);
const vec3 v3_100 = vec3(1.0,0.0,0.0);
const vec3 v3_101 = vec3(1.0,0.0,1.0);
const vec3 v3_110 = vec3(1.0,1.0,0.0);
const vec3 v3_111 = vec3(1.0,1.0,1.0);

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;
vec2 rotateUV( vec2 uv ) {
    vec2 center = vec2(0.5);
    vec2 dir = uv - center;
    float s = -0.489;
    float c = 0.8727;
    return vec2(
    c * dir.x - s * dir.y,
    s * dir.x + c * dir.y
    ) + center;
}
vec3 hash( vec3 p )
{
    p = vec3( dot(p,vec3(127.1,311.7, 74.7)),
    dot(p,vec3(269.5,183.3,246.1)),
    dot(p,vec3(113.5,271.9,124.6)));
    p = -1.0 + 2.0*fract(sin(p)*43758.5453123);

    return p;
}
float noise( in vec3 p )
{
    vec3 i = floor( p );
    vec3 f = fract( p );

    vec3 u = f*f*(3.0-2.0*f);

    return 1.0;
}
vec4 lineColorMix( vec2 ouv, vec4 oColor , vec4 resultColor) {
    float time = _ProgramTime * lineSpeed;
    vec2 uv = rotateUV(ouv + vec2(time * 0.03, 0.0)) * lineScale;
    vec3 col = vec3(0.4);
    if (mod((time + uv.x) * 90.0, 1.5 * 1.0) > 1.0)
        return mix(vec4(mix(col, oColor.rgb, 0.9), oColor.a) * ColorModulator, resultColor, 0.9);
    return resultColor;
}
void noiseBackground(vec4 resultColor) {
    vec2 oUV = pos.xy / ScreenSize;
    //normalized device coordinates from -1 to 1
    vec2 uv = oUV * 2.0 - vec2(1.0);
    //time value
    float t = _ProgramTime*speed;

    uv *= scale;
    float h = noise(vec3(uv * 2.0, t));
    //uv distortion loop
    for (int n = 1; n < layers; n++){
        float i = n * 1.0;
        uv -= vec2(0.7 / i * sin(i * uv.y + i + t * 5.0 + h * i) + 0.8, 0.4 / i * sin(uv.x + 4.0 - i + h + t * 5.0 + 0.3 * i) + 1.6);
    }

    uv -= vec2(1.2 * sin(uv.x + t + h) + 1.8, 0.4 * sin(uv.y + t + 0.3*h) + 1.6);


    // Time varying pixel color
    vec3 col = vec3(0.5 * cos(uv.x) + 0.5, 0.5 * cos(uv.x + uv.y) + 0.5, -0.5 * cos(uv.y) + 0.8) * 0.8;

    // Output to screen
    fragColor = lineColorMix(oUV, vec4(col, 1.0), resultColor);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    if (color.r == 10.0 / 51.0 && color.g == 36.0 / 255.0) {
        noiseBackground(color);
    } else fragColor = color * ColorModulator;
}
