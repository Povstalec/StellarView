package net.povstalec.stellarview.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AetherConfig
{
	public static final String PREFIX = "client.aether.";
	
	public static StellarViewConfigValue.BooleanValue replace_default;
	public static StellarViewConfigValue.BooleanValue config_priority;
	
	public static StellarViewConfigValue.IntValue meteor_shower_chance;
	public static StellarViewConfigValue.IntValue shooting_star_chance;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		replace_default = new StellarViewConfigValue.BooleanValue(client, PREFIX + "replace_default",
				true, 
				"Replaces the default Aether sky with Stellar View sky");

		config_priority = new StellarViewConfigValue.BooleanValue(client, PREFIX + "config_priority", 
				false, 
				"Prioritizes config over information from resourcepacks");
		
		
		
		meteor_shower_chance = new StellarViewConfigValue.IntValue(client, PREFIX + "meteor_shower_chance", 
				10, 0, 100, 
				"Chance of a meteor shower happening each day");
		shooting_star_chance = new StellarViewConfigValue.IntValue(client, PREFIX + "shooting_star_chance", 
				10, 0, 100, 
				"Chance of a shooting star appearing each 1000 ticks");
	}
}
