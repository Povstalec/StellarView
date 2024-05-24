package net.povstalec.stellarview.api.celestials.orbiting;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

public class Barycenter extends OrbitingCelestialObject
{
	public Barycenter()
	{
		super(null, 0);
	}
	
	// Barycenter is invisible, no point in rendering it
	@Override
	public void render(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f parentCoords)
	{
		
	}
}
