package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public class Planet extends OrbitingCelestialObject
{
	private List<Moon> moons = new ArrayList<Moon>();
	
	public Planet(ResourceLocation texture, float size)
	{
		super(texture, size);
	}
	
	
}
