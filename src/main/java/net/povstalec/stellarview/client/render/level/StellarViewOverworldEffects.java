package net.povstalec.stellarview.client.render.level;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.StellarViewSpecialEffects;
import net.povstalec.stellarview.api.celestials.CelestialObject;
import net.povstalec.stellarview.api.celestials.Galaxy;
import net.povstalec.stellarview.api.celestials.Galaxy.SpiralGalaxy;
import net.povstalec.stellarview.api.celestials.Supernova;
import net.povstalec.stellarview.api.celestials.orbiting.Barycenter;
import net.povstalec.stellarview.api.celestials.orbiting.Moon;
import net.povstalec.stellarview.api.celestials.orbiting.Planet;
import net.povstalec.stellarview.api.celestials.orbiting.Sun;
import net.povstalec.stellarview.api.sky_effects.MeteorShower;
import net.povstalec.stellarview.api.sky_effects.ShootingStar;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;

public class StellarViewOverworldEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
	
	public static final ResourceLocation OVERWORLD_SKYBOX = new ResourceLocation(StellarView.MODID, "textures/environment/overworld_skybox/overworld");
	
	public static final int EARTH_YEAR_DAYS = 96;
	
	// Moons
	private static final Moon LUNA = new Moon.DefaultMoon()
		{
			@Override
			protected float getAngularVelocity(ClientLevel level, float partialTicks)
			{
				return 360F / Moon.LUNA_ORBIT_DAYS + 360F / OverworldConfig.overworld_year_length.get();
			}
			
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
			
			@Override
			protected float getSize(ClientLevel level, float partialTicks)
			{
				if(StellarView.isEnhancedCelestialsLoaded())
					return EnhancedCelestialsCompatibility.getMoonSize(partialTicks);
				
				return super.getSize(level, partialTicks);
			}
			
			@Override
			protected float[] getColor(ClientLevel level, float partialTicks)
			{
				if(!StellarView.isEnhancedCelestialsLoaded())
					return super.getColor(level, partialTicks);
				
				return EnhancedCelestialsCompatibility.getMoonColor(level, partialTicks);
			}
		};

		private static final Moon IO = (Moon) new Moon(Moon.IO_TEXTURE, 0.15F)
				.halo(Moon.IO_HALO_TEXTURE, 0.15F);
		private static final Moon EUROPA = (Moon) new Moon(Moon.EUROPA_TEXTURE, 0.125F)
				.halo(Moon.EUROPA_HALO_TEXTURE, 0.125F);
		private static final Moon GANYMEDE = (Moon) new Moon(Moon.GANYMEDE_TEXTURE, 0.25F)
				.halo(Moon.GANYMEDE_HALO_TEXTURE, 0.25F);
		private static final Moon CALLISTO = (Moon) new Moon(Moon.CALLISTO_TEXTURE, 0.2F)
				.halo(Moon.CALLISTO_HALO_TEXTURE, 0.2F);

		private static final Moon ENCELADUS = (Moon) new Moon(Moon.ENCELADUS_TEXTURE, 0.1F)
				.halo(Moon.ENCELADUS_HALO_TEXTURE, 0.1F);
		private static final Moon TETHYS = (Moon) new Moon(Moon.TETHYS_TEXTURE, 0.15F)
				.halo(Moon.TETHYS_HALO_TEXTURE, 0.15F);
		private static final Moon DIONE = (Moon) new Moon(Moon.DIONE_TEXTURE, 0.15F)
				.halo(Moon.DIONE_HALO_TEXTURE, 0.15F);
		private static final Moon RHEA = (Moon) new Moon(Moon.RHEA_TEXTURE, 0.15F)
				.halo(Moon.RHEA_HALO_TEXTURE, 0.15F);
		private static final Moon TITAN = (Moon) new Moon(Moon.TITAN_TEXTURE, 0.25F)
				.halo(Moon.TITAN_HALO_TEXTURE, 0.25F);
		private static final Moon IAPETUS = (Moon) new Moon(Moon.IAPETUS_TEXTURE, 0.15F)
				.halo(Moon.IAPETUS_HALO_TEXTURE, 0.15F);

		private static final Moon ARIEL = (Moon) new Moon(Moon.ARIEL_TEXTURE, 0.075F)
				.halo(Moon.ARIEL_HALO_TEXTURE, 0.075F);
		private static final Moon UMBRIEL = (Moon) new Moon(Moon.UMBRIEL_TEXTURE, 0.075F)
				.halo(Moon.UMBRIEL_HALO_TEXTURE, 0.075F);
		private static final Moon TITANIA = (Moon) new Moon(Moon.TITANIA_TEXTURE, 0.075F)
				.halo(Moon.TITANIA_HALO_TEXTURE, 0.075F);
		private static final Moon OBERON = (Moon) new Moon(Moon.OBERON_TEXTURE, 0.075F)
				.halo(Moon.OBERON_HALO_TEXTURE, 0.075F);

		private static final Moon TRITON = (Moon) new Moon(Moon.TRITON_TEXTURE, 0.08F)
				.halo(Moon.TRITON_HALO_TEXTURE, 0.08F);

		private static final Moon PLUTO = (Moon) new Moon(Moon.PLUTO_TEXTURE, 0.15F)
				.halo(Moon.PLUTO_HALO_TEXTURE, 0.15F);
		private static final Moon CHARON = (Moon) new Moon(Moon.CHARON_TEXTURE, 0.1F)
				.halo(Moon.CHARON_HALO_TEXTURE, 0.1F);
		
		
		// Planets
		private static final Planet MERCURY = (Planet) new Planet(Planet.MERCURY_TEXTURE, 0.7F)
				.halo(Planet.MERCURY_HALO_TEXTURE, 0.7F);
		
		private static final Planet VENUS = (Planet) new Planet(Planet.VENUS_TEXTURE, 0.9F)
				.halo(Planet.VENUS_HALO_TEXTURE, 0.9F);
		
		private static final Planet EARTH = (Planet) new Planet(Planet.EARTH_TEXTURE, 30, Planet.EARTH_DAY_LENGTH)
				{
					@Override
					protected float getAngularVelocity(ClientLevel level, float partialTicks)
					{
						return 360F / OverworldConfig.overworld_year_length.get();
					}
				}
				.addAtmosphere(new Planet.Atmosphere(
								(ShootingStar) new ShootingStar().setRarityValue(OverworldConfig.shooting_star_chance),
								(MeteorShower) new MeteorShower().setRarityValue(OverworldConfig.meteor_shower_chance)))
				.addOrbitingObject(LUNA, 384400F, 360F / 8, (float) Math.toRadians(45), (float) Math.toRadians(-5.145), 0);

		private static final Planet MARS = (Planet) new Planet(Planet.MARS_TEXTURE, 0.8F)
				.halo(Planet.MARS_HALO_TEXTURE, 0.8F);

		private static final Planet VESTA = (Planet) new Planet(Planet.VESTA_TEXTURE, 0.35F)
				.halo(Planet.VESTA_HALO_TEXTURE, 0.35F);

		private static final Planet CERES = (Planet) new Planet(Planet.CERES_TEXTURE, 0.4F)
				.halo(Planet.CERES_HALO_TEXTURE, 0.4F);
		
		private static final Planet JUPITER = (Planet) new Planet(Planet.JUPITER_TEXTURE, 1.5F)
				.addOrbitingObject(IO, 10 * 421700, 360F / 2, 1, (float) Math.toRadians(0.05), (float) Math.toRadians(47))
				.addOrbitingObject(EUROPA, 10 * 671034, 360F / 4, 22, (float) Math.toRadians(0.47), (float) Math.toRadians(180))
				.addOrbitingObject(GANYMEDE, 10 * 1070412, 360F / 7, 44, (float) Math.toRadians(0.2), (float) Math.toRadians(13))
				.addOrbitingObject(CALLISTO, 10 * 1882709, 360F / 16, 88, (float) Math.toRadians(0.192), (float) Math.toRadians(213))
				.halo(Planet.JUPITER_HALO_TEXTURE, 1.5F);

		private static final Planet SATURN = (Planet) new Planet(Planet.SATURN_TEXTURE, 1F)
				.addOrbitingObject(ENCELADUS, 25 * 238000, 360F / 0.361F, 16, (float) Math.toRadians(0.009), (float) Math.toRadians(176))
				.addOrbitingObject(TETHYS, 25 * 294000, 360F / 0.497F, 32, (float) Math.toRadians(1.12), (float) Math.toRadians(19))
				.addOrbitingObject(DIONE, 25 * 377000, 360F / 0.721F, 64, (float) Math.toRadians(0.02), (float) Math.toRadians(64))
				.addOrbitingObject(RHEA, 25 * 527000, 360F / 1.189F, 128, (float) Math.toRadians(0.35), (float) Math.toRadians(110))
				.addOrbitingObject(TITAN, 25 * 1200000, 360F / 4, 256, (float) Math.toRadians(0.348), (float) Math.toRadians(99))
				.addOrbitingObject(IAPETUS, 25 * 3560820, 360F / 21, 152, (float) Math.toRadians(15.47), (float) Math.toRadians(144))
				.halo(Planet.SATURN_HALO_TEXTURE, 1F);
		
		private static final Planet URANUS = (Planet) new Planet(Planet.URANUS_TEXTURE, 0.5F)
				.addOrbitingObject(ARIEL, 100 * 191020, 360F / 0.663F, 7, (float) Math.toRadians(98.041), (float) Math.toRadians(8))
				.addOrbitingObject(UMBRIEL, 100 * 266300, 360F / 1.091F, 77, (float) Math.toRadians(98.128), (float) Math.toRadians(16))
				.addOrbitingObject(TITANIA, 100 * 436300, 360F / 2.292F, 177, (float) Math.toRadians(98.079), (float) Math.toRadians(32))
				.addOrbitingObject(OBERON, 100 * 583500, 360F / 3.542F, 277, (float) Math.toRadians(157.34), (float) Math.toRadians(64))
				.halo(Planet.URANUS_HALO_TEXTURE, 0.5F);
		
		private static final Planet NEPTUNE = (Planet) new Planet(Planet.NEPTUNE_TEXTURE, 0.3F)
				.addOrbitingObject(TRITON, 100 * 354800, 360F / 1.547F, 277, (float) Math.toRadians(98.058), (float) Math.toRadians(64))
				.halo(Planet.NEPTUNE_HALO_TEXTURE, 0.3F);

		// The barycenter is quite literally nothing, and exists just to give Pluto and Charon something to orbit around.
		// I added this as a treat. Really hard to find, both due to size and ridiculous inclination putting it far from the ecliptic,
		// and perhaps amongst the stars of the galactic disc.
		// Those who find this will surely be very happy, if they recognize it for what it is.
		private static final Barycenter PLUTO_CHARON_BARYCENTER = (Barycenter) new Barycenter()
				.addOrbitingObject(PLUTO, 2500 * 1168, 360F / 1.681F, 0, (float) Math.toRadians(0.001), (float) Math.toRadians(69))
				.addOrbitingObject(CHARON, 2500 * 19640, 360F / 1.681F, 180, (float) Math.toRadians(0.001), (float) Math.toRadians(96));
		
		
		
		// Stars
		private static final Sun SOL = (Sun) new Sun.VanillaSun()
				{
					protected boolean shouldRender(ClientLevel level, Camera camera)
					{
						return !OverworldConfig.disable_sun.get();
					}
					
					@Override
					public Vector3f getAxisRotation()
					{
						return new Vector3f((float) OverworldConfig.milky_way_x_axis_rotation.get(), (float) OverworldConfig.milky_way_y_axis_rotation.get(), (float) OverworldConfig.milky_way_z_axis_rotation.get());
					}
				}
				.addOrbitingObject(MERCURY, 54207000F, 360F / 23, (float) Math.toRadians(52), (float) Math.toRadians(7), (float) Math.toRadians(113))
				.addOrbitingObject(VENUS, 107540000F, 360F / 59, (float) Math.toRadians(241), (float) Math.toRadians(1.85), (float) Math.toRadians(123))
				.addOrbitingObject(MARS, 226380000F, 360F / 180, (float) Math.toRadians(139), (float) Math.toRadians(3.39), (float) Math.toRadians(79))
				.addOrbitingObject(VESTA, 356000000F, 360F / 349, (float) Math.toRadians(174), (float) Math.toRadians(7.14), (float) Math.toRadians(104))
				.addOrbitingObject(CERES, 413000000F, 360F / 442, (float) Math.toRadians(81), (float) Math.toRadians(10.59), (float) Math.toRadians(80))
				.addOrbitingObject(JUPITER, 745010000F, 360F / 1152, (float) Math.toRadians(71), (float) Math.toRadians(1.31), (float) Math.toRadians(62))
				.addOrbitingObject(SATURN, 1455200000F, 360F / 2822, (float) Math.toRadians(190), (float) Math.toRadians(2.48), (float) Math.toRadians(93))
				.addOrbitingObject(URANUS, 2932900000F, 360F / 8064, (float) Math.toRadians(270), (float) Math.toRadians(1), (float) Math.toRadians(36))
				.addOrbitingObject(NEPTUNE, 4472500000F, 360F / 15840, (float) Math.toRadians(311), (float) Math.toRadians(1.77), (float) Math.toRadians(1))
				.addOrbitingObject(PLUTO_CHARON_BARYCENTER, 5900000000F, 360F / 23871, (float) Math.toRadians(80), (float) Math.toRadians(17.14), (float) Math.toRadians(0))
				.addOrbitingObject(EARTH, 147280000, 360F / 96, 0, 0, 0);
		//Earth added last because planets kept rendering over the Moon
		//TODO Add a built-in way of ordering the planets by distance
		
		// Clusters
		//public static final StarField VANILLA = new StarField.VanillaStarField(10, 10842L, (short) 1500);
		
		// Galaxies
		public static final SpiralGalaxy MILKY_WAY = (SpiralGalaxy) new Galaxy.SpiralGalaxy(Galaxy.SPIRAL_GALAXY_TEXTURE, 100, 10842L, (byte) 4, (short) 1500)
				.addGalacticObject(new Supernova(10.0F, 15 * CelestialObject.TICKS_PER_DAY + 18000, 5 * CelestialObject.TICKS_PER_DAY), 10, -3, 2)
				.addGalacticObject(SOL, 0, 0, 16, 18, 0, 90);
	
	public StellarViewOverworldEffects()
	{
		super(new StellarViewSky(EARTH)
				.skybox(OVERWORLD_SKYBOX),
				192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
	}
	
	public void setupGalaxy()
	{
		MILKY_WAY.setStarBuffer(OverworldConfig.milky_way_x.get(), OverworldConfig.milky_way_y.get(), OverworldConfig.milky_way_z.get(), 0, 0, 0);
	}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(StellarViewConfig.replace_overworld.get())
			super.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return StellarViewConfig.replace_overworld.get();
    }
	
	@Override
	public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
    {
		if(StellarViewConfig.replace_overworld.get())
		{
			super.adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
			
			if(StellarView.isEnhancedCelestialsLoaded())
				EnhancedCelestialsCompatibility.adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
		}
	}
	
	//TODO Use this again
	/*public double starWidthFunction(double aLocation, double bLocation, double sinRandom, double cosRandom, double sinTheta, double cosTheta, double sinPhi, double cosPhi)
	{
		if(StellarViewConfig.enable_black_hole.get())
			return cosPhi  > 0.0 ? cosPhi * 8 *(bLocation * cosRandom + aLocation * sinRandom) : bLocation * cosRandom + aLocation * sinRandom;
		
		return bLocation * cosRandom + aLocation * sinRandom;
	}*/
}
