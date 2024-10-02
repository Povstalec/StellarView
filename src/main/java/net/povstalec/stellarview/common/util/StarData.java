package net.povstalec.stellarview.common.util;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.client.resourcepack.StarInfo;
import net.povstalec.stellarview.client.resourcepack.objects.StarLike;

public class StarData
{
	private double[][] starCoords;
	private double[] starSizes;
	
	private double deformations[][];
	
	private short[][] starRGBA;
	
	private double[][] randoms;
	
	public StarData(int stars)
	{
		this.starCoords = new double[stars][3];
		this.starSizes = new double[stars];

		this.deformations = new double[stars][2];
		
		this.randoms = new double[stars][2];
		
		this.starRGBA = new short[stars][4];
	}
	
	public static double clampStar(double starSize, double minStarSize, double maxStarSize)
	{
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
	public void newStar(StarInfo starInfo, BufferBuilder builder, RandomSource randomSource, SpaceCoords relativeCoords, double x, double y, double z, int i)
	{
		long seed = randomSource.nextLong();
		
		// Set up position
		
		starCoords[i][0] = x;
		starCoords[i][1] = y;
		starCoords[i][2] = z;
		
		//TODO Set up deformation
		deformations[i][0] = 1; // Height deformation
		deformations[i][1] = 1;// Width deformation
		
		StarLike.StarType starType = starInfo.getRandomStarType(seed);
		Color.IntRGB rgb = starType.getRGB();
		
		// Set up size
		
		starSizes[i] = starType.randomSize(seed); // This randomizes the Star size
		
		// Set up color and alpha
		
		short alpha = starType.randomBrightness(seed); // 0xAA is the default
		
		this.starRGBA[i] = new short[] {(short) rgb.red(), (short) rgb.green(), (short) rgb.blue(), alpha};
		
		// sin and cos are used to effectively clamp the random number between two values without actually clamping it,
		// wwhich would result in some awkward lines as Stars would be brought to the clamped values
		// Both affect Star size and rotation
		double random = randomSource.nextDouble() * Math.PI * 2.0D;
		randoms[i][0] = Math.sin(random); // sin random
		randoms[i][1] = Math.cos(random); // cos random
		
		this.createStar(builder, randomSource, relativeCoords, i);
	}
	
	public void createStar(BufferBuilder builder, RandomSource randomSource, SpaceCoords relativeCoords, int i)
	{
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
			double aLocation = (double) ((j & 2) - 1);
			double bLocation = (double) ((j + 1 & 2) - 1);
			
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
			
			builder.vertex(starCoords[i][0], starCoords[i][1], starCoords[i][2]).color(starRGBA[i][0], starRGBA[i][1], starRGBA[i][2], starRGBA[i][3]);
			// These next few lines add a "custom" element defined as HeightWidthSize in StellarViewVertexFormat
			builder.putFloat(0, (float) height);
			builder.putFloat(4, (float) width);
			builder.putFloat(8, (float) starSizes[i]);
			builder.nextElement();
			
			builder.endVertex();
		}
	}
}
