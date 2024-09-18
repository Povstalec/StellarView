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
	public static StellarViewConfigValue.BooleanValue replace_nether;
	public static StellarViewConfigValue.BooleanValue replace_end;
	
	public static StellarViewConfigValue.BooleanValue disable_stars;
	public static StellarViewConfigValue.BooleanValue day_stars;
	public static StellarViewConfigValue.BooleanValue bright_stars;

	public static StellarViewConfigValue.BooleanValue distance_star_size;
	public static StellarViewConfigValue.BooleanValue distance_star_brightness;
	
	public static StellarViewConfigValue.BooleanValue uniform_star_brightness;
	public static StellarViewConfigValue.BooleanValue equal_spectral_types;
	public static StellarViewConfigValue.BooleanValue uniform_star_color;
	
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
		
		replace_nether = new StellarViewConfigValue.BooleanValue(client, "client.replace_nether", 
				false, 
				"Replaces the Vanilla Nether sky with Stellar View sky");
		
		replace_end = new StellarViewConfigValue.BooleanValue(client, "client.replace_end", 
				true, 
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
		
		distance_star_size = new StellarViewConfigValue.BooleanValue(client, "client.distance_star_size", 
				true, 
				"Stars will become smaller the further away they are");
		distance_star_brightness = new StellarViewConfigValue.BooleanValue(client, "client.distance_star_brightness", 
				true, 
				"Stars will become less bright the further away they are");
		
		uniform_star_brightness = new StellarViewConfigValue.BooleanValue(client, "client.uniform_star_brightness", 
				false, 
				"All stars will have the same brightness");
		equal_spectral_types = new StellarViewConfigValue.BooleanValue(client, "client.equal_spectral_types", 
				false, 
				"All spectral types will be distributed equally, as opposed to a more realistic distribution (About 75% class M stars)");
		uniform_star_color = new StellarViewConfigValue.BooleanValue(client, "client.uniform_star_color", 
				false, 
				"All stars will have the same color (White)");
		
		OverworldConfig.init(client);
	}
}
