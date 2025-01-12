package net.povstalec.stellarview.client.events;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

@Cancelable
public class StellarViewReloadEvent extends Event
{
	private final Map<ResourceLocation, JsonElement> jsonMap;
	private final ResourceManager manager;
	private final ProfilerFiller filler;
	
	public StellarViewReloadEvent(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler)
	{
		this.jsonMap = jsonMap;
		this.manager = manager;
		this.filler = filler;
	}
	
	public Map<ResourceLocation, JsonElement> getJsonMap()
	{
		return jsonMap;
	}
	
	public ResourceManager getManager()
	{
		return manager;
	}
	
	public ProfilerFiller getFiller()
	{
		return filler;
	}
}
