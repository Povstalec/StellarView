package net.povstalec.stellarview.api.client.events;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;

import java.util.HashMap;
import java.util.Map;

public class StellarViewEvents
{
	public static boolean onReload(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler)
	{
		return StellarViewReloadEvent.EVENT.invoker().onReload(jsonMap, manager, filler);
	}
	
	public static boolean onViewCenterReload(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects, HashMap<ResourceLocation, ViewCenter> viewCenters)
	{
		return ViewCenterReloadEvent.EVENT.invoker().onReload(spaceObjects, viewCenters);
	}
	
	public static boolean onEffectsReload(HashMap<ResourceLocation, StarInfo> starTypes, HashMap<ResourceLocation, DustCloudInfo> dustCloudTypes)
	{
		return StellarViewEffectsReloadEvent.EVENT.invoker().onReload(starTypes, dustCloudTypes);
	}
	
	public static boolean onSpaceRendererReload(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects)
	{
		return SpaceRendererReloadEvent.EVENT.invoker().onReload(spaceObjects);
	}
}
