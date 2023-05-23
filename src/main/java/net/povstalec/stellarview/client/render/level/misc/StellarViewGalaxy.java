package net.povstalec.stellarview.client.render.level.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.GameRenderer;

public class StellarViewGalaxy
{
	public enum Type
	{
		VANILLA,
		SPIRAL_GALAXY_2_ARMS,
		SPIRAL_GALAXY_4_ARMS,
		ELLIPTICAL_GALAXY
	}
	
	public final static VertexBuffer createStars(Type type, long seed, int numberOfStars,
			double xOffset, double yOffset, double zOffset, double alpha, double beta, double gamma)
	{
		VertexBuffer starBuffer = new VertexBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		switch(type)
		{
		case SPIRAL_GALAXY_4_ARMS:
			bufferbuilder$renderedbuffer = StellarViewStarFormations.drawSpiralGalaxy(
					bufferbuilder, seed, numberOfStars, 4, 
					xOffset, yOffset, zOffset, alpha, beta, gamma);
			break;
		case SPIRAL_GALAXY_2_ARMS:
			bufferbuilder$renderedbuffer = StellarViewStarFormations.drawSpiralGalaxy(
					bufferbuilder, seed, numberOfStars, 2, 
					xOffset, yOffset, zOffset, alpha, beta, gamma);
			break;
		default:
			bufferbuilder$renderedbuffer = StellarViewStarFormations.drawVanillaStars(bufferbuilder);
		}
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return starBuffer;
	}
}
