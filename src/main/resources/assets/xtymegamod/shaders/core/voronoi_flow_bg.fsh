#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 Size;
uniform float _ProgramTime;
uniform vec4 ColorA;
uniform vec4 ColorB;
uniform vec4 ColorC;

in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 567.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

// ---------------------------------------------------------------
// 三色渐变（含 alpha） + 时间扰动
// ---------------------------------------------------------------

vec4 threeColorGradient(vec2 uv, float time)
{

    float t = sin(time * 0.8 + uv.x * 3.5) * 0.05;
    float k = clamp(uv.x + t, 0.0, 1.0);

    if (k < 0.5)
    return mix(ColorA, ColorB, smoothstep(0.0, 0.5, k));
    else
    return mix(ColorB, ColorC, smoothstep(0.5, 1.0, k));
}

// ====================================================================
// Voronoi with smooth temporal flow animation
// ====================================================================
vec2 fastVoronoiUV(vec2 fragCoord, float cellSize, float time)
{
    vec2 g  = fragCoord / cellSize;
    vec2 id = floor(g);
    vec2 f  = fract(g);

    float minDist = 1e9;
    vec2 bestID = vec2(0.0);

    // 搜索邻域（优化 3×3）
    for (int j = -2; j <= 2; j++) {
        for (int i = -2; i <= 2; i++) {

            vec2 offset = vec2(float(i), float(j));
            vec2 nid = id + offset;

            // 基础随机中心（固定 seed）
            vec2 rnd = vec2(hash21(nid), hash21(nid + 1.23));

            // ---------------------------
            // Voronoi 流动（flow）动画核心
            // ---------------------------
            float h = hash21(nid);
            vec2 flow = vec2(
            sin(time * 0.96 + h * 6.2831),
            cos(time * 0.7 + h * 5.1234)
            ) * 0.45;      // 流动幅度（可调）

            // 流动后的中心位置
            vec2 center = rnd + flow;

            // Voronoi 距离
            vec2 diff = offset + center - f;
            float d = dot(diff, diff);

            if (d < minDist) {
                minDist = d;
                bestID = nid + center;
            }
        }
    }

    // 转为 UV
    vec2 bestPixel = bestID * cellSize;
    return bestPixel / Size.xy;
}


void main()
{
    vec4 textureCol = texture(Sampler0, texCoord0);
    if (textureCol.a <= 0.0) discard;
    float time = _ProgramTime;

    // 可调参数
    float shortSide = min(Size.x, Size.y);
    float cellCount = 4096.0 / shortSide;
    float cellSize = shortSide / cellCount;

    // 获取动态流动的 Voronoi 中心 UV
    vec2 sampleUV = fastVoronoiUV(pos.xy, cellSize, time);

    // 三色渐变着色
    vec4 col = threeColorGradient(sampleUV, time);

    fragColor = vec4(col.rgb, textureCol.a);
}