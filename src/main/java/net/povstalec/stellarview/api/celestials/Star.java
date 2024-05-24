package net.povstalec.stellarview.api.celestials;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class Star
{
	private static final float DEFAULT_DISTANCE = 100.0F;
	private static final int CONTRAST = 8;
	
	private static final float MIN_STAR_SIZE = 0.15F;
	private static final float MAX_STAR_SIZE = 0.25F;
	
	private static final int MIN_STAR_BRIGHTNESS = 170; // 0xAA
	private static final int MAX_STAR_BRIGHTNESS = 255; // 0xFF
	
	public enum SpectralType
	{
		O(255 - (CONTRAST * 6), 255 - (CONTRAST * 6), 255, MAX_STAR_SIZE, MAX_STAR_SIZE, MAX_STAR_BRIGHTNESS, MAX_STAR_BRIGHTNESS), // 0.00003%
		B(255 - (CONTRAST * 4), 255 - (CONTRAST * 4), 255, MIN_STAR_SIZE + 0.09F, MAX_STAR_SIZE, MIN_STAR_BRIGHTNESS + 65, MAX_STAR_BRIGHTNESS), // 0.12%
		A(255 - (CONTRAST * 2), 255 - (CONTRAST * 2), 255, MIN_STAR_SIZE + 0.05F, MIN_STAR_SIZE + 0.07F, MIN_STAR_BRIGHTNESS + 45, MIN_STAR_BRIGHTNESS + 65), // 0.61%
		F(255, 255, 255, MIN_STAR_SIZE + 0.035F, MIN_STAR_SIZE + 0.05F, MIN_STAR_BRIGHTNESS + 30, MIN_STAR_BRIGHTNESS + 45), // 3.0%
		G(255, 255, 255 - (CONTRAST * 4), MIN_STAR_SIZE + 0.02F, MIN_STAR_SIZE + 0.035F, MIN_STAR_BRIGHTNESS + 20, MIN_STAR_BRIGHTNESS + 30), // 7.6%
		K(255, 255 - (CONTRAST * 2), 255 - (CONTRAST * 4), MIN_STAR_SIZE + 0.01F, MIN_STAR_SIZE + 0.02F, MIN_STAR_BRIGHTNESS + 10, MIN_STAR_BRIGHTNESS + 20), // 12%
		M(255, 255 - (CONTRAST * 4), 255 - (CONTRAST * 4), MIN_STAR_SIZE, MIN_STAR_SIZE + 0.01F, MIN_STAR_BRIGHTNESS, MIN_STAR_BRIGHTNESS + 10); // 76%
		
		private final int red;
		private final int green;
		private final int blue;

		private final float minSize;
		private final float maxSize;

		private final int minBrightness;
		private final int maxBrightness;
		
		SpectralType(int red, int green, int blue, float minSize, float maxSize, int minBrightness, int maxBrightness)
		{
			this.red = red;
			this.green = green;
			this.blue = blue;

			this.minSize = minSize;
			this.maxSize = maxSize;

			this.minBrightness = minBrightness;
			this.maxBrightness = maxBrightness;
		}
		
		public int red()
		{
			return red;
		}
		
		public int green()
		{
			return green;
		}
		
		public int blue()
		{
			return blue;
		}
		
		public float randomSize(long seed)
		{
			if(minSize == maxSize)
				return maxSize;
			
			Random random = new Random(seed);
			
			return random.nextFloat(minSize, maxSize);
		}
		
		public int randomBrightness(long seed)
		{
			if(minBrightness == maxBrightness)
				return maxBrightness;
			
			Random random = new Random(seed);
			
			return random.nextInt(minBrightness, maxBrightness);
		}
		
		
		
		public static SpectralType randomSpectralType(long seed)
		{
			Random random = new Random(seed);
			
			if(StellarViewConfig.equal_spectral_types.get())
			{
				SpectralType[] spectralTypes = SpectralType.values();
				return spectralTypes[random.nextInt(0, spectralTypes.length)];
			}
			
			int value = random.nextInt(0, 100);
			
			// Slightly adjusted percentage values that can be found in SpectralType comments
			if(value < 74)
				return M;
			else if(value < (74 + 12))
				return K;
			else if(value < (74 + 12 + 7))
				return G;
			else if(value < (74 + 12 + 7 + 3))
				return F;
			else if(value < (74 + 12 + 7 + 3 + 1))
				return A;
			else if(value < (74 + 12 + 7 + 3 + 1 + 1))
				return B;
			
			return O;
		}
	}
	
	public static double starWidthFunction(double aLocation, double bLocation, double sinRandom, double cosRandom, double sinTheta, double cosTheta, double sinPhi, double cosPhi)
	{
		double width = bLocation * cosRandom + aLocation * sinRandom;
		
		if(true && cosPhi  > 0.0)
			return cosPhi * 8 * width;
		
		return width;
	}
	
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
			double x, double y, double z, double distance, double heightDeformation, double widthDeformation, long seed)
	{
		SpectralType spectralType = SpectralType.randomSpectralType(seed);
		int[] starColor = StellarViewConfig.uniform_star_color.get() ? new int[] {255, 255, 255} : 
			new int[] {spectralType.red(), spectralType.green(), spectralType.blue()};

		double starSize = (double) spectralType.randomSize(seed); // This randomizes the Star size
		double maxStarSize = StellarViewConfig.distance_star_size.get() ? starSize : 0.2 + starSize * 1 / 5;
		double minStarSize = StellarViewConfig.distance_star_size.get() ? starSize : starSize * 3 / 5;
		
		int alpha = spectralType.randomBrightness(seed); // 0xAA is the default
		
		// Makes stars less bright the further away they are
		if(StellarViewConfig.distance_star_brightness.get())
		{
			int minAlpha = (alpha - 0xAA) * 2 / 3;
			
			if(distance > 40)
				alpha -= 2 * (int) Math.round(distance);
			
			if(alpha < minAlpha)
				alpha = minAlpha;
		}
		
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
			double aLocation = (double) ((j & 2) - 1) * Mth.clamp(starSize * 20 * distance, minStarSize, maxStarSize); //starSize;
			double bLocation = (double) ((j + 1 & 2) - 1) * Mth.clamp(starSize * 20 * distance, minStarSize, maxStarSize); //starSize;
			
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
			
			builder.vertex(starX + projectedX, starY + heightProjectionY, starZ + projectedZ).color(starColor[0], starColor[1], starColor[2], alpha).endVertex();
		}
	}
	
	public static void createStar(BufferBuilder builder, RandomSource randomsource, 
			double x, double y, double z, double distance, long seed)
	{
			createStar(builder, randomsource, x, y, z, distance, 1.0, 1.0, seed);
	}
	
	public static void createVanillaStar(BufferBuilder builder, RandomSource randomsource, 
			double x, double y, double z, double starSize, double distance, long seed)
	{
		if(distance < 1.0D && distance > 0.01D)
			createStar(builder, randomsource, x, y, z, distance, seed);
	}
}
