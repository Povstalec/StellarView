package net.povstalec.stellarview.api.client.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;

import java.util.HashMap;
import java.util.Map;

@Cancelable
public class StellarViewEffectsReloadEvent extends Event
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
