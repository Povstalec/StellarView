package net.povstalec.stellarview.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BetaConfig
{
	public static final String PREFIX = "client.beta.";
	
	public static StellarViewConfigValue.BooleanValue disable_sun;

	public static StellarViewConfigValue.BooleanValue disable_moon;
	public static StellarViewConfigValue.BooleanValue disable_moon_phases;

	public static StellarViewConfigValue.IntValue meteor_shower_chance;
	public static StellarViewConfigValue.IntValue shooting_star_chance;
	
	public static StellarViewConfigValue.IntValue milky_way_x;
	public static StellarViewConfigValue.IntValue milky_way_y;
	public static StellarViewConfigValue.IntValue milky_way_z;
	
	public static StellarViewConfigValue.IntValue milky_way_alpha;
	public static StellarViewConfigValue.IntValue milky_way_beta;
	public static StellarViewConfigValue.IntValue milky_way_gamma;
	
	public static void init(ForgeConfigSpec.Builder client)
	{
		disable_sun = new StellarViewConfigValue.BooleanValue(client, PREFIX + "disable_sun", 
				false, 
				"Disables the Sun");
		
		disable_moon = new StellarViewConfigValue.BooleanValue(client, PREFIX + "disable_moon", 
				false, 
				"Disables the Moon");
		disable_moon_phases = new StellarViewConfigValue.BooleanValue(client, PREFIX + "disable_moon_phases", 
				false, 
				"Disables Moon phases");

		meteor_shower_chance = new StellarViewConfigValue.IntValue(client, PREFIX + "meteor_shower_chance", 
				10, 0, 100, 
				"Chance of a meteor shower happening each day");
		shooting_star_chance = new StellarViewConfigValue.IntValue(client, PREFIX + "shooting_star_chance", 
				10, 0, 100, 
				"Chance of a shooting star appearing each 1000 ticks");

		milky_way_x = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_x", 
				0, -45, 45, 
				"Specifies Milky Way X position");
		milky_way_y = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_y", 
				0, -45, 45, 
				"Specifies Milky Way Y position");
		milky_way_z = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_z", 
				16, -45, 45, 
				"Specifies Milky Way Z position");

		milky_way_alpha = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_alpha", 
				90, 0, 360, 
				"Specifies Milky Way Alpha rotation");
		milky_way_beta = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_beta", 
				18, 0, 360, 
				"Specifies Milky Way Beta rotation");
		milky_way_gamma = new StellarViewConfigValue.IntValue(client, PREFIX + "milky_way_gamma", 
				0, 0, 360, 
				"Specifies Milky Way Gamma rotation");
	}
}
