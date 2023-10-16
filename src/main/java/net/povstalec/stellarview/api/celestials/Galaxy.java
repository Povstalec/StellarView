package net.povstalec.stellarview.api.celestials;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class Galaxy extends StarField
{
	protected Galaxy(long seed, short numberOfStars)
	{
		super(seed, numberOfStars);
	}
	
	
	
	public static class SpiralGalaxy extends StarField
	{
		byte numberOfArms;
		
		public SpiralGalaxy(long seed, byte numberOfArms, short numberOfStars)
		{
			super(seed, numberOfStars);
			this.numberOfArms = numberOfArms;
		}

		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder,
				float xOffset, float yOffset, float zOffset,
				float xAxisRotation, float yAxisRotation, float zAxisRotation)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			for(int j = 0; j < numberOfArms; j++)
			{
				double rotation = Math.PI * j / ((double) numberOfArms / 2);
				double length = randomsource.nextDouble() + 1.5;
				for(int i = 0; i < numberOfStars; i++)
				{
					double progress = (double) i / numberOfStars;
					
					double phi = length * Math.PI * progress - rotation;
					double r = StellarCoordinates.spiralR(5, phi, rotation);
					
					double x =  r * Math.cos(phi) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1 / (progress * 1.5);
					double z =  r * Math.sin(phi) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1 / (progress * 1.5);
					double y =  (randomsource.nextFloat() * 4.0F - 2.0F) * 1 / (progress * 1.5);
					
					//Rotates around X
					double alphaX = x;
					double alphaY = z * Math.sin(xAxisRotation) + y * Math.cos(xAxisRotation);
					double alphaZ = z * Math.cos(xAxisRotation) - y * Math.sin(xAxisRotation);
					
					//Rotates around Z
					double betaX = alphaX * Math.cos(zAxisRotation) - alphaY * Math.sin(zAxisRotation);
					double betaY = - alphaX * Math.sin(zAxisRotation) - alphaY * Math.cos(zAxisRotation);
					double betaZ = alphaZ;
					
					//Rotates around Y
					double gammaX = - betaX * Math.sin(yAxisRotation) - betaZ * Math.cos(yAxisRotation);
					double gammaY = betaY;
					double gammaZ = betaX * Math.cos(yAxisRotation) - betaZ * Math.sin(yAxisRotation);
					
					x = gammaX + xOffset;
					y = gammaY + yOffset;
					z = gammaZ + zOffset;
					
					double starSize = (double) (0.15F + randomsource.nextFloat() * 0.1F); // This randomizes the Star size
					double distance = x * x + y * y + z * z;
					
					Star.createStar(bufferBuilder, randomsource, x, y, z, starSize, distance, new int[] {255, 255, 255});
				}
			}
			return bufferBuilder.end();
		}
	}
	
	public static class LenticularGalaxy extends StarField
	{
		public LenticularGalaxy(long seed, short numberOfStars)
		{
			super(seed, numberOfStars);
		}

		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder,
				float xOffset, float yOffset, float zOffset,
				float xAxisRotation, float yAxisRotation, float zAxisRotation)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			
			double length = randomsource.nextDouble() + 1.5;
			for(int i = 0; i < numberOfStars; i++)
			{
				double progress = (double) i / numberOfStars;
				
				double phi = length * 2 * Math.PI * progress;
				double r = StellarCoordinates.elipticalR(10, 5, phi);
				
				double x =  r * Math.cos(phi) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1;
				double z =  r * Math.sin(phi) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1;
				double y =  (randomsource.nextFloat() * 4.0F - 2.0F) * 1;
				
				//Rotates around X
				double alphaX = x;
				double alphaY = z * Math.sin(xAxisRotation) + y * Math.cos(xAxisRotation);
				double alphaZ = z * Math.cos(xAxisRotation) - y * Math.sin(xAxisRotation);
				
				//Rotates around Z
				double betaX = alphaX * Math.cos(zAxisRotation) - alphaY * Math.sin(zAxisRotation);
				double betaY = - alphaX * Math.sin(zAxisRotation) - alphaY * Math.cos(zAxisRotation);
				double betaZ = alphaZ;
				
				//Rotates around Y
				double gammaX = - betaX * Math.sin(yAxisRotation) - betaZ * Math.cos(yAxisRotation);
				double gammaY = betaY;
				double gammaZ = betaX * Math.cos(yAxisRotation) - betaZ * Math.sin(yAxisRotation);
				
				x = gammaX + xOffset;
				y = gammaY + yOffset;
				z = gammaZ + zOffset;
				
				double starSize = (double) (0.15F + randomsource.nextFloat() * 0.1F); // This randomizes the Star size
				double distance = x * x + y * y + z * z;
				
				Star.createStar(bufferBuilder, randomsource, x, y, z, starSize, distance, new int[] {255, 255, 255});
			}
			return bufferBuilder.end();
		}
	}
	
	//TODO Elliptical
	//TODO Irregular
}
