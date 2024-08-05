#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

mat4 matrix = mat4(	1.0, 0.0, 0.0, 0.0,
					0.0, 1.0, 0.0, 0.0,
					0.0, 0.0, 1.0, 0.0,
					0.0, 0.0, 0.0, 1.0);

out vec4 vertexColor;

void main() {
	
	gl_Position = ProjMat * ModelViewMat * matrix * vec4(Position, 1.0);

    vertexColor = Color;
}
