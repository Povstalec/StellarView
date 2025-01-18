package net.povstalec.stellarview.common.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.povstalec.stellarview.client.util.StarData;

import java.util.Random;

public class DustCloudData
{
	private double[][] dustCloudCoords;
	private double[] dustCloudSizes;
	
	private short[][] dustCloudRGBA;
	
	private double[][] randoms;
	
	public DustCloudData(int dustClouds)
	{
		this.dustCloudCoords = new double[dustClouds][3];
		this.dustCloudSizes = new double[dustClouds];
		
		this.randoms = new double[dustClouds][2];
		
		this.dustCloudRGBA = new short[dustClouds][4];
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
	 * @param random Random used for randomizing the star information
	 * @param relativeCoords SpaceCoords that give a relative position between the observer and the star
	 * @param x X coordinate of the star
	 * @param y Y coordinate of the star
	 * @param z Z coordinate of the star
	 * @param i Index of the star
	 */
	public void newDustCloud(DustCloudInfo dustCloudInfo, BufferBuilder builder, Random random, double x, double y, double z, double sizeMultiplier, int i)
	{
		// Set up position
		
		dustCloudCoords[i][0] = x;
		dustCloudCoords[i][1] = y;
		dustCloudCoords[i][2] = z;
		
		DustCloudInfo.DustCloudType dustCloudType = dustCloudInfo.getRandomDustCloudType(random);
		Color.IntRGB rgb = dustCloudType.getRGB();
		
		// Set up size
		
		dustCloudSizes[i] = dustCloudType.randomSize(random) * sizeMultiplier; // This randomizes the Star size
		
		// Set up color and alpha
		
		short alpha = dustCloudType.randomBrightness(random); // 0xAA is the default
		
		this.dustCloudRGBA[i] = new short[] {(short) rgb.red(), (short) rgb.green(), (short) rgb.blue(), alpha};
		
		// sin and cos are used to effectively clamp the random number between two values without actually clamping it,
		// wwhich would result in some awkward lines as Stars would be brought to the clamped values
		// Both affect Star size and rotation
		double randomValue = random.nextDouble() * Math.PI * 2.0D;
		randoms[i][0] = Math.sin(randomValue); // sin random
		randoms[i][1] = Math.cos(randomValue); // cos random
		
		this.createDustCloud(builder, i);
	}
	
	public void createDustCloud(BufferBuilder builder, int i)
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
			double aLocation = (j & 2) - 1;
			double bLocation = (j + 1 & 2) - 1;
			
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
			double height = aLocation * cosRandom - bLocation * sinRandom;
			double width = bLocation * cosRandom + aLocation * sinRandom;
			
			builder.addVertex((float) dustCloudCoords[i][0], (float) dustCloudCoords[i][1], (float) dustCloudCoords[i][2])
					.setColor((byte) dustCloudRGBA[i][0], (byte) dustCloudRGBA[i][1], (byte) dustCloudRGBA[i][2], (byte) dustCloudRGBA[i][3]);
			
			StarData.addStarHeightWidthSize(builder, (float) height, (float) width, (float) dustCloudSizes[i]);
			
			builder.setUv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F);
		}
	}
}
