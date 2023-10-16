package net.povstalec.stellarview.api.celestials.orbiting;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.celestials.CelestialObject;

public class OrbitingCelestialObject extends CelestialObject
{
	protected float distance;
	protected float size;

	protected ResourceLocation haloTexture;
	protected float haloSize;
	protected boolean hasHalo = false;

	protected boolean blends = false;
	protected boolean blendsDuringDay = false;
	protected boolean visibleDuringDay = false;
	
	protected float initialTheta = 0;
	protected float initialPhi = 0;
	protected float rotation = 0;
	
	public OrbitingCelestialObject(ResourceLocation texture, float size)
	{
		super(texture);
		this.size = size;
	}
	
	/**
	 * Forces the Celestial Object to blend with the background
	 * @return self
	 */
	public OrbitingCelestialObject blends()
	{
		this.blends = true;
		return this;
	}
	
	public OrbitingCelestialObject blendsDuringDay()
	{
		this.blendsDuringDay = true;
		
		return this;
	}
	
	public OrbitingCelestialObject visibleDuringDay()
	{
		this.visibleDuringDay = true;
		
		return this;
	}
	
	/**
	 * Creates a Halo around the Celestial Object
	 * @param haloTexture Texture used for the Halo
	 * @param haloSize Size of the Halo
	 * @return self
	 */
	public OrbitingCelestialObject halo(ResourceLocation haloTexture, float haloSize)
	{
		this.haloTexture = haloTexture;
		this.haloSize = haloSize;
		this.hasHalo = true;
		return this;
	}
	
	/**
	 * Initial Theta for Spherical Coordinates
	 * @param initialTheta Initial Theta
	 * @return self
	 */
	public OrbitingCelestialObject initialTheta(float initialTheta)
	{
		this.initialTheta = initialTheta;
		return this;
	}

	/**
	 * Initial Phi for Spherical Coordinates
	 * @param initialPhi Initial Phi
	 * @return self
	 */
	public OrbitingCelestialObject initialPhi(float initialPhi)
	{
		this.initialPhi = initialPhi;
		return this;
	}
	
	@Override
	protected boolean shouldRender(ClientLevel level, Camera camera)
	{
		return true;
	}
	
	@Override
	protected boolean shouldBlend(ClientLevel level, Camera camera)
	{
		return this.blends;
	}
	
	@Override
	protected boolean isVisibleDuringDay(ClientLevel level, Camera camera)
	{
		return this.visibleDuringDay;
	}
	
	@Override
	protected boolean shouldBlendDuringDay(ClientLevel level, Camera camera)
	{
		return this.blendsDuringDay;
	}

	@Override
	protected float getTheta(ClientLevel level, float partialTicks)
	{
		return this.initialTheta;
	}

	@Override
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		return this.initialTheta;
	}

	@Override
	protected float getSize(ClientLevel level, float partialTicks)
	{
		return this.size;
	}
	
	protected void renderHalo(BufferBuilder bufferbuilder, Matrix4f lastMatrix, float[] uv, float rotation, 
			float theta, float phi, float brightness)
	{
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		super.renderObject(bufferbuilder, lastMatrix, this.haloTexture, uv, this.haloSize, rotation, theta, phi, brightness);
		RenderSystem.defaultBlendFunc();
	}
	
	@Override
	protected void renderObject(BufferBuilder bufferbuilder, Matrix4f lastMatrix, ResourceLocation texture, float[] uv,
			float size, float rotation, float theta, float phi, float brightness)
	{
		if(this.hasHalo)
			this.renderHalo(bufferbuilder, lastMatrix, uv, rotation, theta, phi,  brightness);
		super.renderObject(bufferbuilder, lastMatrix, texture, uv, size, rotation, theta, phi, brightness);
	}
}
