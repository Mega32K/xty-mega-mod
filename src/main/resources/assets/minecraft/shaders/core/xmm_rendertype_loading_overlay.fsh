#version 150
#define layers 5 //int how many layers
#define speed .25 //float speed multiplyer
#define scale 1.2 //float scale multiplyer

in vec4 vertexColor;
in vec3 pos;

uniform vec4 ColorModulator;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;
uniform float TotalTime;

const vec3 v3_000 = vec3(0.0,0.0,0.0);
const vec3 v3_001 = vec3(0.0,0.0,1.0);
const vec3 v3_010 = vec3(0.0,1.0,0.0);
const vec3 v3_100 = vec3(1.0,0.0,0.0);
const vec3 v3_101 = vec3(1.0,0.0,1.0);
const vec3 v3_110 = vec3(1.0,1.0,0.0);
const vec3 v3_111 = vec3(1.0,1.0,1.0);
out vec4 fragColor;

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
void noiseBackground() {
    //normalized device coordinates from -1 to 1
    vec2 uv = (pos.xy / ScreenSize) * vec2(2.0) - vec2(1.0);
    //time value
    float t = TotalTime*speed;

    uv *= scale;
    float h = noise(vec3(uv*2.,t));
    //uv distortion loop
    for (int n = 1; n < layers; n++){
        float i = n * 1.0;
        uv -= vec2(0.7 / i * sin(i * uv.y+i + t*5. + h * i) + 0.8, 0.4 / i * sin(uv.x+4.-i+h + t*5. + 0.3 * i) + 1.6);
    }

    uv -= vec2(1.2 * sin(uv.x + t + h) + 1.8, 0.4 * sin(uv.y + t + 0.3*h) + 1.6);


    // Time varying pixel color
    vec3 col = vec3(.5 * cos(uv.x) + 0.5, .5 * cos(uv.x + uv.y) + 0.5, -.5 * cos(uv.y) + 0.8)*0.8;

    // Output to screen
    fragColor = vec4(col,vertexColor.a) * ColorModulator;
}
void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) discard;
    fragColor = color * ColorModulator;
    if (color.r == 239.0 / 255.0) {
        noiseBackground();
    }
}