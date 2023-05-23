package net.povstalec.stellarview.client.render.level.misc;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.util.RandomSource;

public class StellarViewStarFormations
{
	
	public static BufferBuilder.RenderedBuffer drawSpiralGalaxy(BufferBuilder builder, long seed, 
			int numberOfStars, int numberOfArms,
			double xOffset, double yOffset, double zOffset, double alpha, double beta, double gamma)
	{
		RandomSource randomsource = RandomSource.create(seed);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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
				double alphaY = z * Math.sin(alpha) + y * Math.cos(alpha);
				double alphaZ = z * Math.cos(alpha) - y * Math.sin(alpha);
				
				//Rotates around Z
				double betaX = alphaX * Math.cos(beta) - alphaY * Math.sin(beta);
				double betaY = - alphaX * Math.sin(beta) - alphaY * Math.cos(beta);
				double betaZ = alphaZ;
				
				//Rotates around Y
				double gammaX = - betaX * Math.sin(gamma) - betaZ * Math.cos(gamma);
				double gammaY = betaY;
				double gammaZ = betaX * Math.cos(gamma) - betaZ * Math.sin(gamma);
				
				x = gammaX + xOffset;
				y = gammaY + yOffset;
				z = gammaZ + zOffset;
				
				double starSize = (double) (0.15F + randomsource.nextFloat() * 0.1F); // This randomizes the Star size
				double distance = x * x + y * y + z * z;
				
				StellarViewStar.createStar(builder, randomsource, x, y, z, starSize, distance, new int[] {255, 255, 255});
			}
		}
		return builder.end();
	}
	
	public static BufferBuilder.RenderedBuffer drawVanillaStars(BufferBuilder builder)
	{
		RandomSource randomsource = RandomSource.create(10842L);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		for(int i = 0; i < 1500; i++)
		{
			// This generates random coordinates for the Star close to the camera
			double x = (double) (randomsource.nextFloat() * 2.0F - 1.0F);
			double y = (double) (randomsource.nextFloat() * 2.0F - 1.0F);
			double z = (double) (randomsource.nextFloat() * 2.0F - 1.0F);
			
			double starSize = (double) (0.15F + randomsource.nextFloat() * 0.1F); // This randomizes the Star size
			double distance = x * x + y * y + z * z;
			
			StellarViewStar.createVanillaStar(builder, randomsource, x, y, z, starSize, distance, new int[] {255, 255, 255});
		}
		return builder.end();
	}
}
