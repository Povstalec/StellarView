package net.povstalec.stellarview.api.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;

import java.util.HashMap;
import java.util.Map;

public interface SpaceRendererReloadEvent
{
	Event<SpaceRendererReloadEvent> EVENT = EventFactory.createArrayBacked(SpaceRendererReloadEvent.class,
			(listeners) -> (spaceObjects) ->
			{
				for (SpaceRendererReloadEvent listener : listeners)
				{
					if(listener.onReload(spaceObjects))
						return true;
				}
				
				return false;
			});
	
	/**
	 * Returns true if canceled, otherwise false
	 * @param spaceObjects
	 * @return
	 */
	boolean onReload(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects);
}
