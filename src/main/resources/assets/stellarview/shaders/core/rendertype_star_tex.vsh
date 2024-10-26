#version 150

in vec3 StarPos;
in vec4 Color;
in vec3 HeightWidthSize;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 RelativeSpaceLy;
uniform vec3 RelativeSpaceKm;

float DEFAULT_DISTANCE = 100;

out vec4 vertexColor;
out vec2 texCoord0;

float clampStar(float starSize, float distance)
{
	//float maxStarSize = 0.2 + starSize / 5;
	
	starSize -= starSize * distance / 1000000.0;
	
	if(starSize < 0.08)
		return 0.08;
	
	return starSize;// > maxStarSize ? maxStarSize : starSize;
}

void main() {
	float x = StarPos.x - RelativeSpaceLy.x;
	float y = StarPos.y - RelativeSpaceLy.y;
	float z = StarPos.z - RelativeSpaceLy.z;
	
	float distance = sqrt(x * x + y * y + z * z);
	
	// COLOR START - Adjusts the brightness (alpha) of the star based on its distance
	
	float alpha = Color.w;
	float minAlpha = alpha * 0.1; // Previously used (alpha - 0.66) * 2 / 3
	
	// Stars appear dimmer the further away they are
	alpha -= distance / 100000;
	
	if(alpha < minAlpha)
			alpha = minAlpha;
	
	// COLOR END
	
	float starSize = clampStar(HeightWidthSize.z * 4, distance);
	
	distance = 1.0 / distance;
	x *= distance;
	y *= distance;
	z *= distance;
	
	// This effectively pushes the Star away from the camera
	// It's better to have them very far away, otherwise they will appear as though they're shaking when the Player is walking
	float starX = x * DEFAULT_DISTANCE;
	float starY = y * DEFAULT_DISTANCE;
	float starZ = z * DEFAULT_DISTANCE;
	
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
	float sphericalTheta = atan(x, z);
	float sinTheta = sin(sphericalTheta);
	float cosTheta = cos(sphericalTheta);
	
	float xzLength = sqrt(x * x + z * z);
	float sphericalPhi = atan(xzLength, y);
	float sinPhi = sin(sphericalPhi); //TODO These don't repeat so remove them
	float cosPhi = cos(sphericalPhi); //
	
	float height = HeightWidthSize.x * starSize;
	float width = HeightWidthSize.y * starSize;
	
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
	
	vec3 pos = vec3(projectedX + starX, heightProjectionY + starY, projectedZ + starZ);
	
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
	
	vertexColor = vec4(Color.x, Color.y, Color.z, alpha);
    texCoord0 = UV0;
}
