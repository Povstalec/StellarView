package net.povstalec.stellarview.api.celestials;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;

public abstract class StarField
{
	@Nullable
	protected VertexBuffer starBuffer;
	
	private List<GalacticObject> galacticObjects = new ArrayList<GalacticObject>();
	
	protected long seed;
	
	protected short numberOfStars;
	
	protected float xOffset = 0;
	protected float yOffset = 0;
	protected float zOffset = 0;

	protected float xAxisRotation = 0;
	protected float yAxisRotation = 0;
	protected float zAxisRotation = 0;
	
	public StarField(long seed, short numberOfStars)
	{
		this.seed = seed;
		
		this.numberOfStars = numberOfStars;
	}
	
	public StarField setStarBuffer(float xOffset, float yOffset, float zOffset,
			float xAxisRotation, float yAxisRotation, float zAxisRotation)
	{
		starBuffer = new VertexBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		
		this.xAxisRotation = xAxisRotation;
		this.yAxisRotation = yAxisRotation;
		this.zAxisRotation = zAxisRotation;
		
		bufferbuilder$renderedbuffer = getStarBuffer(bufferBuilder, xOffset, yOffset, zOffset, xAxisRotation, yAxisRotation, zAxisRotation);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return this;
	}
	
	public final StarField addGalacticObject(GalacticObject object, float x, float y, float z)
	{
		this.galacticObjects.add(object.setGalacticPosition(x, y, z));
		return this;
	}
	
	
	
	protected abstract BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder,
			float xOffset, float yOffset, float zOffset,
			float xAxisRotation, float yAxisRotation, float zAxisRotation);
	
	public float getXOffset()
	{
		return this.xOffset;
	}
	
	public float getYOffset()
	{
		return this.yOffset;
	}
	
	public float getZOffset()
	{
		return this.zOffset;
	}
	
	public float getXRotation()
	{
		return this.xAxisRotation;
	}
	
	public float getYRotation()
	{
		return this.yAxisRotation;
	}
	
	public float getZRotation()
	{
		return this.zAxisRotation;
	}
	
	protected void renderStars(ClientLevel level, Camera camera, float partialTicks, float rain, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog,
			float xAxisRotation, float yAxisRotation, float zAxisRotation)
	{
		float starBrightness = Star.getStarBrightness(level, camera, partialTicks);
		
		if(starBrightness > 0.0F)
		{
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			//RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/sun.png"));
			FogRenderer.setupNoFog();
			
			stack.mulPose(Axis.XP.rotation(xAxisRotation));
			stack.mulPose(Axis.YP.rotation(yAxisRotation));
			stack.mulPose(Axis.ZP.rotation(zAxisRotation));
			
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
			//this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexShader());
			VertexBuffer.unbind();
			
			setupFog.run();
		}
	}
	
	public void render(ClientLevel level, Camera camera, float partialTicks, float rain, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder,
			float xAxisRotation, float yAxisRotation, float zAxisRotation)
	{
		if(this.starBuffer != null)
			renderStars(level, camera, partialTicks, rain, stack, projectionMatrix, setupFog, xAxisRotation, yAxisRotation, zAxisRotation);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		this.galacticObjects.stream().forEach(galacticObject ->
		{
			galacticObject.setOffset(xOffset, yOffset, zOffset);
			galacticObject.render(level, camera, partialTicks, stack, bufferbuilder, xAxisRotation, yAxisRotation, zAxisRotation);
		});
	}
	
	
	
	public static class VanillaStarField extends StarField
	{
		public VanillaStarField(long seed, short numberOfStars)
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

			for(int i = 0; i < numberOfStars; i++)
			{
				// This generates random coordinates for the Star close to the camera
				double x = (double) (randomsource.nextFloat() * 2.0F - 1.0F);
				double y = (double) (randomsource.nextFloat() * 2.0F - 1.0F);
				double z = (double) (randomsource.nextFloat() * 2.0F - 1.0F);
				
				double starSize = (double) (0.15F + randomsource.nextFloat() * 0.1F); // This randomizes the Star size
				double distance = x * x + y * y + z * z;
				
				Star.createVanillaStar(bufferBuilder, randomsource, x, y, z, starSize, distance, new int[] {255, 255, 255});
			}
			return bufferBuilder.end();
		}
		
	}
	
	/*public static class GlobularCluster extends StarField //TODO
	{
		public GlobularCluster(long seed, short numberOfStars,
			float xAxisRotation, float yAxisRotation, float zAxisRotation)
		{
			super(seed, numberOfStars, xAxisRotation, yAxisRotation, zAxisRotation);
		}

		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, float xOffset, float yOffset, float zOffset)
		{
			
		}
	}*/
}
