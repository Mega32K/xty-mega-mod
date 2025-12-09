#version 150

in vec3 Position;
in vec4 Color;
in vec2 ScreenSize;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec3 pos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    pos = Position;
    vertexColor = Color;
}