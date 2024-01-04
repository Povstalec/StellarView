package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestials.StarField;
import net.povstalec.stellarview.api.celestials.StellarObject;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class OrbitingCelestialObject extends StellarObject
{
	protected float angularVelocity = 0;
	protected float distance = 0;
	
	protected float initialTetha = 0;
	protected float initialPhi = 0;
	
	protected List<OrbitingCelestialObject> orbitingObjects = new ArrayList<OrbitingCelestialObject>();
	
	public OrbitingCelestialObject(ResourceLocation texture, float size)
	{
		super(texture, size);
	}

	@Override
	protected float getTetha(ClientLevel level, float partialTicks)
	{
		return this.initialTetha * (float) Math.sin(Math.toRadians(angularVelocity * ((float) level.getDayTime() / 24000)));
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
	
	public final OrbitingCelestialObject addOrbitingObject(OrbitingCelestialObject object, float distance, float angularVelocity, float initialPhi, float initialTetha, float rotation)
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
		object.initialTetha = initialTetha;
		this.rotation = rotation;
		
		this.orbitingObjects.add(object);
		
		return this;
	}
	
	public Vector3f getRelativeCartesianCoordinates(ClientLevel level, float partialTicks)
	{
		return StellarCoordinates.sphericalToCartesian(new Vector3f(distance, getTetha(level, partialTicks), getPhi(level, partialTicks)));
	}
	
	public float getDistanceSquaredFromViewCenter(Vector3f vievCenterCoords, Vector3f parentCoords, ClientLevel level, float partialTicks)
	{
		Vector3f relativeCoords = getRelativeCartesianCoordinates(level, partialTicks);
		Vector3f absoluteCoords = StellarCoordinates.absoluteVector(parentCoords, relativeCoords);
		
		return StellarCoordinates.relativeVector(vievCenterCoords, absoluteCoords).lengthSquared();
	}
	
	@Override
	protected Vector3f findRelative(Vector3f vievCenterCoords, Vector3f coords)
	{
		return StellarCoordinates.relativeVector(coords, vievCenterCoords);
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
	
	public void renderFrom(OrbitingCelestialObject viewCenter, Vector3f viewCenterCoords, ClientLevel level, Camera camera, float partialTicks,
			PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f axisRotation)
	{
		Vector3f absoluteCoords = StellarCoordinates.absoluteVector(viewCenterCoords, getRelativeCartesianCoordinates(level, partialTicks));
		if(primaryBody.isPresent())
		{
			if(primaryBody.get() instanceof OrbitingCelestialObject parent)
			{
				parent.renderFrom(viewCenter, absoluteCoords, level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder, skyAxisRotation, StellarCoordinates.addVectors(getAxisRotation(), axisRotation));
			}
			else if(primaryBody.get() instanceof StarField starField)
			{
				if(!StellarViewConfig.disable_stars.get())
				{
					float rain = 1.0F - level.getRainLevel(partialTicks);
		        	starField.render(this, viewCenter, viewCenterCoords, level, camera, partialTicks, rain, stack, projectionMatrix, setupFog, bufferbuilder, skyAxisRotation, StellarCoordinates.addVectors(getAxisRotation(), axisRotation), new Vector3f(0, 0, 0));
				}
			}
		}
		else
			this.render(viewCenter, absoluteCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, new Vector3f(0, 0, 0));
		
	}
	
	public void renderLocalSky(ClientLevel level, Camera camera, float partialTicks,
			PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		this.renderFrom(this, new Vector3f(0, 0, 0), level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
	}
}
