package net.povstalec.stellarview.api.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;

import java.util.HashMap;

public interface StellarViewEffectsReloadEvent
{
	Event<StellarViewEffectsReloadEvent> EVENT = EventFactory.createArrayBacked(StellarViewEffectsReloadEvent.class,
			(listeners) -> (starTypes, dustCloudTypes) ->
			{
				for (StellarViewEffectsReloadEvent listener : listeners)
				{
					if(listener.onReload(starTypes, dustCloudTypes))
						return true;
				}
				
				return false;
			});
	
	/**
	 * Returns true if canceled, otherwise false
	 * @param starTypes
	 * @param dustCloudTypes
	 * @return
	 */
	boolean onReload(HashMap<ResourceLocation, StarInfo> starTypes, HashMap<ResourceLocation, DustCloudInfo> dustCloudTypes);
}
