package net.povstalec.stellarview;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class StellarView implements ModInitializer
{
	public static final String MODID = "stellarview";
	
	public static final String ENHANCED_CELESTIALS_MODID = "enhancedcelestials";
	public static final String LUNAR_MODID = "lunar";
	public static final String TWILIGHT_FOREST_MODID = "twilightforest";
	public static final String AETHER_MODID = "aether";
	
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	
	private static Optional<Boolean> isEnhancedCelestialsLoaded = Optional.empty();
	private static Optional<Boolean> isLunarLoaded = Optional.empty();

	@Override
	public void onInitialize()
	{
		//StellarViewReloadEvent.EVENT.register((jsonMap, manager, filler) -> true);
	}
	
	public static boolean isEnhancedCelestialsLoaded() //TODO Handle Enhanced Celestials Compatibility
	{
		if(isEnhancedCelestialsLoaded.isEmpty())
			isEnhancedCelestialsLoaded = Optional.of(FabricLoader.getInstance().isModLoaded(ENHANCED_CELESTIALS_MODID));
		
		return isEnhancedCelestialsLoaded.get();
	}
	
	public static boolean isLunarLoaded()
	{
		if(isLunarLoaded.isEmpty())
			isLunarLoaded = Optional.of(FabricLoader.getInstance().isModLoaded(LUNAR_MODID));
		
		return isLunarLoaded.get();
	}
}