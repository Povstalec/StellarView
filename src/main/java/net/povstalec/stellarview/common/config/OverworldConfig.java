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
	
	public static StellarViewConfigValue.IntValue milky_way_x;
	public static StellarViewConfigValue.IntValue milky_way_y;
	public static StellarViewConfigValue.IntValue milky_way_z;

	public static StellarViewConfigValue.IntValue overworld_year_length;
	
	public static StellarViewConfigValue.IntValue overworld_z_rotation_multiplier;
	
	public static StellarViewConfigValue.IntValue milky_way_x_axis_rotation;
	public static StellarViewConfigValue.IntValue milky_way_y_axis_rotation;
	public static StellarViewConfigValue.IntValue milky_way_z_axis_rotation;
	
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
		
		
		
		overworld_year_length = new StellarViewConfigValue.IntValue(client, PREFIX + "overworld_year_length", 
				96, 1, 512, 
				"Specifies the number of days it takes for the Earth to complete one orbit around the Sun");

		overworld_z_rotation_multiplier = new StellarViewConfigValue.IntValue(client, "client.overworld_z_rotation_multiplier", 
				3000, 1, 3000, 
				"Controls how much the Overworld sky rotates when moving along the Z-axis");
		
		
		
		milky_way_x = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_x", 
				0, -45, 45, 
				"Specifies Milky Way X position");
		milky_way_y = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_y", 
				0, -45, 45, 
				"Specifies Milky Way Y position");
		milky_way_z = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_z", 
				16, -45, 45, 
				"Specifies Milky Way Z position");

		milky_way_x_axis_rotation = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_x_axis_rotation", 
				18, 0, 360, 
				"Specifies Milky Way Alpha rotation");
		milky_way_y_axis_rotation = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_y_axis_rotation", 
				0, 0, 360, 
				"Specifies Milky Way Beta rotation");
		milky_way_z_axis_rotation = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_z_axis_rotation", 
				90, 0, 360, 
				"Specifies Milky Way Gamma rotation");
	}
}
