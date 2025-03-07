package net.povstalec.stellarview.api.client.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;

import java.util.HashMap;
import java.util.Map;

public class ViewCenterReloadEvent extends Event implements ICancellableEvent
{
	private final HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects;
	private final HashMap<ResourceLocation, ViewCenter> viewCenters;
	
	public ViewCenterReloadEvent(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects, HashMap<ResourceLocation, ViewCenter> viewCenters)
	{
		this.spaceObjects = spaceObjects;
		this.viewCenters = viewCenters;
	}
	
	public Map<ResourceLocation, SpaceObjectRenderer<?>> getRenderedSpaceObjects()
	{
		return spaceObjects;
	}
	
	public Map<ResourceLocation, ViewCenter> getViewCenters()
	{
		return viewCenters;
	}
}
