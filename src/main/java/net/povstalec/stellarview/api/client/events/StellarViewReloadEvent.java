package net.povstalec.stellarview.api.client.events;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public interface StellarViewReloadEvent
{
	Event<StellarViewReloadEvent> EVENT = EventFactory.createArrayBacked(StellarViewReloadEvent.class,
			(listeners) -> (jsonMap, manager, filler) ->
			{
				for (StellarViewReloadEvent listener : listeners)
				{
					if(listener.onReload(jsonMap, manager, filler))
						return true;
				}
				
				return false;
			});
	
	/**
	 * Returns true if canceled, otherwise false
	 * @param jsonMap
	 * @param manager
	 * @param filler
	 * @return
	 */
	boolean onReload(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler);
}
