package net.povstalec.stellarview.api.celestial_objects;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.config.StellarViewConfigValue;

public class RarityObject extends CelestialObject
{
	protected StellarViewConfigValue.IntValue value;
	protected int defaultRarity;
	
	public RarityObject(ResourceLocation texture, float distance, float size, int defaultRarity)
	{
		super(texture, distance, size);
		this.defaultRarity = defaultRarity;
	}
	
	public RarityObject setRarityValue(StellarViewConfigValue.IntValue value)
	{
		this.value = value;
		
		return this;
	}
	
	public RarityObject setDefaultRarity(int defaultRarity)
	{
		this.defaultRarity = defaultRarity;
		
		return this;
	}
	
	public int getRarity()
	{
		return value == null ? defaultRarity : value.get();
	}
}
