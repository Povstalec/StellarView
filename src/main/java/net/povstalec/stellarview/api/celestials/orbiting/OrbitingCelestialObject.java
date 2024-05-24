package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

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
	
	protected float getAngularVelocity(ClientLevel level, float partialTicks)
	{
		return this.angularVelocity;
	}

	@Override
	protected float getTetha(ClientLevel level, float partialTicks)
	{
		return this.initialTetha * (float) Math.sin(Math.toRadians(getAngularVelocity(level, partialTicks) * ((float) level.getDayTime() / 24000)));
	}

	@Override
	protected float getPhi(ClientLevel level, float partialTicks)
	{
		return this.initialPhi + (float) Math.toRadians(getAngularVelocity(level, partialTicks) * ((float) level.getDayTime() / 24000));
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
		
		return StellarCoordinates.lengthSquared(StellarCoordinates.relativeVector(vievCenterCoords, absoluteCoords));
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
		float distanceSquared = StellarCoordinates.lengthSquared(StellarCoordinates.relativeVector(absoluteCoords, vievCenterCoords));
		
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
				//TODO
				//this.setGalacticPosition(this.getX()-0.001F, this.getY(), this.getZ());
				//starField.setStarBuffer(this.getX()-0.001F, this.getY(), this.getZ(), this.axisRotation.x, this.axisRotation.y, this.axisRotation.z);
				
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

	/**
	 * Approximate E (Eccentric Anomaly) for a given
	 * e (eccentricity) and M (Mean Anomaly)
	 * where e < 1 and E and M are given in radians
	 * 
	 * This is performed by finding the root of the
	 * function f(E) = E - e*sin(E) - M(t)
	 * via Newton's method, where the derivative of
	 * f(E) with respect to E is 
	 * f'(E) = 1 - e*cos(E)
	 * 
	 * @param eccentricity
	 * @param meanAnomaly
	 * @return
	 */
	public static double approximateEccentricAnomaly(double eccentricity, double meanAnomaly) {
		double E = meanAnomaly;
		// Perform 12 iterations
		// No clue what would be an appropriate amount in practice
		for (int i=0;i<12;i++) {
			E = E - 
				(E - eccentricity * Math.sin(E) - meanAnomaly) /
				(1 - eccentricity * Math.cos(E));
		}
		return E;
	}
}
