#version 150

in vec3 StarPos;
in vec4 Color;
in vec3 HeightWidthSize;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 RelativeSpaceLy;
uniform vec3 RelativeSpaceKm;

uniform mat3 LensingMat;
uniform mat3 LensingMatInv;
uniform float LensingIntensity;

float DEFAULT_DISTANCE = 100;
float MIN_STAR_SIZE = 0.02;

out vec4 vertexColor;

float clampStar(float starSize, float distance)
{
	//float maxStarSize = 0.2 + starSize / 5;
	
	starSize -= starSize * distance / 1000000.0;
	
	if(starSize < MIN_STAR_SIZE)
		return MIN_STAR_SIZE;
	
	return starSize;// > maxStarSize ? maxStarSize : starSize;
}

void main()
{
	vec3 xyz = vec3(StarPos.x - RelativeSpaceLy.x, StarPos.y - RelativeSpaceLy.y, StarPos.z - RelativeSpaceLy.z);
	
	float distance = sqrt(xyz.x * xyz.x + xyz.y * xyz.y + xyz.z * xyz.z);
	
	// COLOR START - Adjusts the brightness (alpha) of the star based on its distance
	
	float alpha = Color.w;
	float minAlpha = alpha * 0.1;
	
	// Stars appear dimmer the further away they are
	alpha -= distance / 100000;
	
	if(alpha < minAlpha)
	{
		alpha = minAlpha;
		
		/*if(distance > 3000000)
		{
			if(minAlpha < 0.08)
			{
				if(distance < 4000000)
				{
					alpha = ( minAlpha * (4000000 - distance) ) / 1000000;
					
					if(alpha < 0)
						alpha = 0;
				}
				else
					alpha = 0;
			}
			else
			{
				float lowerAlpha = minAlpha * 0.5; // TODO This should ideally be a value provided for the vertex format
				
				if(distance < 4000000)
				{
					alpha = ( minAlpha * (4000000 - distance) ) / 1000000;
					
					if(alpha < lowerAlpha)
						alpha = lowerAlpha;
				}
				else
					alpha = lowerAlpha;
			}
		}*/
	}
	
	// COLOR END
	
	float starSize = clampStar(HeightWidthSize.z, distance);
	
	distance = 1.0 / distance;
	xyz.x *= distance;
	xyz.y *= distance;
	xyz.z *= distance;
	
	if(LensingIntensity > 0.0)
		xyz = LensingMat * xyz;
	
	// This effectively pushes the Star away from the camera
	// It's better to have them very far away, otherwise they will appear as though they're shaking when the Player is walking
	float starX = xyz.x * DEFAULT_DISTANCE;
	float starY = xyz.y * DEFAULT_DISTANCE;
	float starZ = xyz.z * DEFAULT_DISTANCE;
	
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
	float sinPhi = sin(sphericalPhi); //TODO These don't repeat so remove them
	float cosPhi = cos(sphericalPhi); //
	
	float height = HeightWidthSize.x * starSize;
	float width;
	if(LensingIntensity > 1.0)
	{
		float lensingAmount = cosPhi * LensingIntensity;
		width = lensingAmount  > 1.0 ? lensingAmount * HeightWidthSize.y * starSize :  HeightWidthSize.y * starSize;
	}
	else
		width = HeightWidthSize.y * starSize;
	
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
	
	vec3 pos =  LensingIntensity > 0.0 ? LensingMatInv * vec3(projectedX + starX, heightProjectionY + starY, projectedZ + starZ) : vec3(projectedX + starX, heightProjectionY + starY, projectedZ + starZ);
	
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
	
	vertexColor = vec4(Color.x, Color.y, Color.z, alpha);
}
