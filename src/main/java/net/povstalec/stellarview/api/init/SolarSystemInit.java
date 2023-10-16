package net.povstalec.stellarview.api.init;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.celestials.SolarSystem;
import net.povstalec.stellarview.api.celestials.orbiting.Moon;
import net.povstalec.stellarview.api.celestials.orbiting.Sun;
import net.povstalec.stellarview.common.config.OverworldConfig;

public class SolarSystemInit
{
	public static final SolarSystem SOL_SYSTEM = new SolarSystem(new Sun.DefaultSun(), 0, 0, 16, (float) Math.toRadians(90), (float) Math.toRadians(18), 0)
			.addCelestialObject(new Sun.VanillaSun()
			{
				@Override
				protected boolean shouldRender(ClientLevel level, Camera camera)
				{
					return !OverworldConfig.disable_sun.get();
				}
			})
			.addCelestialObject(new Moon.DefaultMoon()
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
			});
}
