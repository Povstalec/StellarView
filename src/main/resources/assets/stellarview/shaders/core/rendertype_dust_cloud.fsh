#version 150

uniform vec4 ColorModulator;
uniform vec2 DustCloudParams;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float dustCloud(vec2 uv) {
    float r = length(uv - vec2(0.5, 0.5));
    if (r <= DustCloudParams.x) {
        return 1.0;
    }

    float x = r - DustCloudParams.x;
    return exp(-x * x / (2.0 * DustCloudParams.y * DustCloudParams.y));
}

void main() {
    vec4 color = dustCloud(texCoord0) * vertexColor;
    if (color.a < 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
