package net.povstalec.stellarview.client.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.api.common.space_objects.StarLike;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class StarData
{
	public static final float DEFAULT_DISTANCE = 100;
	public static final float MIN_STAR_SIZE = 0.02F;
	public static final float MIN_TEX_STAR_SIZE = 0.08F;
	
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
	
	public void renderStars(StarField.LevelOfDetail levelOfDetail, Matrix4f pose, Matrix4f projectionMatrix, SpaceCoords difference, boolean isStatic, boolean hasTexture)
	{
		switch(levelOfDetail)
		{
		case LOD3:
			if(lod3 == null)
				lod3 = newStars(StarField.LevelOfDetail.LOD3);
			lod3.renderStarBuffer(pose, projectionMatrix, difference, isStatic, hasTexture);
		case LOD2:
			if(lod2 == null)
				lod2 = newStars(StarField.LevelOfDetail.LOD2);
			lod2.renderStarBuffer(pose, projectionMatrix, difference, isStatic, hasTexture);
		case LOD1:
			if(lod1 == null)
				lod1 = newStars(StarField.LevelOfDetail.LOD1);
			lod1.renderStarBuffer(pose, projectionMatrix, difference, isStatic, hasTexture);
		}
	}
	
	protected abstract LOD newStars(StarField.LevelOfDetail lod);
	
	public static void addStarHeightWidthSize(BufferBuilder builder, float height, float width, float size)
	{
		long i = builder.beginElement(StellarViewVertexFormat.ELEMENT_HEIGHT_WIDTH_SIZE.get());
		
		if (i != -1L)
		{
			MemoryUtil.memPutFloat(i, height);
			MemoryUtil.memPutFloat(i + Float.BYTES, width);
			MemoryUtil.memPutFloat(i + Float.BYTES * 2, size);
		}
	}
	
	
	
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
		 */
		public void newStar(StarLike.StarType starType, Random random, double x, double y, double z)
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
				
				builder.addVertex((float) starCoords[i][0], (float) starCoords[i][1], (float) starCoords[i][2])
						.setColor((byte) starRGBA[i][0], (byte) starRGBA[i][1], (byte) starRGBA[i][2], (byte) starRGBA[i][3]);
				
				addStarHeightWidthSize(builder, (float) height, (float) width, (float) starSizes[i]);
				
				if(hasTexture)
					builder.setUv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F);
			}
		}
		
		public MeshData getStarBuffer(Tesselator tesselator, boolean hasTexture)
		{
			final var bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, hasTexture ? StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get() : StellarViewVertexFormat.STAR_POS_COLOR_LY.get());
			
			for(int i = 0; i < stars; i++)
			{
				createStar(bufferBuilder, hasTexture, i);
			}
			return bufferBuilder.build();
		}
		
		private void renderStarBuffer(Matrix4f pose, Matrix4f projectionMatrix, SpaceCoords difference, boolean isStatic, boolean hasTexture)
		{
			if(stars == 0)
				return;
			
			if(starBuffer == null) // Buffer requires setup
			{
				if(!SpaceRenderer.loadNewStars())
					return;
				
				starBuffer = new StarBuffer();
				
				Tesselator tesselator = Tesselator.getInstance();
				RenderSystem.setShader(GameRenderer::getPositionShader);
				MeshData mesh = isStatic ? getStaticStarBuffer(tesselator, hasTexture, difference) : getStarBuffer(tesselator, hasTexture);
				
				starBuffer.bind();
				starBuffer.upload(mesh);
				if(isStatic)
					starBuffer.drawWithShader(pose, projectionMatrix, hasTexture ? GameRenderer.getPositionTexColorShader() : GameRenderer.getPositionColorShader());
				else
					starBuffer.drawWithShader(pose, projectionMatrix, difference, hasTexture ? StellarViewShaders.starTexShader() : StellarViewShaders.starShader());
				VertexBuffer.unbind();
				
				SpaceRenderer.loadedStars(stars);
			}
			else
			{
				starBuffer.bind();
				if(isStatic)
					starBuffer.drawWithShader(pose, projectionMatrix, hasTexture ? GameRenderer.getPositionTexColorShader() : GameRenderer.getPositionColorShader());
				else
					starBuffer.drawWithShader(pose, projectionMatrix, difference, hasTexture ? StellarViewShaders.starTexShader() : StellarViewShaders.starShader());
				VertexBuffer.unbind();
			}
		}
		
		//============================================================================================
		//*******************************************Static*******************************************
		//============================================================================================
		
		public MeshData getStaticStarBuffer(Tesselator tesselator, boolean hasTexture, SpaceCoords difference)
		{
			final var bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, hasTexture ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR);
			
			for(int i = 0; i < stars; i++)
			{
				createStaticStar(bufferBuilder, hasTexture, i, difference);
			}
			return bufferBuilder.build();
		}
		
		double clampStar(double starSize, double minStarSize, double distance)
		{
			starSize -= starSize * distance / 1000000.0;
			
			if(starSize < minStarSize)
				return minStarSize;
			
			return starSize;
		}
		
		private void createStaticStar(BufferBuilder builder, boolean hasTexture, int i, SpaceCoords difference)
		{
			double x = starCoords[i][0] - difference.x().toLy();
			double y = starCoords[i][1] - difference.y().toLy();
			double z = starCoords[i][2] - difference.z().toLy();
			
			double distance = Math.sqrt(x * x + y * y + z * z); // Distance squared
			
			// COLOR START - Adjusts the brightness (alpha) of the star based on its distance
			
			short alpha = starRGBA[i][3];
			short minAlpha = (short) (alpha / 10);
			
			// Stars appear dimmer the further away they are
			alpha -= (short) (255 * distance / 100000);
			
			if(alpha < minAlpha)
				alpha = minAlpha;
			
			if(hasTexture && alpha < 26)
				alpha = 26;
			
			// COLOR END
			
			double starSize = clampStar(hasTexture ? starSizes[i] * 4 : starSizes[i], hasTexture ? MIN_TEX_STAR_SIZE : MIN_STAR_SIZE, distance);
			
			distance = 1.0D / distance; // Regular distance
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
				double height = (aLocation * cosRandom - bLocation * sinRandom) * starSize;
				double width = (bLocation * cosRandom + aLocation * sinRandom) * starSize;
				
				double heightProjectionY = height * sinPhi; // Y projection of the Star's height
				
				double heightProjectionXZ = -height * cosPhi; // If the Star is angled, the XZ projected height needs to be subtracted from both X and Z
				
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
				
				if(hasTexture)
				{
					builder.addVertex((float) (starX + projectedX), (float) (starY + heightProjectionY), (float) (starZ + projectedZ))
							.setUv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F)
							.setColor((byte) starRGBA[i][0], (byte) starRGBA[i][1], (byte) starRGBA[i][2], alpha);
				}
				else
				{
					builder.addVertex((float) (starX + projectedX), (float) (starY + heightProjectionY), (float) (starZ + projectedZ))
							.setColor((byte) starRGBA[i][0], (byte) starRGBA[i][1], (byte) starRGBA[i][2], alpha);
				}
			}
		}
	}
}
