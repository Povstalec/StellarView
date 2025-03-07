package net.povstalec.stellarview.api.client.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;

import java.util.HashMap;
import java.util.Map;

public class SpaceRendererReloadEvent extends Event implements ICancellableEvent
{
	private final HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects;
	
	public SpaceRendererReloadEvent(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects)
	{
		this.spaceObjects = spaceObjects;
	}
	
	public Map<ResourceLocation, SpaceObjectRenderer<?>> getRenderedSpaceObjects()
	{
		return spaceObjects;
	}
}
