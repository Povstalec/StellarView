package net.povstalec.stellarview.common.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.client.resourcepack.objects.StarField;
import net.povstalec.stellarview.client.resourcepack.objects.StarLike;
import net.povstalec.stellarview.client.util.StarBuffer;
import net.povstalec.stellarview.common.config.GeneralConfig;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class StarData
{
	private LOD lod1;
	private LOD lod2;
	private LOD lod3;
	
	public StarData() {}
	
	private LOD getLOD(StarField.LevelOfDetail lod)
	{
		return switch(lod)
		{
			case LOD1 -> lod1;
			case LOD2 -> lod2;
			case LOD3 -> lod3;
		};
	}
	
	public void reset()
	{
		if(lod1 != null)
			lod1.reset();
		if(lod2 != null)
			lod2.reset();
		if(lod3 != null)
			lod3.reset();
	}
	
	public void renderStars(StarField.LevelOfDetail levelOfDetail, Matrix4f pose, Matrix4f projectionMatrix, SpaceCoords difference, boolean hasTexture)
	{
		switch(levelOfDetail)
		{
		case LOD3:
			if(lod3 == null)
				lod3 = newStars(StarField.LevelOfDetail.LOD3);
			lod3.renderStarBuffer(pose, projectionMatrix, difference, hasTexture);
		case LOD2:
			if(lod2 == null)
				lod2 = newStars(StarField.LevelOfDetail.LOD2);
			lod2.renderStarBuffer(pose, projectionMatrix, difference, hasTexture);
		case LOD1:
			if(lod1 == null)
				lod1 = newStars(StarField.LevelOfDetail.LOD1);
			lod1.renderStarBuffer(pose, projectionMatrix, difference, hasTexture);
		}
	}
	
	protected abstract LOD newStars(StarField.LevelOfDetail lod);
	
	
	
	public static class LOD
	{
		@Nullable
		private StarBuffer starBuffer;
		
		private double[][] starCoords;
		private double[] starSizes;
		
		private short[][] starRGBA;
		
		private double[][] randoms;
		
		private int stars;
		
		public LOD(int stars)
		{
			this.starCoords = new double[stars][3];
			this.starSizes = new double[stars];
			
			this.randoms = new double[stars][2];
			
			this.starRGBA = new short[stars][4];
			
			this.stars = 0;
		}
		
		public void reset()
		{
			if(starBuffer == null)
				return;
			
			starBuffer.close();
			starBuffer = null;
		}
		
		/**
		 * Creates information for a completely new star
		 * @param starType StarType used for obtaining information about what star to create
		 * @param random Random used for randomizing the star information
		 * @param x X coordinate of the star
		 * @param y Y coordinate of the star
		 * @param z Z coordinate of the star
		 * @param hasTexture Whether or not the star has a texture
		 */
		public void newStar(StarLike.StarType starType, Random random, double x, double y, double z, boolean hasTexture)
		{
			// Set up position
			
			starCoords[stars][0] = x;
			starCoords[stars][1] = y;
			starCoords[stars][2] = z;
			
			short alpha = starType.randomBrightness(random); // 0xAA is the default
			Color.IntRGB rgb = starType.getRGB();
			
			// Set up size
			
			starSizes[stars] = starType.randomSize(random); // This randomizes the Star size
			
			// Set up color and alpha
			
			starRGBA[stars] = new short[] {(short) rgb.red(), (short) rgb.green(), (short) rgb.blue(), alpha};
			
			// sin and cos are used to effectively clamp the random number between two values without actually clamping it,
			// wwhich would result in some awkward lines as Stars would be brought to the clamped values
			// Both affect Star size and rotation
			double randomValue = random.nextDouble() * Math.PI * 2.0D;
			randoms[stars][0] = Math.sin(randomValue); // sin random
			randoms[stars][1] = Math.cos(randomValue); // cos random
			
			//lod.createStar(builder, hasTexture, lod.size);
			stars++;
		}
		
		public void createStar(BufferBuilder builder, boolean hasTexture, int i)
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
				
				builder.vertex(starCoords[i][0], starCoords[i][1], starCoords[i][2]).color(starRGBA[i][0], starRGBA[i][1], starRGBA[i][2], starRGBA[i][3]);
				// These next few lines add a "custom" element defined as HeightWidthSize in StellarViewVertexFormat
				builder.putFloat(0, (float) height);
				builder.putFloat(4, (float) width);
				builder.putFloat(8, (float) starSizes[i]);
				builder.nextElement();
				
				if(hasTexture)
					builder.uv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F);
				
				builder.endVertex();
			}
		}
		
		public BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, boolean hasTexture)
		{
			bufferBuilder.begin(VertexFormat.Mode.QUADS, hasTexture ? StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX : StellarViewVertexFormat.STAR_POS_COLOR_LY);
			
			for(int i = 0; i < stars; i++)
			{
				createStar(bufferBuilder, hasTexture, i);
			}
			return bufferBuilder.end();
		}
		
		private void renderStarBuffer(Matrix4f pose, Matrix4f projectionMatrix, SpaceCoords difference, boolean hasTexture)
		{
			if(stars == 0)
				return;
			
			if(starBuffer == null) // Buffer requires setup
			{
				if(!SpaceRenderer.loadNewStars())
					return;
				
				starBuffer = new StarBuffer();
				
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				RenderSystem.setShader(GameRenderer::getPositionShader);
				BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
				
				bufferbuilder$renderedbuffer = getStarBuffer(bufferBuilder, GeneralConfig.textured_stars.get());
				
				starBuffer.bind();
				starBuffer.upload(bufferbuilder$renderedbuffer);
				starBuffer.drawWithShader(pose, projectionMatrix, difference, hasTexture ? StellarViewShaders.starTexShader() : StellarViewShaders.starShader());
				VertexBuffer.unbind();
				
				SpaceRenderer.loadedStars(stars);
			}
			else
			{
				starBuffer.bind();
				starBuffer.drawWithShader(pose, projectionMatrix, difference, hasTexture ? StellarViewShaders.starTexShader() : StellarViewShaders.starShader());
				VertexBuffer.unbind();
			}
		}
	}
}
