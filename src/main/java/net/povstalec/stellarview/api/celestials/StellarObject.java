package net.povstalec.stellarview.api.celestials;

import java.util.Optional;

import org.joml.Vector3f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class StellarObject extends CelestialObject
{
	protected float size;
	
	protected Vector3f coordinates = new Vector3f(0, 0, 0);
	
	protected float xOffset = 0;
	protected float yOffset = 0;
	protected float zOffset = 0;
	
	private float xAxisRotation;
	private float yAxisRotation;
	private float zAxisRotation;

	protected Optional<StellarObject> primaryBody = Optional.empty();
	
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
		float x = -coordinates.x + xOffset;
		float y = -coordinates.y + yOffset;
		float z = -coordinates.z + zOffset;
		
		return (float) StellarCoordinates.sphericalTheta(x, y, z);
	}
	
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		float x = -coordinates.x + xOffset;
		float y = -coordinates.y + yOffset;
		float z = -coordinates.z + zOffset;

		return (float) StellarCoordinates.sphericalPhi(x, y, z);
	}
	
	protected float distanceSize(float size)
	{
		float x = -coordinates.x + xOffset;
		float y = -coordinates.y + yOffset;
		float z = -coordinates.z + zOffset;
		float distance = x * x + y * y + z * z;
		
		return size * (1.0F / (float) Math.sqrt(distance));
	}
	
	protected float getSize(ClientLevel level, float partialTicks)
	{
		return distanceSize(size);
	}
	
	public StellarObject setGalacticPosition(float galacticX, float galacticY, float galacticZ)
	{
		this.coordinates.x = galacticX;
		this.coordinates.y = galacticY;
		this.coordinates.z = galacticZ;
		
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
		return this.coordinates.x;
	}
	
	public float getY()
	{
		return this.coordinates.y;
	}
	
	public float getZ()
	{
		return this.coordinates.z;
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
