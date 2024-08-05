package net.povstalec.stellarview.client.resourcepack;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class StarInfo
{
	private static final float DEFAULT_DISTANCE = 100.0F;
	
	private double[][] starCoords;
	private double[][] starSizes;
	
	private double deformations[][];
	
	private short[][] starRGBA;
	
	private double[][] randoms;
	
	public StarInfo(int stars)
	{
		this.starCoords = new double[stars][3];
		this.starSizes = new double[stars][3];

		this.deformations = new double[stars][2];
		
		this.randoms = new double[stars][2];
		
		this.starRGBA = new short[stars][4];
	}
	
	public static double clampStar(double starSize, double minStarSize, double maxStarSize)
	{
		//System.out.println(minStarSize + " < " + starSize + " < " + maxStarSize);
		
		if(starSize < minStarSize)
			return minStarSize;
		
		return starSize > maxStarSize ? maxStarSize : starSize;
	}
	
	/**
	 * Creates information for a completely new star
	 * @param builder BufferBuilder used for building the vertexes
	 * @param randomSource RandomSource used for randomizing the star information
	 * @param relativeCoords SpaceCoords that give a relative position between the observer and the star
	 * @param x X coordinate of the star
	 * @param y Y coordinate of the star
	 * @param z Z coordinate of the star
	 * @param i Index of the star
	 */
	public void newStar(BufferBuilder builder, RandomSource randomSource, SpaceCoords viewCenterCoords, double x, double y, double z, int i)
	{
		long seed = randomSource.nextLong();
		
		// Set up position
		
		starCoords[i][0] = x;
		starCoords[i][1] = y;
		starCoords[i][2] = z;
		
		Star.Type spectralType = Star.Type.randomSpectralType(seed);
		
		//TODO Set up deformation
		deformations[i][0] = 1; // Height deformation
		deformations[i][1] = 1;// Width deformation

		// Set up size
		
		starSizes[i][0] = (double) spectralType.randomSize(seed); // This randomizes the Star size
		starSizes[i][1] = StellarViewConfig.distance_star_size.get() ? starSizes[i][0] : 0.2 + starSizes[i][0] * 1 / 5;
		starSizes[i][2] = StellarViewConfig.distance_star_size.get() ? starSizes[i][0] : starSizes[i][0] * 3 / 5;
		//starSizes[i][0] = Mth.clamp(starSize * 200000 * distance, TextureLayer.MIN_VISUAL_SIZE, maxStarSize);
		
		// Set up color and alpha
		
		short alpha = spectralType.randomBrightness(seed); // 0xAA is the default
		
		this.starRGBA[i] = StellarViewConfig.uniform_star_color.get() ? new short[] {255, 255, 255, alpha} : 
			new short[] {spectralType.red(), spectralType.green(), spectralType.blue(), alpha};
		
		// sin and cos are used to effectively clamp the random number between two values without actually clamping it,
		// wwhich would result in some awkward lines as Stars would be brought to the clamped values
		// Both affect Star size and rotation
		double random = randomSource.nextDouble() * Math.PI * 2.0D;
		randoms[i][0] = Math.sin(random); // sin random
		randoms[i][1] = Math.cos(random); // cos random
		
		this.createStar(builder, randomSource, viewCenterCoords, i);
	}
	
	public void createStar(BufferBuilder builder, RandomSource randomSource, SpaceCoords viewCenterCoords, int i)
	{
		double x = starCoords[i][0] - viewCenterCoords.x().toLy();
		double y = starCoords[i][1] - viewCenterCoords.y().toLy();
		double z = starCoords[i][2] - viewCenterCoords.z().toLy();
		
		double distance = x * x + y * y + z * z; // Distance squared
		
		short alpha = starRGBA[i][3];
		// Makes stars less bright the further away they are
		if(StellarViewConfig.distance_star_brightness.get())
		{
			short minAlpha = (short) ((alpha - 0xAA) * 2 / 3);
			
			if(distance > 40)
				alpha -= 2 * (int) Math.round(distance); //TODO Change this so it works with new distances
			
			if(alpha < minAlpha)
				alpha = minAlpha;
		}

		distance = 1.0D / Math.sqrt(distance); // Regular distance
		x *= distance;
		y *= distance;
		z *= distance;
		
		// This effectively pushes the Star away from the camera
		// It's better to have them very far away, otherwise they will appear as though they're shaking when the Player is walking
		double starX = x * DEFAULT_DISTANCE;
		double starY = y * DEFAULT_DISTANCE;
		double starZ = z * DEFAULT_DISTANCE;
		
		double starSize = clampStar(starSizes[i][0] * 200000 * distance, TextureLayer.MIN_VISUAL_SIZE, starSizes[i][1]);
		
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
		double sphericalTheta = Math.atan2(x, z);
		double sinTheta = Math.sin(sphericalTheta);
		double cosTheta = Math.cos(sphericalTheta);
		
		double xzLength = Math.sqrt(x * x + z * z);
		double sphericalPhi = Math.atan2(xzLength, y);
		double sinPhi = Math.sin(sphericalPhi);
		double cosPhi = Math.cos(sphericalPhi);
		
		double sinRandom = randoms[i][0];
		double cosRandom = randoms[i][1];
		
		// This loop creates the 4 corners of a Star
		for(int j = 0; j < 4; ++j)
		{
			/* Bitwise AND is there to multiply the size by either 1 or -1 to reach this effect:
			 * Where a coordinate is written as (A,B)
			 * 		(-1,1)		(1,1)
			 * 		x-----------x
			 * 		|			|
			 * 		|			|
			 * 		|			|
			 * 		|			|
			 * 		x-----------x
			 * 		(-1,-1)		(1,-1)
			 * 								|	A	B
			 * 0 & 2 = 000 & 010 = 000 = 0	|	x
			 * 1 & 2 = 001 & 010 = 000 = 0	|	x	x
			 * 2 & 2 = 010 & 010 = 010 = 2	|	x	x
			 * 3 & 2 = 011 & 010 = 010 = 2	|	x	x
			 * 4 & 2 = 100 & 000 = 000 = 0	|		x
			 * 
			 * After you subtract 1 one from each of them, you get this:
			 * j:	0	1	2	3
			 * --------------------
			 * A:	-1	-1	1	1
			 * B:	-1	1	1	-1
			 * Which corresponds to:
			 * UV:	00	01	11	10
			 */
			double aLocation = (double) ((j & 2) - 1) * starSize;
			double bLocation = (double) ((j + 1 & 2) - 1) * starSize;
			
			/* These are the values for cos(random) = sin(random)
			 * (random is simply there to randomize the star rotation)
			 * j:	0	1	2	3
			 * -------------------
			 * A:	0	-2	0	2
			 * B:	-2	0	2	0
			 * 
			 * A and B are there to create a diamond effect on the Y-axis and X-axis respectively
			 * (Pretend it's not as stretched as the slashes make it look)
			 * Where a coordinate is written as (B,A)
			 * 
			 *           (0,2)
			 *          /\
			 *   (-2,0)/  \(2,0)
			 *         \  /
			 *          \/
			 *           (0,-2)
			 * 
			 */
			double height = deformations[i][0] * (aLocation * cosRandom - bLocation * sinRandom);
			double width = deformations[i][1] * (bLocation * cosRandom + aLocation * sinRandom);
			//double width = starWidthFunction(aLocation, bLocation, sinRandom, cosRandom, sinTheta, cosTheta, sinPhi, cosPhi);
			
			double heightProjectionY = height * sinPhi; // Y projection of the Star's height
			
			double heightProjectionXZ = - height * cosPhi; // If the Star is angled, the XZ projected height needs to be subtracted from both X and Z 
			
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
			double projectedX = heightProjectionXZ * sinTheta - width * cosTheta;
			double projectedZ = width * sinTheta + heightProjectionXZ * cosTheta;
			
			builder.vertex(starX + projectedX, starY + heightProjectionY, starZ + projectedZ).color(starRGBA[i][0], starRGBA[i][1], starRGBA[i][2], alpha).endVertex();
		}
	}
}
