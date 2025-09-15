#version 330 core
#extension GL_ARB_explicit_uniform_location : require

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 UV0;
// Instanced
layout (location = 2) in vec3 StarPos;
layout (location = 3) in vec4 Color;
layout (location = 4) in float Rotation;
layout (location = 5) in float Size;
layout (location = 6) in float MaxDistance;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform vec3 RelativeSpaceLy;
uniform vec3 RelativeSpaceKm;

uniform mat3 LensingMat;
uniform mat3 LensingMatInv;
uniform float LensingIntensity;

const float DEFAULT_DISTANCE = 100;
const float MAX_SIZE = 50;
const float MAX_ALPHA = 0.025;

const float KM_PER_LY = 9460730472581.2;

out vec2 texCoord0;
out vec4 vertexColor;

float clampDustCloud(float size, float distance)
{
	float minSize = size * 0.04;

	size = 100000 * size / distance;
	
	if(size > MAX_SIZE)
		return MAX_SIZE;
	
	return size < minSize ? minSize : size;
}

float clampAlpha(float alpha, float distance)
{
	float minAlpha = alpha * 0.005;
	
	// Stars appear dimmer the further away they are
	//alpha -= distance / 100000;
	alpha = 100000 * alpha / distance;
	
	if(alpha < minAlpha)
		return minAlpha;
		
	return alpha > MAX_ALPHA ? MAX_ALPHA : alpha;
}

void main()
{
	vec3 xyz = vec3(StarPos.x - RelativeSpaceLy.x - RelativeSpaceKm.z / KM_PER_LY, StarPos.y - RelativeSpaceLy.y - RelativeSpaceKm.z / KM_PER_LY, StarPos.z - RelativeSpaceLy.z - RelativeSpaceKm.z / KM_PER_LY);
	
	float distance = sqrt(xyz.x * xyz.x + xyz.y * xyz.y + xyz.z * xyz.z);
	
	// COLOR START - Adjusts the brightness (alpha) of the star based on its distance
	
	float alpha = Color.w;
	alpha = clampAlpha(alpha, distance);
	
	// COLOR END
	
	float starSize = clampDustCloud(Size * 4, distance);
	
	// Normalize
	xyz /= distance;
	
	if(LensingIntensity > 1.0)
		xyz = LensingMat * xyz;
	
	/* These very obviously represent Spherical Coordinates (r, theta, phi)
	 * 
	 * Spherical equations (adjusted for Minecraft, since usually +Z is up, while in Minecraft +Y is up):
	 * 
	 * r = sqrt(x * x + y * y + z * z)
	 * tetha = arctg(x / z)
	 * phi = arccos(y / r)
	 * 
	 * x = r * sin(phi) * sin(theta)
	 * y = r * cos(phi)
	 * z = r * sin(phi) * cos(theta)
	 * 
	 * Polar equations
	 * z = r * cos(theta)
	 * x = r * sin(theta)
	 */
	float sphericalTheta = atan(xyz.x, xyz.z);
	float sinTheta = sin(sphericalTheta);
	float cosTheta = cos(sphericalTheta);
	
	float xzLength = sqrt(xyz.x * xyz.x + xyz.z * xyz.z);
	float sphericalPhi = atan(xzLength, xyz.y);
	float sinPhi = sin(sphericalPhi);
	float cosPhi = cos(sphericalPhi);
	
	float sinRotation = sin(Rotation);
	float cosRotation = cos(Rotation);
	
	float height = (Position.x * cosRotation - Position.y * sinRotation) * starSize;
	float width = (Position.y * cosRotation + Position.x * sinRotation) * starSize;
	if(LensingIntensity > 1.0)
	{
		float lensingAmount = cosPhi * LensingIntensity;
		if(lensingAmount > 1.0)
			width = lensingAmount * width;
	}
	
	float heightProjectionY = height * sinPhi;
	float heightProjectionXZ = - height * cosPhi;
	
	/* 
	 * projectedX:
	 * Projected height is projected onto the X-axis using sin(theta) and then gets subtracted (added because it's already negative)
	 * Width is projected onto the X-axis using cos(theta) and then gets subtracted
	 * 
	 * projectedZ:
	 * Width is projected onto the Z-axis using sin(theta)
	 * Projected height is projected onto the Z-axis using cos(theta) and then gets subtracted (added because it's already negative)
	 * 
	 */
	float projectedX = heightProjectionXZ * sinTheta - width * cosTheta;
	float projectedZ = width * sinTheta + heightProjectionXZ * cosTheta;
		
	// This effectively pushes the Star away from the camera
	// It's better to have them very far away, otherwise they will appear as though they're shaking when the Player is walking
	xyz *= DEFAULT_DISTANCE;
	
	vec3 pos = vec3(projectedX + xyz.x, heightProjectionY + xyz.y, projectedZ + xyz.z);
	
	if(LensingIntensity > 1.0)
		pos = LensingMatInv * pos;
	
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

	vertexColor = vec4(Color.x, Color.y, Color.z, alpha);
    texCoord0 = UV0;
}
