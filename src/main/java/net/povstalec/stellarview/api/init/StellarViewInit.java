package net.povstalec.stellarview.api.init;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestials.orbiting.Moon;
import net.povstalec.stellarview.api.celestials.orbiting.Planet;
import net.povstalec.stellarview.api.celestials.orbiting.Sun;
import net.povstalec.stellarview.common.config.OverworldConfig;

public class StellarViewInit
{
	public static final ResourceLocation EARTH_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/venus.png");//TODO Change
	
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
	
	
	
	// Planets
	public static final Planet EARTH = (Planet) new Planet(EARTH_TEXTURE, 30)
			.addOrbitingObject(LUNA, 20, 360F / 8);
	
	
	
	// Stars
	public static final Sun SOL = (Sun) new Sun.VanillaSun()
			.addOrbitingObject(EARTH, 20, 360F / 96);
}
