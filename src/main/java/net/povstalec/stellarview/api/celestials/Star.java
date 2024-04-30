package net.povstalec.stellarview.api.celestials;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class Star
{
	private static final float DEFAULT_DISTANCE = 100.0F;
	
	/**
	 * Returns the brightness of stars in the current Player location
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float getStarBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float rain = 1.0F - level.getRainLevel(partialTicks);
		float starBrightness = level.getStarBrightness(partialTicks);
		starBrightness = StellarViewConfig.day_stars.get() && starBrightness < 0.5F ? 0.5F : starBrightness;
		if(StellarViewConfig.bright_stars.get())
			starBrightness = starBrightness * (1 + ((float) (15 - level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15)) / 15));
		starBrightness = starBrightness * rain;
		
		return starBrightness;
	}
	
	public static void createStar(BufferBuilder builder, RandomSource randomsource, 
			double x, double y, double z, double starSize, double distance, int[] starColor, double heightDeformation, double widthDeformation)
	{
		distance = 1.0D / Math.sqrt(distance);
		x *= distance;
		y *= distance;
		z *= distance;
		
		// This effectively pushes the Star away from the camera
		// It's better to have them very far away, otherwise they will appear as though they're shaking when the Player is walking
		double starX = x * DEFAULT_DISTANCE;
		double starY = y * DEFAULT_DISTANCE;
		double starZ = z * DEFAULT_DISTANCE;
		
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
		
		// sin and cos are used to effectively clamp the random number between two values without actually clamping it,
		// wwhich would result in some awkward lines as Stars would be brought to the clamped values
		// Both affect Star size and rotation
		double random = randomsource.nextDouble() * Math.PI * 2.0D;
		double sinRandom = Math.sin(random);
		double cosRandom = Math.cos(random);
		
		if(starColor.length < 3)
			starColor = new int[] {255, 255, 255};
		
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
			double aLocation = (double) ((j & 2) - 1) * Mth.clamp(starSize * 20 * distance, 0.1, 0.25); //starSize;
			double bLocation = (double) ((j + 1 & 2) - 1) * Mth.clamp(starSize * 20 * distance, 0.1, 0.25); //starSize;
			
			/* These are the values for cos(random) = sin(random)
			 * (random is simply there to randomize the star rotation)
			 * j:	0	1	2	3
			 * -------------------
			 * A:	0	-2	0	2
			 * B:	-2	0	2	0
			 * 
			 * A and B are there to create a diamond effect on the Y-axis and X-axis respectively
			 * (Pretend it's not as stretched as the slashes make it looked)
			 * Where a coordinate is written as (B,A)
			 * 
			 * 			(0,2)
			 * 			/\
			 * 	 (-2,0)/  \(2,0)
			 * 		   \  /
			 * 			\/
			 * 			(0,-2)
			 * 
			 */
			double height = heightDeformation * (aLocation * cosRandom - bLocation * sinRandom);
			double width = widthDeformation * (bLocation * cosRandom + aLocation * sinRandom);
			
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
			
			builder.vertex(starX + projectedX, starY + heightProjectionY, starZ + projectedZ).color(starColor[0], starColor[1], starColor[2], 0xAA).endVertex();
		}
	}

	public static int[] randomStarColor(long seed, int contrast)
	{
		// This chooses a random color for a star based on a few select presets.
		RandomSource randomsource = RandomSource.create(seed);

		int[] starClassM = {255, 255-(contrast*4), 255-(contrast*4)};
		int[] starClassK = {255, 255-(contrast*2), 255-(contrast*4)};
		int[] starClassG = {255, 255, 255-(contrast*4)};
		int[] starClassF = {255, 255, 255};
		int[] starClassA = {255-(contrast*2), 255-(contrast*2), 255};
		int[] starClassB = {255-(contrast*4), 255-(contrast*4), 255};
		int[] starClassO = {255-(contrast*6), 255-(contrast*6), 255};

		int[][] starColors = {starClassM, starClassK, starClassG, starClassF, starClassA, starClassB, starClassO};
		int[] starColor = starColors[randomsource.nextInt(starColors.length)];

		return starColor;
	}
	
	public static void createStar(BufferBuilder builder, RandomSource randomsource, 
			double x, double y, double z, double starSize, double distance, int[] starColor)
	{
			createStar(builder, randomsource, x, y, z, starSize, distance, starColor, 1.0, 1.0);
	}
	
	public static void createVanillaStar(BufferBuilder builder, RandomSource randomsource, 
			double x, double y, double z, double starSize, double distance, int[] starColor)
	{
		if(distance < 1.0D && distance > 0.01D)
			createStar(builder, randomsource, x, y, z, starSize, distance, starColor);
	}
}
