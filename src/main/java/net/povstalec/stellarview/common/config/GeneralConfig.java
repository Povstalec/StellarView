package net.povstalec.stellarview.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig
{
	public static final String PREFIX = "client.general";

	public static StellarViewConfigValue.BooleanValue disable_view_center_rotation;
	
	public static StellarViewConfigValue.BooleanValue disable_stars;
	public static StellarViewConfigValue.BooleanValue bright_stars;
	public static StellarViewConfigValue.BooleanValue textured_stars;
	public static StellarViewConfigValue.BooleanValue dust_clouds;
	
	public static void init(ForgeConfigSpec.Builder client)
	{

		disable_view_center_rotation = new StellarViewConfigValue.BooleanValue(client, "client.disable_view_center_rotation", 
				false, 
				"Gets rid of any rotation caused by the view center, making the space XYZ coordinates align with the XYZ directions in Minecraft");
		
		
		
		disable_stars = new StellarViewConfigValue.BooleanValue(client, "client.disable_stars", 
				false, 
				"Removes Stars");
		
		bright_stars = new StellarViewConfigValue.BooleanValue(client, "client.bright_stars", 
				true, 
				"Makes Stars brighter");
		
		textured_stars = new StellarViewConfigValue.BooleanValue(client, "client.textured_stars",
				false,
				"Enables the use of textures for stars");
		
		dust_clouds = new StellarViewConfigValue.BooleanValue(client, "client.dust_clouds",
				true,
				"Enables dust cloud rendering");
	}
}
