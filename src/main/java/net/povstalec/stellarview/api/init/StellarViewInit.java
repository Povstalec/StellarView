package net.povstalec.stellarview.api.init;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.celestials.CelestialObject;
import net.povstalec.stellarview.api.celestials.Galaxy;
import net.povstalec.stellarview.api.celestials.Galaxy.SpiralGalaxy;
import net.povstalec.stellarview.api.celestials.Supernova;
import net.povstalec.stellarview.api.celestials.orbiting.Moon;
import net.povstalec.stellarview.api.celestials.orbiting.Moon.DefaultMoon;
import net.povstalec.stellarview.api.celestials.orbiting.Planet;
import net.povstalec.stellarview.api.celestials.orbiting.Sun;
import net.povstalec.stellarview.api.sky_effects.MeteorShower;
import net.povstalec.stellarview.api.sky_effects.ShootingStar;
import net.povstalec.stellarview.common.config.OverworldConfig;

public class StellarViewInit
{
	// Moons
	public static final Moon LUNA = new Moon.DefaultMoon()
	{
		@Override
		protected boolean shouldRender(ClientLevel level, Camera camera)
		{
			return !OverworldConfig.disable_moon.get();
		}
		
		@Override
		protected boolean hasPhases()
		{
			return !OverworldConfig.disable_moon_phases.get();
		}
	};

	public static final Moon GANYMEDE = new DefaultMoon(0.25F);
	
	
	
	// Planets
	public static final Planet VENUS = (Planet) new Planet(Planet.VENUS_TEXTURE, 1)
			.halo(Planet.VENUS_HALO_TEXTURE, 1);
	
	public static final Planet EARTH = (Planet) new Planet(Planet.EARTH_TEXTURE, 30, Planet.EARTH_DAY_LENGTH)
			.addAtmosphere(new Planet.Atmosphere(
							(ShootingStar) new ShootingStar().setRarityValue(OverworldConfig.shooting_star_chance),
							(MeteorShower) new MeteorShower().setRarityValue(OverworldConfig.meteor_shower_chance)))
			.addOrbitingObject(LUNA, 384400F, 360F / 8, 0)
			.setGalacticPosition(0, 0, 75);

	public static final Planet MARS = (Planet) new Planet(Planet.MARS_TEXTURE, 1)
			.halo(Planet.MARS_HALO_TEXTURE, 1);
	
	public static final Planet JUPITER = (Planet) new Planet(Planet.JUPITER_TEXTURE, 1)
			.addOrbitingObject(GANYMEDE, 10 * 1070000, 360F / 7, 0)
			.halo(Planet.JUPITER_HALO_TEXTURE, 1);
	
	
	
	// Stars
	public static final Sun SOL = (Sun) new Sun.VanillaSun()
			.addOrbitingObject(VENUS, 107540000, 360F / 59, (float) Math.toRadians(241))
			.addOrbitingObject(MARS, 226380000, 360F / 180, (float) Math.toRadians(139))
			.addOrbitingObject(JUPITER, 745010000, 360F / 1152, (float) Math.toRadians(71))
			.addOrbitingObject(EARTH, 147280000, 360F / 96, 0);
	//Earth added last because planets kept rendering over the Moon
	//TODO Add a built-in way of ordering the planets by distance
	
	// Clusters
	//public static final StarField VANILLA = new StarField.VanillaStarField(10, 10842L, (short) 1500);
	
	// Galaxies
	public static final SpiralGalaxy MILKY_WAY = (SpiralGalaxy) new Galaxy.SpiralGalaxy(100, 10842L, (byte) 4, (short) 1500)
			.addGalacticObject(new Supernova(10.0F, 15 * CelestialObject.TICKS_PER_DAY + 18000, 5 * CelestialObject.TICKS_PER_DAY), 10, -3, 2)
			.addGalacticObject(SOL, 0, 0, 16, 18, 0, 90);
	
	//public static final StarField SPINDLE_GALAXY = new Galaxy.LenticularGalaxy(10842L, (short) 6000);
}
