package net.povstalec.stellarview.client.render;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.resourcepack.effects.MeteorEffect;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;

import javax.annotation.Nullable;
import java.util.HashMap;

public class StellarViewEffects
{
	private static HashMap<ResourceLocation, StarInfo> STAR_TYPES = new HashMap<>();
	private static HashMap<ResourceLocation, DustCloudInfo> DUST_CLOUD_TYPES = new HashMap<>();
	
	public static void reset()
	{
		STAR_TYPES.clear();
		DUST_CLOUD_TYPES.clear();
	}
	
	public static void setupEffects(HashMap<ResourceLocation, StarInfo> starTypes, HashMap<ResourceLocation, DustCloudInfo>  dustCloudTypes)
	{
		STAR_TYPES = starTypes;
		DUST_CLOUD_TYPES = dustCloudTypes;
	}
	
	public static boolean hasStarInfo(ResourceLocation location)
	{
		return STAR_TYPES.containsKey(location);
	}
	
	@Nullable
	public static StarInfo getStarInfo(ResourceLocation location)
	{
		return STAR_TYPES.get(location);
	}
	
	public static boolean hasDustCloudInfo(ResourceLocation location)
	{
		return DUST_CLOUD_TYPES.containsKey(location);
	}
	
	@Nullable
	public static DustCloudInfo getDustCloudInfo(ResourceLocation location)
	{
		return DUST_CLOUD_TYPES.get(location);
	}
}
