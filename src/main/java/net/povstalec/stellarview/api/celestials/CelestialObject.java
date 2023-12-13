package net.povstalec.stellarview.api.celestials;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.celestials.orbiting.OrbitingCelestialObject;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public abstract class CelestialObject
{
	/**
	 * Gravitational Constant
	 */
	public static final float G = 6.673e-11F;
	public static final int TICKS_PER_DAY = 24000;
	
	public static final float[] FULL_UV = new float[] {0.0F, 0.0F, 1.0F, 1.0F};
	public static final float DEFAULT_DISTANCE = 100.0F;

	protected ResourceLocation texture;

	protected ResourceLocation haloTexture;
	protected float haloSize;
	protected boolean hasHalo = false;

	protected boolean blends = false;
	protected boolean blendsDuringDay = false;
	protected boolean visibleDuringDay = false;
	
	protected float rotation = 0; // Rotation around the axis facing the Player
	
	public CelestialObject(ResourceLocation texture)
	{
		this.texture = texture;
	}
	
	/**
	 * Creates a Halo around the Celestial Object
	 * @param haloTexture Texture used for the Halo
	 * @param haloSize Size of the Halo
	 * @return self
	 */
	public CelestialObject halo(ResourceLocation haloTexture, float haloSize)
	{
		this.haloTexture = haloTexture;
		this.haloSize = haloSize;
		this.hasHalo = true;
		return this;
	}
	
	/**
	 * Forces the Celestial Object to blend with the background
	 * @return self
	 */
	public CelestialObject blends()
	{
		this.blends = true;
		return this;
	}
	
	public CelestialObject blendsDuringDay()
	{
		this.blendsDuringDay = true;
		
		return this;
	}
	
	public CelestialObject visibleDuringDay()
	{
		this.visibleDuringDay = true;
		
		return this;
	}
	
	//============================================================================================
	//******************************************Getters*******************************************
	//============================================================================================
	
	protected ResourceLocation getTexture(ClientLevel level, Camera camera, float partialTicks)
	{
		return this.texture;
	}
	
	protected float[] getUV(ClientLevel level, Camera camera, float partialTicks)
	{
		return FULL_UV;
	}
	
	protected boolean shouldRender(ClientLevel level, Camera camera)
	{
		return true;
	}
	
	protected boolean shouldBlend(ClientLevel level, Camera camera)
	{
		return this.blends;
	}
	
	protected boolean isVisibleDuringDay(ClientLevel level, Camera camera)
	{
		return this.visibleDuringDay;
	}
	
	protected boolean shouldBlendDuringDay(ClientLevel level, Camera camera)
	{
		return this.blendsDuringDay;
	}
	
	protected float getBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness;
		if(isVisibleDuringDay(level, camera))
		{
			if(shouldBlendDuringDay(level, camera))
			{
				brightness = level.getStarBrightness(partialTicks) * 2;
				brightness = brightness < 0.25F ? 0.25F : brightness;
				return brightness * (1.0F - level.getRainLevel(partialTicks));
			}
			else
				return 1.0F - level.getRainLevel(partialTicks);
		}
		else
		{
			brightness = level.getStarBrightness(partialTicks);
			brightness = StellarViewConfig.day_stars.get() && brightness < 0.5F ? 
					0.5F : brightness;
			if(StellarViewConfig.bright_stars.get())
				brightness = brightness * (1 + ((float) (15 - level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15)) / 15));
			return brightness * (1.0F - level.getRainLevel(partialTicks));
		}
	}
	
	//TODO Rename these 3
	protected abstract float getTheta(ClientLevel level, float partialTicks);
	
	protected abstract float getPhi(ClientLevel level, float partialTicks);
	
	protected abstract float getSize(ClientLevel level, float partialTicks);
	
	protected float getRotation(ClientLevel level, float partialTicks)
	{
		return this.rotation;
	}
	
	protected void renderObject(BufferBuilder bufferbuilder, Matrix4f lastMatrix, ResourceLocation texture, float[] uv,
			float size, float rotation, float theta, float phi, float brightness)
	{
		if(uv == null || uv.length < 4)
			uv = FULL_UV;
		
		float[] corner00 = StellarCoordinates.placeOnSphere(-size, -size, DEFAULT_DISTANCE, theta, phi, rotation);
		float[] corner10 = StellarCoordinates.placeOnSphere(size, -size, DEFAULT_DISTANCE, theta, phi, rotation);
		float[] corner11 = StellarCoordinates.placeOnSphere(size, size, DEFAULT_DISTANCE, theta, phi, rotation);
		float[] corner01 = StellarCoordinates.placeOnSphere(-size, size, DEFAULT_DISTANCE, theta, phi, rotation);
		
		if(brightness > 0.0F)
		{
			RenderSystem.setShaderColor(1, 1, 1, brightness);
			
			RenderSystem.setShaderTexture(0, texture);
	        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
	        bufferbuilder.vertex(lastMatrix, corner00[0], corner00[1], corner00[2]).uv(uv[0], uv[1]).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner10[0], corner10[1], corner10[2]).uv(uv[2], uv[1]).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner11[0], corner11[1], corner11[2]).uv(uv[2], uv[3]).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner01[0], corner01[1], corner01[2]).uv(uv[0], uv[3]).endVertex();
	        BufferUploader.drawWithShader(bufferbuilder.end());
		}
	}
	
	protected void renderHalo(BufferBuilder bufferbuilder, Matrix4f lastMatrix, float[] uv, float rotation, 
			float theta, float phi, float brightness)
	{
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		this.renderObject(bufferbuilder, lastMatrix, this.haloTexture, uv, this.haloSize, rotation, theta, phi, brightness);
		RenderSystem.defaultBlendFunc();
	}
	
	public void render(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f coords, boolean renderChildren)
	{
		if(!shouldRender(level, camera) || this == viewCenter)
			return;
		
		stack.pushPose();
		
		stack.mulPose(Axis.YP.rotationDegrees(skyAxisRotation.y));
        stack.mulPose(Axis.ZP.rotationDegrees(skyAxisRotation.z));
        stack.mulPose(Axis.XP.rotationDegrees(skyAxisRotation.x));
		
		float brightness = getBrightness(level, camera, partialTicks);
		
		if(shouldBlend(level, camera))
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		else
			RenderSystem.defaultBlendFunc();
		
		Matrix4f lastMatrix = stack.last().pose();
		float[] uv = getUV(level, camera, partialTicks);
		float rotation = getRotation(level, partialTicks);
		
		Vector3f relative = StellarCoordinates.relativeVector(coords, vievCenterCoords);
		
		Vector3f sphericalCoords = StellarCoordinates.cartesianToSpherical(relative);
		float theta = sphericalCoords.y;
		float phi = sphericalCoords.z;
		
		if(this.hasHalo)
			this.renderHalo(bufferbuilder, lastMatrix, uv, rotation, theta, phi, brightness);
		renderObject(bufferbuilder, lastMatrix, getTexture(level, camera, partialTicks), uv, getSize(level, partialTicks), rotation, theta, phi, brightness);

		RenderSystem.defaultBlendFunc();
		stack.popPose();
	}
}
