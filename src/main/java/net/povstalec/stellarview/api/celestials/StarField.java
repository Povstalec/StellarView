package net.povstalec.stellarview.api.celestials;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.api.celestials.orbiting.OrbitingCelestialObject;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class StarField extends StellarObject
{
	@Nullable
	protected VertexBuffer starBuffer;
	
	private List<StellarObject> galacticObjects = new ArrayList<StellarObject>();
	
	protected long seed;
	
	protected short numberOfStars;
	
	protected Vector3f offset = new Vector3f(0, 0, 0);
	
	public StarField(ResourceLocation texture, float size, long seed, short numberOfStars)
	{
		super(texture, size);
		
		this.seed = seed;
		
		this.numberOfStars = numberOfStars;
	}
	
	public StarField setStarBuffer(float xOffset, float yOffset, float zOffset,
			float xAxisRotation, float yAxisRotation, float zAxisRotation)
	{
		if(starBuffer != null)
			starBuffer.close();
		
		starBuffer = new VertexBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		this.offsetCoords.set(xOffset, yOffset, zOffset);
		
		this.setRotation(xAxisRotation, yAxisRotation, zAxisRotation);
		
		bufferbuilder$renderedbuffer = getStarBuffer(bufferBuilder, xOffset, yOffset, zOffset, xAxisRotation, yAxisRotation, zAxisRotation);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return this;
	}
	
	public final StarField addGalacticObject(StellarObject object,
			float x, float y, float z)
	{
		this.addGalacticObject(object, x, y, z, 0, 0, 0);
		return this;
	}
	
	public final StarField addGalacticObject(StellarObject object,
			float x, float y, float z,
			float xRotation, float yRotation, float zRotation)
	{
		this.galacticObjects.add(object.setGalacticPosition(x, y, z).setRotation(xRotation, yRotation, zRotation));
		object.primaryBody = Optional.of(this);
		return this;
	}
	
	
	
	protected abstract BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder,
			float xOffset, float yOffset, float zOffset,
			float xAxisRotation, float yAxisRotation, float zAxisRotation);
	
	protected void renderStars(ClientLevel level, Camera camera, float partialTicks, float rain, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog,
			Vector3f skyAxisRotation, Vector3f axisRotation)
	{
		//setStarBuffer(this.offsetCoords.x, this.offsetCoords.y, this.offsetCoords.z,
		//		skyAxisRotation.x, skyAxisRotation.y, skyAxisRotation.z);
		
		float starBrightness = Star.getStarBrightness(level, camera, partialTicks);
		
		if(starBrightness > 0.0F)
		{
			stack.pushPose();
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			//RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/sun.png"));
			FogRenderer.setupNoFog();
			
			stack.mulPose(Vector3f.YP.rotationDegrees(skyAxisRotation.y()));
	        stack.mulPose(Vector3f.ZP.rotationDegrees(skyAxisRotation.z()));
	        stack.mulPose(Vector3f.XP.rotationDegrees(skyAxisRotation.x()));
	        
	        stack.mulPose(Vector3f.YP.rotationDegrees(axisRotation.y()));
	        stack.mulPose(Vector3f.ZP.rotationDegrees(axisRotation.z()));
	        stack.mulPose(Vector3f.XP.rotationDegrees(axisRotation.x()));
			
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
			//this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexShader());
			VertexBuffer.unbind();
			
			setupFog.run();
			stack.popPose();
		}
	}
	
	public void render(OrbitingCelestialObject viewCenterParent, OrbitingCelestialObject viewCenter, Vector3f viewCenterCoords, ClientLevel level, Camera camera, float partialTicks, float rain, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f axisRotation, Vector3f coords)
	{
		if(this.starBuffer != null)
			renderStars(level, camera, partialTicks, rain, stack, projectionMatrix, setupFog, skyAxisRotation, axisRotation);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		this.galacticObjects.stream().forEach(galacticObject ->
		{
			if(!galacticObject.equals(viewCenterParent))
				galacticObject.setRotation(axisRotation);
			else
				galacticObject.setRotation(new Vector3f(0, 0, 0));
			galacticObject.render(viewCenter, viewCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, StellarCoordinates.subtractVectors(StellarCoordinates.addVectors(offsetCoords, coords), galacticObject.coordinates));
		});
	}
	

	
	public static class GlobularCluster extends StarField
	{
		public GlobularCluster(ResourceLocation texture, float size, long seed, short numberOfStars)
		{
			super(texture, size, seed, numberOfStars);
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
				
				Star.createVanillaStar(bufferBuilder, randomsource, x, y, z, starSize, distance, randomsource.nextLong());
			}
			return bufferBuilder.end();
		}
	}
	
	public static class VanillaStarField extends GlobularCluster
	{
		public VanillaStarField(ResourceLocation texture, float size)
		{
			super(texture, size, 10842L, (short) 1500);
		}
	}
}
