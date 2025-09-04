package net.povstalec.stellarview.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.api.common.space_objects.StarLike;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.client.render.shader.VertexOrder;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class DustCloudData
{
	public static final float DEFAULT_DISTANCE = 100;
	public static final float MAX_SIZE = 50;
	public static final float MAX_ALPHA = 0.025F;
	
	private LOD lod1;
	
	public DustCloudData() {}
	
	public void reset()
	{
		if(lod1 != null)
			lod1.reset();
	}
	
	public void renderDustClouds(Matrix4f pose, Matrix4f projectionMatrix, SpaceCoords difference, boolean isStatic)
	{
		if(lod1 == null)
			lod1 = newDustClouds();
		lod1.renderDustCloudBuffer(pose, projectionMatrix, difference, isStatic);
	}
	
	protected abstract DustCloudData.LOD newDustClouds();
	
	
	
	public static class LOD
	{
		@Nullable
		protected DustCloudBuffer dustCloudBuffer;
		
		private double[][] dustCloudCoords;
		private double[] dustCloudSizes;
		
		private short[][] dustCloudRGBA;
		
		private double[][] randoms;
		
		private int dustClouds;
		
		public LOD(int dustClouds)
		{
			this.dustCloudCoords = new double[dustClouds][3];
			this.dustCloudSizes = new double[dustClouds];
			
			this.randoms = new double[dustClouds][2];
			
			this.dustCloudRGBA = new short[dustClouds][4];
			
			this.dustClouds = 0;
		}
		
		public void reset()
		{
			if(dustCloudBuffer == null)
				return;
			
			dustCloudBuffer.close();
			dustCloudBuffer = null;
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
		public void newDustCloud(DustCloudInfo.DustCloudType dustCloudType, Random random, double x, double y, double z, double sizeMultiplier)
		{
			// Set up position
			
			dustCloudCoords[dustClouds][0] = x;
			dustCloudCoords[dustClouds][1] = y;
			dustCloudCoords[dustClouds][2] = z;
			
			Color.IntRGB rgb = dustCloudType.getRGB();
			
			// Set up size
			
			dustCloudSizes[dustClouds] = dustCloudType.randomSize(random) * sizeMultiplier; // This randomizes the Star size
			
			// Set up color and alpha
			
			short alpha = dustCloudType.randomBrightness(random); // 0xAA is the default
			
			this.dustCloudRGBA[dustClouds] = new short[] {(short) rgb.red(), (short) rgb.green(), (short) rgb.blue(), alpha};
			
			// sin and cos are used to effectively clamp the random number between two values without actually clamping it,
			// wwhich would result in some awkward lines as Stars would be brought to the clamped values
			// Both affect Star size and rotation
			double randomValue = random.nextDouble() * Math.PI * 2.0D;
			randoms[dustClouds][0] = Math.sin(randomValue); // sin random
			randoms[dustClouds][1] = Math.cos(randomValue); // cos random
			
			dustClouds++;
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
				
				builder.vertex(dustCloudCoords[i][0], dustCloudCoords[i][1], dustCloudCoords[i][2]).color(dustCloudRGBA[i][0], dustCloudRGBA[i][1], dustCloudRGBA[i][2], dustCloudRGBA[i][3]);
				// These next few lines add a "custom" element defined as HeightWidthSize in StellarViewVertexFormat
				builder.putFloat(0, (float) height);
				builder.putFloat(4, (float) width);
				builder.putFloat(8, (float) dustCloudSizes[i]);
				builder.nextElement();
				
				builder.uv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F);
				
				builder.endVertex();
			}
		}
		
		public BufferBuilder.RenderedBuffer getDustCloudBuffer(BufferBuilder bufferBuilder)
		{
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX);
			
			for(int i = 0; i < dustClouds; i++)
			{
				createDustCloud(bufferBuilder, i);
			}
			return bufferBuilder.end();
		}
		
		public void renderDustCloudBuffer(Matrix4f pose, Matrix4f projectionMatrix, SpaceCoords difference, boolean isStatic)
		{
			if(dustClouds == 0)
				return;
			
			if(dustCloudBuffer == null) // Buffer requires setup
			{
				if(!SpaceRenderer.loadNewDustClouds())
					return;
				
				dustCloudBuffer = new DustCloudBuffer();
				
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				RenderSystem.setShader(GameRenderer::getPositionShader);
				BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
				
				bufferbuilder$renderedbuffer = isStatic ? getStaticDustCloudBuffer(bufferBuilder, difference) : getDustCloudBuffer(bufferBuilder);
				
				dustCloudBuffer.bind();
				dustCloudBuffer.upload(bufferbuilder$renderedbuffer);
				if(isStatic)
					dustCloudBuffer.drawWithShader(pose, projectionMatrix, VertexOrder.texColorShader());
				else
					dustCloudBuffer.drawWithShader(pose, projectionMatrix, difference, StellarViewShaders.starDustCloudShader());
				VertexBuffer.unbind();
				
				SpaceRenderer.loadedDustClouds(dustClouds);
			}
			else
			{
				dustCloudBuffer.bind();
				if(isStatic)
					dustCloudBuffer.drawWithShader(pose, projectionMatrix, VertexOrder.texColorShader());
				else
					dustCloudBuffer.drawWithShader(pose, projectionMatrix, difference, StellarViewShaders.starDustCloudShader());
				VertexBuffer.unbind();
			}
		}
		
		//============================================================================================
		//*******************************************Static*******************************************
		//============================================================================================
		
		public BufferBuilder.RenderedBuffer getStaticDustCloudBuffer(BufferBuilder bufferBuilder, SpaceCoords difference)
		{
			bufferBuilder.begin(VertexFormat.Mode.QUADS, VertexOrder.texColorFormat());
			
			for(int i = 0; i < dustClouds; i++)
			{
				createStaticDustCloud(bufferBuilder, i, difference);
			}
			return bufferBuilder.end();
		}
		
		double clampDustCloud(double size, double distance)
		{
			double minSize = size * 0.04F;
			
			size = 100000 * size / distance;
			
			if(size > MAX_SIZE)
				return MAX_SIZE;
			
			return size < minSize ? minSize : size;
		}
		
		double clampAlpha(double alpha, double distance)
		{
			double minAlpha = alpha * 0.005F;
			
			// Stars appear dimmer the further away they are
			//alpha -= distance / 100000;
			alpha = 100000 * alpha / distance;
			
			if(alpha < minAlpha)
				return minAlpha;
			
			return alpha > MAX_ALPHA ? MAX_ALPHA : alpha;
		}
		
		private void createStaticDustCloud(BufferBuilder builder, int i, SpaceCoords difference)
		{
			double x = dustCloudCoords[i][0] - difference.x().toLy();
			double y = dustCloudCoords[i][1] - difference.y().toLy();
			double z = dustCloudCoords[i][2] - difference.z().toLy();
			
			double distance = Math.sqrt(x * x + y * y + z * z); // Distance squared
			
			// COLOR START - Adjusts the brightness (alpha) of the star based on its distance
			
			short alpha = dustCloudRGBA[i][3];
			alpha = (short) (255 * clampAlpha(alpha / 255D, distance));
			
			//if(alpha < 26)
			//	alpha = 26;
			
			// COLOR END
			
			double starSize = clampDustCloud(dustCloudSizes[i] * 4, distance);
			
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
				
				if(VertexOrder.texColor())
					builder.vertex(starX + projectedX, starY + heightProjectionY, starZ + projectedZ).uv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F).color(dustCloudRGBA[i][0], dustCloudRGBA[i][1] , dustCloudRGBA[i][2], alpha).endVertex();
				else
					builder.vertex(starX + projectedX, starY + heightProjectionY, starZ + projectedZ).color(dustCloudRGBA[i][0], dustCloudRGBA[i][1] , dustCloudRGBA[i][2], alpha).uv( (float) (aLocation + 1) / 2F, (float) (bLocation + 1) / 2F).endVertex();
			}
		}
	}
}
