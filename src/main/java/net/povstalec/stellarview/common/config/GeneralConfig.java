package net.povstalec.stellarview.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneralConfig
{
	public static final String PREFIX = "client.general";
	
	public static StellarViewConfigValue.BooleanValue static_sky;
	
	public static StellarViewConfigValue.BooleanValue use_game_ticks;
	public static StellarViewConfigValue.IntValue tick_multiplier;
	
	public static StellarViewConfigValue.BooleanValue disable_view_center_rotation;
	
	public static StellarViewConfigValue.IntValue space_region_render_distance;
	
	public static StellarViewConfigValue.BooleanValue gravitational_lensing;
	public static StellarViewConfigValue.BooleanValue dust_clouds;
	
	public static StellarViewConfigValue.BooleanValue disable_stars;
	public static StellarViewConfigValue.BooleanValue textured_stars;
	public static StellarViewConfigValue.BooleanValue light_pollution;
	public static StellarViewConfigValue.IntValue star_brightness;
	public static StellarViewConfigValue.IntValue dust_cloud_brightness;
	
	public static void init(ModConfigSpec.Builder client)
	{
		static_sky = new StellarViewConfigValue.BooleanValue(client, "client.static_sky",
				false,
				"Makes the sky static (compatible with shaders)");
		
		
		
		use_game_ticks = new StellarViewConfigValue.BooleanValue(client, "client.use_game_ticks",
				false,
				"False - Positions along orbits, supernovae and meteor effects are affected when /time set command is used | True - Only planetary rotation is affected, everything else is unaffected (good choice if you want to use /time set 0 command without the sky reverting back to how it looked at tick 0)");
		
		tick_multiplier = new StellarViewConfigValue.IntValue(client, "client.tick_multiplier",
				1, 1, 1000,
				"Specifies the max distance at which a Space Region can render");

		
		
		disable_view_center_rotation = new StellarViewConfigValue.BooleanValue(client, "client.disable_view_center_rotation", 
				false, 
				"Gets rid of any rotation caused by the view center, making the space XYZ coordinates align with the XYZ directions in Minecraft");
		
		
		
		space_region_render_distance = new StellarViewConfigValue.IntValue(client, "client.space_region_render_distance",
				8, 1, 12,
				"Specifies the max distance at which a Space Region can render");
		
		
		
		gravitational_lensing = new StellarViewConfigValue.BooleanValue(client, "client.gravitational_lensing",
				true,
				"Enables gravitational lensing effects");
		
		dust_clouds = new StellarViewConfigValue.BooleanValue(client, "client.dust_clouds",
				true,
				"Enables dust cloud rendering");
		
		
		
		disable_stars = new StellarViewConfigValue.BooleanValue(client, "client.disable_stars", 
				false, 
				"Removes Stars");
		
		textured_stars = new StellarViewConfigValue.BooleanValue(client, "client.textured_stars",
				true,
				"Enables the use of textures for stars");
		
		light_pollution = new StellarViewConfigValue.BooleanValue(client, "client.light_pollution",
				true,
				"Makes sky objects dimmer when the player is near a light source");
		
		star_brightness = new StellarViewConfigValue.IntValue(client, "client.star_brightness",
				100, 0, 100,
				"Specifies the base brightness of stars and planets");
		
		dust_cloud_brightness = new StellarViewConfigValue.IntValue(client, "client.dust_cloud_brightness",
				100, 0, 100,
				"Specifies the base brightness of dust clouds and nebulae");
	}
}
