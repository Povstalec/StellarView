package net.povstalec.stellarview.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class OverworldConfig
{
	public static final String PREFIX = "client.overworld.";
	
	public static StellarViewConfigValue.BooleanValue replace_vanilla;
	public static StellarViewConfigValue.BooleanValue config_priority;
	
	public static StellarViewConfigValue.BooleanValue stars_always_visible;
	
	public static StellarViewConfigValue.IntValue meteor_shower_chance;
	public static StellarViewConfigValue.IntValue shooting_star_chance;
	
	public static StellarViewConfigValue.BooleanValue vanilla_moon;
	
	public static StellarViewConfigValue.IntValue sol_x_offset;
	public static StellarViewConfigValue.IntValue sol_y_offset;
	public static StellarViewConfigValue.IntValue sol_z_offset;
	
	public static StellarViewConfigValue.IntValue overworld_z_rotation_multiplier;
	
	public static StellarViewConfigValue.IntValue sol_x_rotation;
	public static StellarViewConfigValue.IntValue sol_y_rotation;
	public static StellarViewConfigValue.IntValue sol_z_rotation;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		replace_vanilla = new StellarViewConfigValue.BooleanValue(client, PREFIX + "replace_vanilla", 
				true, 
				"Replaces the Vanilla Overworld sky with Stellar View sky");

		config_priority = new StellarViewConfigValue.BooleanValue(client, PREFIX + "config_priority", 
				false, 
				"Prioritizes config over information from resourcepacks");
		
		
		
		stars_always_visible = new StellarViewConfigValue.BooleanValue(client, PREFIX + "stars_always_visible", 
				false, 
				"Stars will always be visible, even during daytime");
		
		
		
		meteor_shower_chance = new StellarViewConfigValue.IntValue(client, PREFIX + "meteor_shower_chance", 
				10, 0, 100, 
				"Chance of a meteor shower happening each day");
		shooting_star_chance = new StellarViewConfigValue.IntValue(client, PREFIX + "shooting_star_chance", 
				10, 0, 100, 
				"Chance of a shooting star appearing each 1000 ticks");
		
		
		
		overworld_z_rotation_multiplier = new StellarViewConfigValue.IntValue(client, "client.overworld_z_rotation_multiplier", 
				3000, 0, 3000, 
				"Controls how much the Overworld sky rotates when moving along the Z-axis");
		
		
		
		vanilla_moon = new StellarViewConfigValue.BooleanValue(client, PREFIX + "vanilla_moon",
				false,
				"Uses the Vanilla Moon texture for rendering");
		
		
		
		sol_x_offset = new StellarViewConfigValue.IntValue(client, PREFIX + "sol_x_offset", 
				0, -120, 120, 
				"Specifies Sol X offset");
		sol_y_offset = new StellarViewConfigValue.IntValue(client, PREFIX + "sol_y_offset", 
				0, -120, 120, 
				"Specifies Sol Y offset");
		sol_z_offset = new StellarViewConfigValue.IntValue(client, PREFIX + "sol_z_offset", 
				0, -120, 120, 
				"Specifies Sol Z offset");

		sol_x_rotation = new StellarViewConfigValue.IntValue(client, PREFIX + "sol_x_rotation", 
				0, 0, 360, 
				"Specifies Sol X-axis rotation");
		sol_y_rotation = new StellarViewConfigValue.IntValue(client, PREFIX + "sol_y_rotation", 
				0, 0, 360, 
				"Specifies Sol Y-axis rotation");
		sol_z_rotation = new StellarViewConfigValue.IntValue(client, PREFIX + "sol_z_rotation", 
				0, 0, 360, 
				"Specifies Sol Z-axis rotation");
	}
}
