#version 330 core
#extension GL_ARB_explicit_uniform_location : require

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 UV0;
// Instanced
layout (location = 2) in vec3 offset;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position + offset, 1.0);

    texCoord0 = UV0;
    vertexColor = vec4(1.0, 1.0, 1.0, 1.0);
}
