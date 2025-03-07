package net.povstalec.stellarview.api.client.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;

import java.util.HashMap;
import java.util.Map;

public class StellarViewEffectsReloadEvent extends Event implements ICancellableEvent
{
	private final HashMap<ResourceLocation, StarInfo> starTypes;
	private final HashMap<ResourceLocation, DustCloudInfo> dustCloudTypes;
	
	public StellarViewEffectsReloadEvent(HashMap<ResourceLocation, StarInfo> starTypes, HashMap<ResourceLocation, DustCloudInfo> dustCloudTypes)
	{
		this.starTypes = starTypes;
		this.dustCloudTypes = dustCloudTypes;
	}
	
	public Map<ResourceLocation, StarInfo> getStarTypes()
	{
		return starTypes;
	}
	
	public Map<ResourceLocation, DustCloudInfo> getDustCloudTypes()
	{
		return dustCloudTypes;
	}
}
