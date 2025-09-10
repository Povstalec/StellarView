#version 330 core
#extension GL_ARB_explicit_uniform_location : enable

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;
// Instanced
layout (location = 2) in vec3 offset;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position + offset, 1.0);

    vertexColor = Color;
}
