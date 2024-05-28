package net.povstalec.stellarview.api.celestials.orbiting;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public class Barycenter extends OrbitingCelestialObject
{
	public Barycenter(ResourceLocation texture, float size)
	{
		super(texture, size);
	}

	// Barycenter is invisible, no point in rendering it
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
		
		// Renders objects in front of it
		this.orbitingObjects.stream().forEach(orbitingObject ->
		{
			if(getDistanceSquaredFromViewCenter(vievCenterCoords, absoluteCoords, level, partialTicks) <= distanceSquared)
				orbitingObject.render(viewCenter, vievCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, absoluteCoords);
		});
	}
}
