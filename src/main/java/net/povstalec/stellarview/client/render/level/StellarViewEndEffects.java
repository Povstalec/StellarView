package net.povstalec.stellarview.client.render.level;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.StellarViewSpecialEffects;
import net.povstalec.stellarview.api.celestials.CelestialObject;
import net.povstalec.stellarview.api.celestials.Galaxy;
import net.povstalec.stellarview.api.celestials.Galaxy.SpiralGalaxy;
import net.povstalec.stellarview.api.celestials.Supernova;
import net.povstalec.stellarview.api.celestials.orbiting.Planet;
import net.povstalec.stellarview.api.celestials.orbiting.Sun;
import net.povstalec.stellarview.api.sky_effects.MeteorShower;
import net.povstalec.stellarview.api.sky_effects.ShootingStar;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class StellarViewEndEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
	
	public static final ResourceLocation END_SKYBOX = new ResourceLocation(StellarView.MODID, "textures/environment/overworld_skybox/end");
	

	
	private static final Planet END = (Planet) new Planet(Planet.EARTH_TEXTURE, 30, Planet.EARTH_DAY_LENGTH)
			.addAtmosphere(new Planet.Atmosphere(
							(ShootingStar) new ShootingStar().setRarityValue(OverworldConfig.shooting_star_chance),
							(MeteorShower) new MeteorShower().setRarityValue(OverworldConfig.meteor_shower_chance)));
	
	// Stars
	private static final Sun END_SUN = (Sun) new Sun.VanillaSun()
			{
				protected boolean shouldRender(ClientLevel level, Camera camera)
				{
					return !OverworldConfig.disable_sun.get();
				}
						
				@Override
				public Vector3f getAxisRotation()
				{
					return new Vector3f(18, 30, 15);
				}
			}
			.addOrbitingObject(END, 147280000, 360F / 96, 0, 0, 0);
	
	// Galaxies
	public static final SpiralGalaxy MILKY_WAY = (SpiralGalaxy) new Galaxy.SpiralGalaxy(100, 10842L, (byte) 4, (short) 1500)
			.addGalacticObject(new Supernova(10.0F, 15 * CelestialObject.TICKS_PER_DAY + 18000, 5 * CelestialObject.TICKS_PER_DAY), 10, -3, 2)
			.addGalacticObject(END_SUN, 36, 8, 16, 18, 30, 15);
	
	public StellarViewEndEffects()
	{
		super(new StellarViewSky(END)
				.skybox(END_SKYBOX),
				Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
	}

    public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight)
    {
       return biomeFogColor;
    }

    public boolean isFoggyAt(int x, int y)
    {
       return true;
    }
	
	public void setupGalaxy()
	{
		MILKY_WAY.setStarBuffer(36, 8, 16, 18, 30, 15);
	}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(StellarViewConfig.replace_end.get())
			super.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return StellarViewConfig.replace_end.get();
    }
}
