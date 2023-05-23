package net.povstalec.stellarview.client.render.level;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestial_objects.Moon;
import net.povstalec.stellarview.api.celestial_objects.Sun;
import net.povstalec.stellarview.client.render.level.misc.StellarViewGalaxy;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkybox;

public class StellarViewSky extends AbstractStellarViewSky
{
	public StellarViewSky(){}
	
	public final StellarViewSky vanilla()
	{
		this.starBuffer = StellarViewGalaxy.createStars(
				StellarViewGalaxy.Type.VANILLA, 0, 0,
				0, 0, 0, 0, 0, 0);
		return this;
	}
	
	public final StellarViewSky spiralGalaxy4Arms(long seed, int numberOfStars,
			double xOffset, double yOffset, double zOffset, double alpha, double beta, double gamma)
	{
		this.starBuffer = StellarViewGalaxy.createStars(
				StellarViewGalaxy.Type.SPIRAL_GALAXY_4_ARMS, seed, numberOfStars,
				xOffset, yOffset, zOffset, alpha, beta, gamma);
		return this;
	}
	
	public final StellarViewSky spiralGalaxy2Arms(long seed, int numberOfStars,
			double xOffset, double yOffset, double zOffset, double alpha, double beta, double gamma)
	{
		this.starBuffer = StellarViewGalaxy.createStars(
				StellarViewGalaxy.Type.SPIRAL_GALAXY_2_ARMS, seed, numberOfStars,
				xOffset, yOffset, zOffset, alpha, beta, gamma);
		return this;
	}
	
	public final StellarViewSky vanillaSun()
	{
		this.celestialObjects.add(new Sun.VanillaSun());
		return this;
	}
	
	public final StellarViewSky sun(Sun sun)
	{
		this.celestialObjects.add(sun);
		return this;
	}
	
	public final StellarViewSky vanillaMoon()
	{
		this.celestialObjects.add(new Moon.VanillaMoon());
		return this;
	}
	
	public final StellarViewSky moon(Moon moon)
	{
		this.celestialObjects.add(moon);
		return this;
	}
	
	public final StellarViewSky skybox(ResourceLocation texture)
	{
		this.skybox = new StellarViewSkybox(texture);
		return this;
	}
}
