#version 150

uniform vec4 ColorModulator;
uniform vec2 DustCloudParams;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32 + gl_FragCoord.xy);
    return fract(p.x * p.y);
}

float dustCloud(vec2 uv) {
    float r = length(uv - vec2(0.5, 0.5));

    float density;

    if (r <= DustCloudParams.x) {
        density = 1.0;
    } else {
        float x = r - DustCloudParams.x;
        density = exp(-x * x / (2.0 * DustCloudParams.y * DustCloudParams.y));
    }

    float noise = (hash(uv) - 0.5) * 0.25;

    return clamp(density + noise, 0.0, 1.0);
}

void main() {
    vec4 color = dustCloud(texCoord0) * vertexColor;
    if (color.a < 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
