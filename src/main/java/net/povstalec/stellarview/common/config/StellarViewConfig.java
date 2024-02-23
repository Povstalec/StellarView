package net.povstalec.stellarview.common.config;

import java.io.File;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.stellarview.StellarView;

@Mod.EventBusSubscriber
public class StellarViewConfig
{
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec CLIENT_CONFIG;
	
	public static StellarViewConfigValue.BooleanValue replace_overworld;
	public static StellarViewConfigValue.BooleanValue replace_end;
	
	public static StellarViewConfigValue.BooleanValue disable_stars;
	public static StellarViewConfigValue.BooleanValue day_stars;
	public static StellarViewConfigValue.BooleanValue bright_stars;
	
	public static StellarViewConfigValue.IntValue rotation_multiplier;
	
	static
	{
		CLIENT_BUILDER.push("Stellar View Client Config");
		
		generalClientConfig(CLIENT_BUILDER);

		CLIENT_BUILDER.pop();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	public static void loadConfig(ForgeConfigSpec config, String path)
	{
		StellarView.LOGGER.info("Loading Config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
		StellarView.LOGGER.info("Built config: " + path);
		file.load();
		StellarView.LOGGER.info("Loaded Config: " + path);
		config.setConfig(file);
	}
	
	private static void generalClientConfig(ForgeConfigSpec.Builder client)
	{
		replace_overworld = new StellarViewConfigValue.BooleanValue(client, "client.replace_overworld", 
				true, 
				"Replaces the Vanilla Overworld sky with Stellar View sky");
		
		replace_end = new StellarViewConfigValue.BooleanValue(client, "client.replace_end", 
				false, 
				"Replaces the Vanilla End sky with Stellar View sky");
		
		disable_stars = new StellarViewConfigValue.BooleanValue(client, "client.disable_stars", 
				false, 
				"Removes Stars");
		day_stars = new StellarViewConfigValue.BooleanValue(client, "client.day_stars", 
				false, 
				"Stars will be visible during the day");
		bright_stars = new StellarViewConfigValue.BooleanValue(client, "client.bright_stars", 
				true, 
				"Makes Stars brighter");

		rotation_multiplier = new StellarViewConfigValue.IntValue(client, "client.rotation_multiplier", 
				300, 1, 300, 
				"Controls how much the sky rotates when moving");
		
		OverworldConfig.init(client);
	}
}
