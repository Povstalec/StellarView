package net.povstalec.stellarview.api.celestials;

import java.util.Optional;

import com.mojang.math.Vector3f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public abstract class StellarObject extends CelestialObject
{
	protected float size;
	
	protected Vector3f coordinates = new Vector3f(0, 0, 0);
	protected Vector3f offsetCoords = new Vector3f(0, 0, 0);

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
	protected float getTetha(ClientLevel level, float partialTicks)
	{
		float x = -coordinates.x() + offsetCoords.x();
		float y = -coordinates.y() + offsetCoords.y();
		float z = -coordinates.z() + offsetCoords.z();
		
		return (float) StellarCoordinates.sphericalTheta(x, y, z);
	}

	@Override
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		float x = -coordinates.x() + offsetCoords.x();
		float y = -coordinates.y() + offsetCoords.y();
		float z = -coordinates.z() + offsetCoords.z();

		return (float) StellarCoordinates.sphericalPhi(x, y, z);
	}
	
	protected float distanceSize(float size)
	{
		float x = -coordinates.x() + offsetCoords.x();
		float y = -coordinates.y() + offsetCoords.y();
		float z = -coordinates.z() + offsetCoords.z();
		float distance = x * x + y * y + z * z;
		
		return size * (1.0F / (float) Math.sqrt(distance));
	}

	@Override
	protected float getSize(ClientLevel level, float partialTicks)
	{
		return distanceSize(size);
	}
	
	public StellarObject setGalacticPosition(float galacticX, float galacticY, float galacticZ)
	{
		this.coordinates.set(galacticX, galacticY, galacticZ);
		
		return this;
	}
	
	public StellarObject setOffset(float xOffset, float yOffset, float zOffset)
	{
		this.offsetCoords.set(xOffset, yOffset, zOffset);
		
		return this;
	}
	
	public StellarObject setRotation(float xRotation, float yRotation, float zRotation)
	{
		this.axisRotation.set(xRotation, yRotation, zRotation);
		
		return this;
	}
	
	public StellarObject setRotation(Vector3f rotation)
	{
		this.axisRotation.set(rotation.x(), rotation.y(), rotation.z());
		
		return this;
	}
	
	public float getX()
	{
		return this.coordinates.x();
	}
	
	public float getY()
	{
		return this.coordinates.y();
	}
	
	public float getZ()
	{
		return this.coordinates.z();
	}
	
	public Vector3f getAxisRotation()
	{
		return this.axisRotation;
	}
	
	/*@Override
	public void render(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f parentCoords)
	{
		Vector3f relativeCoords = getRelativeCartesianCoordinates(level, partialTicks);
		Vector3f absoluteCoords = StellarCoordinates.absoluteVector(parentCoords, relativeCoords);
		
		super.render(viewCenter, vievCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, absoluteCoords);
	}*/
}
