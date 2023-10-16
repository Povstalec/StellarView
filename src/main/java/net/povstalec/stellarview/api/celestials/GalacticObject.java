package net.povstalec.stellarview.api.celestials;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class GalacticObject extends CelestialObject
{
	protected float size;
	
	protected float galacticX = 0;
	protected float galacticY = 0;
	protected float galacticZ = 0;
	
	protected float xOffset = 0;
	protected float yOffset = 0;
	protected float zOffset = 0;
	
	/**
	 * 
	 * @param texture Texture that will be visible when looking at the object from another location in the galaxy
	 */
	public GalacticObject(ResourceLocation texture, float size)
	{
		super(texture);

		this.size = size;
	}

	@Override
	protected float getTheta(ClientLevel level, float partialTicks)
	{
		float x = -galacticX + xOffset;
		float y = -galacticY + yOffset;
		float z = -galacticZ + zOffset;
		
		return (float) StellarCoordinates.sphericalTheta(x, y, z);
	}
	
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		float x = -galacticX + xOffset;
		float y = -galacticY + yOffset;
		float z = -galacticZ + zOffset;

		return (float) StellarCoordinates.sphericalPhi(x, y, z);
	}
	
	protected float distanceSize(float size)
	{
		float x = -galacticX + xOffset;
		float y = -galacticY + yOffset;
		float z = -galacticZ + zOffset;
		float distance = x * x + y * y + z * z;
		
		return size * (1.0F / (float) Math.sqrt(distance));
	}
	
	protected float getSize(ClientLevel level, float partialTicks)
	{
		return distanceSize(size);
	}
	
	public GalacticObject setGalacticPosition(float galacticX, float galacticY, float galacticZ)
	{
		this.galacticX = galacticX;
		this.galacticY = galacticY;
		this.galacticZ = galacticZ;
		
		return this;
	}
	
	public GalacticObject setOffset(float xOffset, float yOffset, float zOffset)
	{
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		
		return this;
	}
	
	@Override
	protected void renderObject(BufferBuilder bufferbuilder, Matrix4f lastMatrix, ResourceLocation texture, float[] uv,
			float size, float rotation, float theta, float phi, float brightness)
	{
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		super.renderObject(bufferbuilder, lastMatrix, texture, uv, size, rotation, theta, phi, brightness);
	}
}
