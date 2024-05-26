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
		super(Planet.VENUS_TEXTURE, 0);
	}
}
