package net.povstalec.stellarview.api.client.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;

import java.util.HashMap;
import java.util.Map;

@Cancelable
public class SpaceRendererReloadEvent extends Event
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
