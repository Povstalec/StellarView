package net.povstalec.stellarview.api.celestials;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class Galaxy extends StarField
{
	protected Galaxy(ResourceLocation texture, float size, long seed, short numberOfStars)
	{
		super(texture, size, seed, numberOfStars);
	}

	public static final ResourceLocation SPIRAL_GALAXY_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/galaxy/spiral_galaxy.png");
	public static final ResourceLocation LENTICULAR_GALAXY_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/galaxy/lenticular_galaxy.png");
	public static final ResourceLocation ELLIPTICAL_GALAXY_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/galaxy/elliptical_galaxy.png");
	public static final ResourceLocation IRREGULAR_GALAXY_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/galaxy/irregular_galaxy.png");

	public static class SpiralGalaxy extends Galaxy
	{
		
		private byte numberOfArms;
		
		public SpiralGalaxy(float size, long seed, byte numberOfArms, short numberOfStars)
		{
			super(SPIRAL_GALAXY_TEXTURE, size, seed, numberOfStars);
			this.numberOfArms = numberOfArms;
		}

		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder,
				float xOffset, float yOffset, float zOffset,
				float xAxisRotation, float yAxisRotation, float zAxisRotation)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				double rotation = Math.PI * j / ((double) numberOfArms / 2);
				double length = randomsource.nextDouble() + 1.5;
				for(int i = 0; i < numberOfStars; i++)
				{
					double progress = (double) i / numberOfStars;
					
					double phi = length * Math.PI * progress - rotation;
					double r = StellarCoordinates.spiralR(5, phi, rotation);
					//perhaps r *= size, so you can control the diameter of the galaxy somewhat? -NW
					
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
					
					double distance = x * x + y * y + z * z;
					
					Star.createStar(bufferBuilder, randomsource, x, y, z, distance, randomsource.nextLong());
				}
			}
			return bufferBuilder.end();
		}
	}
	
	public static class LenticularGalaxy extends Galaxy
	{
		public LenticularGalaxy(float size, long seed, short numberOfStars)
		{
			super(LENTICULAR_GALAXY_TEXTURE, size, seed, numberOfStars);
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
				//perhaps r *= size, so you can control the diameter of the galaxy somewhat? -NW
				
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
				
				double distance = x * x + y * y + z * z;
				
				Star.createStar(bufferBuilder, randomsource, x, y, z, distance, randomsource.nextLong());
			}
			return bufferBuilder.end();
		}
	}

	public static class EllipticalGalaxy extends Galaxy {
		public EllipticalGalaxy(float size, long seed, short numberOfStars) {
			super(ELLIPTICAL_GALAXY_TEXTURE, size, seed, numberOfStars);
		}

		@Override
		protected BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder,
				float xOffset, float yOffset, float zOffset,
				float xAxisRotation, float yAxisRotation, float zAxisRotation) {
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			for (int i = 0; i < numberOfStars; i++) {
				double r = randomsource.nextDouble() * size;
				//size used here to allow control of the size of the galaxy,
				// rather than the texture that isn't even rendered in the first place
				//if you want to hard-code a size, i'd recommend something like 50 or 100 -NW
				double tetha = 2 * Math.PI * randomsource.nextDouble();
				//tetha is inclination
				double phi = Math.acos(2 * randomsource.nextDouble() - 1);
				//phi is azimuth

				double x = r * Math.sin(phi) * Math.cos(tetha) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1;
				double z = r * Math.sin(phi) * Math.sin(tetha) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1;
				double y = r * Math.cos(phi) + (randomsource.nextFloat() * 4.0F - 2.0F) * 1;

				//Rotates around X
				double alphaX = x;
				double alphaY = z * Math.sin(xAxisRotation) + y * Math.cos(xAxisRotation);
				double alphaZ = z * Math.cos(xAxisRotation) - y * Math.sin(xAxisRotation);

				//Rotates around Y
				double betaX = alphaX * Math.cos(zAxisRotation) - alphaY * Math.sin(zAxisRotation);
				double betaY = -alphaX * Math.sin(zAxisRotation) - alphaY * Math.cos(zAxisRotation);
				double betaZ = alphaZ;

				//Rotates around Z
				double gammaX = -betaX * Math.sin(yAxisRotation) - betaZ * Math.cos(yAxisRotation);
				double gammaY = betaY;
				double gammaZ = betaX * Math.cos(yAxisRotation) - betaZ * Math.sin(yAxisRotation);

				x = gammaX + xOffset;
				y = gammaY + yOffset;
				z = gammaZ + zOffset;

				double distance = x * x + y * y + z * z;

				Star.createStar(bufferBuilder, randomsource, x, y, z, distance, randomsource.nextLong());
			}
			return bufferBuilder.end();
		}
	}
	//TODO Irregular
	//Perhaps this should be generated using Perlin noise, smoothed with interpolation. -NW
}
