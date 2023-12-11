package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public class Planet extends OrbitingCelestialObject
{
	private Optional<Integer> rotationPeriod = Optional.empty();
	
	public Planet(ResourceLocation texture, float size)
	{
		this(texture, size, 0);
	}
	
	public Planet(ResourceLocation texture, float size, int rotationPeriod)
	{
		super(texture, size);
		if(rotationPeriod > 0)
			this.rotationPeriod = Optional.of(rotationPeriod);
	}
	
	public Optional<Integer> getRotationPeriod()
	{
		return this.rotationPeriod;
	}
}
