package net.povstalec.stellarview.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig
{
	public static final String PREFIX = "client.general";

	public static StellarViewConfigValue.BooleanValue disable_stars;
	public static StellarViewConfigValue.BooleanValue bright_stars;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		disable_stars = new StellarViewConfigValue.BooleanValue(client, "client.disable_stars", 
				false, 
				"Removes Stars");
		
		bright_stars = new StellarViewConfigValue.BooleanValue(client, "client.bright_stars", 
				true, 
				"Makes Stars brighter");
	}
}
