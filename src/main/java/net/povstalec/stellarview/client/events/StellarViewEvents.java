package net.povstalec.stellarview.client.events;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class StellarViewEvents
{
	public static boolean onReload(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler)
	{
		return MinecraftForge.EVENT_BUS.post(new StellarViewReloadEvent(jsonMap, manager, filler));
	}
}
