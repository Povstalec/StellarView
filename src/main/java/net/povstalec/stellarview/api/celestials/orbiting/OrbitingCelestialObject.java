package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestials.StellarObject;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public class OrbitingCelestialObject extends StellarObject
{
	//protected float mass;
	protected float angularVelocity = 0;
	protected float distance = 0;
	
	protected float initialTheta = 0;
	protected float initialPhi = 0;
	protected float rotation = 0;
	
	protected List<OrbitingCelestialObject> orbitingObjects = new ArrayList<OrbitingCelestialObject>();
	
	public OrbitingCelestialObject(ResourceLocation texture, float size)
	{
		super(texture, size);
		//this.mass = mass;
	}

	@Override
	protected float getTheta(ClientLevel level, float partialTicks)
	{
		return this.initialTheta;
	}

	@Override
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		return this.initialPhi + (float) Math.toRadians(angularVelocity * ((float) level.getDayTime() / 24000));
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
	
	public final OrbitingCelestialObject addOrbitingObject(OrbitingCelestialObject object, float distance, float angularVelocity, float initialPhi)
	{
		if(object.primaryBody.isPresent())
		{
			StellarView.LOGGER.error("Object is already orbiting a primary body");
			return this;
		}
		
		object.primaryBody = Optional.of(this);
		object.distance = distance;
		object.angularVelocity = angularVelocity;
		object.initialPhi = initialPhi;
		
		this.orbitingObjects.add(object);
		
		return this;
	}
	
	public Vector3f getRelativeCartesianCoordinates(ClientLevel level, float partialTicks)
	{
		return StellarCoordinates.sphericalToCartesian(new Vector3f(distance, getTheta(level, partialTicks), getPhi(level, partialTicks)));
	}
	
	public float getDistanceSquaredFromViewCenter(Vector3f vievCenterCoords, Vector3f parentCoords, ClientLevel level, float partialTicks)
	{
		Vector3f relativeCoords = getRelativeCartesianCoordinates(level, partialTicks);
		Vector3f absoluteCoords = StellarCoordinates.absoluteVector(parentCoords, relativeCoords);
		
		return StellarCoordinates.relativeVector(vievCenterCoords, absoluteCoords).lengthSquared();
	}
	
	@Override
	public void render(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f parentCoords)
	{
		Vector3f relativeCoords = getRelativeCartesianCoordinates(level, partialTicks);
		Vector3f absoluteCoords = StellarCoordinates.absoluteVector(parentCoords, relativeCoords);
		float distanceSquared = StellarCoordinates.relativeVector(absoluteCoords, vievCenterCoords).lengthSquared();
		
		// Renders objects behind it
		this.orbitingObjects.stream().forEach(orbitingObject ->
		{
			if(getDistanceSquaredFromViewCenter(vievCenterCoords, absoluteCoords, level, partialTicks) > distanceSquared)
				orbitingObject.render(viewCenter, vievCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, absoluteCoords);
		});
		
		super.render(viewCenter, vievCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, absoluteCoords);

		// Renders objects in front of it
		this.orbitingObjects.stream().forEach(orbitingObject ->
		{
			if(getDistanceSquaredFromViewCenter(vievCenterCoords, absoluteCoords, level, partialTicks) <= distanceSquared)
				orbitingObject.render(viewCenter, vievCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, absoluteCoords);
		});
	}
	
	//TODO
	public void renderFrom(OrbitingCelestialObject viewCenter, Vector3f viewCenterCoords, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation)
	{
		Vector3f absoluteCoords = StellarCoordinates.absoluteVector(viewCenterCoords, getRelativeCartesianCoordinates(level, partialTicks));
		if(primaryBody.isPresent())
		{
			if(primaryBody.get() instanceof OrbitingCelestialObject parent)
			{
				parent.renderFrom(viewCenter, absoluteCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation);
			}
			//else if(primaryBody.get() instanceof StarField starField)
			//	starField.render(level, camera, partialTicks, partialTicks, stack, null, null, bufferbuilder, xRotation, yRotation, zRotation);
		}
		else
			this.render(viewCenter, absoluteCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, new Vector3f(0, 0, 0));
		
	}
	
	public void renderLocalSky(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		this.renderFrom(this, new Vector3f(0, 0, 0), level, camera, partialTicks, stack, bufferbuilder, new Vector3f(0, 0, 0));
	}
}
