package net.povstalec.stellarview.api.client.events;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;

import java.util.HashMap;
import java.util.Map;

@Cancelable
public class ViewCenterReloadEvent extends Event
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
