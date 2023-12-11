package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestials.StellarObject;

public class OrbitingCelestialObject extends StellarObject
{
	//protected float mass;
	protected float angularVelocity = 0;
	protected float distance = 0;
	
	protected float initialTheta = 0;
	protected float initialPhi = 0;
	protected float rotation = 0;
	
	private Optional<StellarObject> primaryBody = Optional.empty();
	private List<OrbitingCelestialObject> orbitingObjects = new ArrayList<OrbitingCelestialObject>();
	
	public OrbitingCelestialObject(ResourceLocation texture, float size)
	{
		super(texture, size);
		//this.mass = mass;
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
	protected float getTheta(ClientLevel level, float partialTicks)
	{
		return this.initialTheta;
	}

	@Override
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		return this.initialPhi - (float) Math.toRadians(angularVelocity * ((float) level.getDayTime() / 24000));
	}

	@Override
	protected float getSize(ClientLevel level, float partialTicks)
	{
		return this.size;
	}
	
	//TODO
	/*protected float getMass(ClientLevel level, float partialTicks)
	{
		return this.mass;
	}
	
	protected float getVelocity(ClientLevel level, float partialTicks, float parentMass)
	{
		return (float) Math.sqrt(G * parentMass / distance);
	}
	
	protected float getAngularVelocity(ClientLevel level, float partialTicks, float parentMass)
	{
		return getVelocity(level, partialTicks, parentMass) / distance;
	}*/
	
	public final OrbitingCelestialObject addOrbitingObject(OrbitingCelestialObject object, float distance, float angularVelocity)
	{
		if(object.primaryBody.isPresent())
		{
			StellarView.LOGGER.error("Object is already orbiting a primary body");
			return this;
		}
		
		object.primaryBody = Optional.of(this);
		object.distance = distance;
		object.angularVelocity = angularVelocity;
		
		this.orbitingObjects.add(object);
		
		return this;
	}
	
	public void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			float xRotation, float yRotation, float zRotation)
	{
		super.render(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation);
		
		this.orbitingObjects.stream().forEach(orbitingObject ->
		{
			orbitingObject.render(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation);
		});
	}
	
	//TODO
	public void renderFromHere(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			float xRotation, float yRotation, float zRotation)
	{
		if(primaryBody.isPresent() && primaryBody.get() instanceof OrbitingCelestialObject parent)
			parent.renderFromHere(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation);
		else
			this.render(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation);
		
	}
}
