package net.povstalec.stellarview.api.celestials;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class StellarObject extends CelestialObject
{
	protected float size;
	
	protected float stellarX = 0;
	protected float stellarY = 0;
	protected float stellarZ = 0;
	
	protected float xOffset = 0;
	protected float yOffset = 0;
	protected float zOffset = 0;
	
	private float xAxisRotation;
	private float yAxisRotation;
	private float zAxisRotation;
	
	/**
	 * 
	 * @param texture Texture that will be visible when looking at the object from another location in the galaxy
	 */
	public StellarObject(ResourceLocation texture, float size)
	{
		super(texture);

		this.size = size;
	}

	@Override
	protected float getTheta(ClientLevel level, float partialTicks)
	{
		float x = -stellarX + xOffset;
		float y = -stellarY + yOffset;
		float z = -stellarZ + zOffset;
		
		return (float) StellarCoordinates.sphericalTheta(x, y, z);
	}
	
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		float x = -stellarX + xOffset;
		float y = -stellarY + yOffset;
		float z = -stellarZ + zOffset;

		return (float) StellarCoordinates.sphericalPhi(x, y, z);
	}
	
	protected float distanceSize(float size)
	{
		float x = -stellarX + xOffset;
		float y = -stellarY + yOffset;
		float z = -stellarZ + zOffset;
		float distance = x * x + y * y + z * z;
		
		return size * (1.0F / (float) Math.sqrt(distance));
	}
	
	protected float getSize(ClientLevel level, float partialTicks)
	{
		return distanceSize(size);
	}
	
	public StellarObject setGalacticPosition(float galacticX, float galacticY, float galacticZ)
	{
		this.stellarX = galacticX;
		this.stellarY = galacticY;
		this.stellarZ = galacticZ;
		
		return this;
	}
	
	public StellarObject setOffset(float xOffset, float yOffset, float zOffset)
	{
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		
		return this;
	}
	
	public float getX()
	{
		return this.stellarX;
	}
	
	public float getY()
	{
		return this.stellarY;
	}
	
	public float getZ()
	{
		return this.stellarZ;
	}
	
	public float getXAxisRotation()
	{
		return this.xAxisRotation;
	}
	
	public float getYAxisRotation()
	{
		return this.yAxisRotation;
	}
	
	public float getZAxisRotation()
	{
		return this.zAxisRotation;
	}
}
