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
	
	public static StellarViewConfigValue.BooleanValue replace_vanilla;

	public static StellarViewConfigValue.BooleanValue disable_sun;

	public static StellarViewConfigValue.BooleanValue disable_moon;
	public static StellarViewConfigValue.BooleanValue disable_moon_phases;
	
	public static StellarViewConfigValue.BooleanValue disable_stars;
	public static StellarViewConfigValue.BooleanValue day_stars;
	public static StellarViewConfigValue.BooleanValue bright_stars;
	
	public static StellarViewConfigValue.IntValue milky_way_x;
	public static StellarViewConfigValue.IntValue milky_way_y;
	public static StellarViewConfigValue.IntValue milky_way_z;
	
	public static StellarViewConfigValue.IntValue milky_way_alpha;
	public static StellarViewConfigValue.IntValue milky_way_beta;
	public static StellarViewConfigValue.IntValue milky_way_gamma;
	
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
		replace_vanilla = new StellarViewConfigValue.BooleanValue(client, "client.replace_vanilla", 
				true, 
				"Replaces the Vanilla Overworld sky with Stellar View sky");
		
		disable_sun = new StellarViewConfigValue.BooleanValue(client, "client.disable_sun", 
				false, 
				"Disables the Sun");
		
		disable_moon = new StellarViewConfigValue.BooleanValue(client, "client.disable_moon", 
				false, 
				"Disables the Moon");
		disable_moon_phases = new StellarViewConfigValue.BooleanValue(client, "client.disable_moon_phases", 
				false, 
				"Disables Moon phases");
		
		disable_stars = new StellarViewConfigValue.BooleanValue(client, "client.disable_stars", 
				false, 
				"Removes Stars");
		day_stars = new StellarViewConfigValue.BooleanValue(client, "client.day_stars", 
				false, 
				"Stars will be visible during the day");
		bright_stars = new StellarViewConfigValue.BooleanValue(client, "client.bright_stars", 
				true, 
				"Makes Stars brighter");

		milky_way_x = new StellarViewConfigValue.IntValue(client, "client.milky_way_x", 
				0, -20, 20, 
				"Specifies Milky Way X position");

		milky_way_y = new StellarViewConfigValue.IntValue(client, "client.milky_way_y", 
				0, -20, 20, 
				"Specifies Milky Way Y position");

		milky_way_z = new StellarViewConfigValue.IntValue(client, "client.milky_way_z", 
				16, -20, 20, 
				"Specifies Milky Way Z position");

		milky_way_alpha = new StellarViewConfigValue.IntValue(client, "client.milky_way_alpha", 
				90, 0, 360, 
				"Specifies Milky Way Alpha rotation");

		milky_way_beta = new StellarViewConfigValue.IntValue(client, "client.milky_way_beta", 
				18, 0, 360, 
				"Specifies Milky Way Beta rotation");

		milky_way_gamma = new StellarViewConfigValue.IntValue(client, "client.milky_way_gamma", 
				0, 0, 360, 
				"Specifies Milky Way Gamma rotation");
	}
}
