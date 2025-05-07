package net.povstalec.stellarview.api.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;

import java.util.HashMap;

public interface ViewCenterReloadEvent
{
	Event<ViewCenterReloadEvent> EVENT = EventFactory.createArrayBacked(ViewCenterReloadEvent.class,
			(listeners) -> (spaceObjects, viewCenters) ->
			{
				for (ViewCenterReloadEvent listener : listeners)
				{
					if(listener.onReload(spaceObjects, viewCenters))
						return true;
				}
				
				return false;
			});
	
	/**
	 * Returns true if canceled, otherwise false
	 * @param spaceObjects
	 * @param viewCenters
	 * @return
	 */
	boolean onReload(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects, HashMap<ResourceLocation, ViewCenter> viewCenters);
}
