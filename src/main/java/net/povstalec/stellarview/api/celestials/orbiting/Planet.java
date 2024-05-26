package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.Optional;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.sky_effects.MeteorShower;
import net.povstalec.stellarview.api.sky_effects.ShootingStar;
import net.povstalec.stellarview.client.render.level.misc.StellarViewFogEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkyEffects;
import net.povstalec.stellarview.common.config.OverworldConfig;

public class Planet extends OrbitingCelestialObject
{
	public static final ResourceLocation MERCURY_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/mercury.png");
	public static final ResourceLocation MERCURY_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/mercury_halo.png");
	
	public static final ResourceLocation VENUS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/venus.png");
	public static final ResourceLocation VENUS_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/venus_halo.png");
	
	public static final ResourceLocation EARTH_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/earth.png");
	public static final ResourceLocation EARTH_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/earth_halo.png");
	
	public static final ResourceLocation MARS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/mars.png");
	public static final ResourceLocation MARS_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/mars_halo.png");

	public static final ResourceLocation VESTA_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/vesta.png");
	public static final ResourceLocation VESTA_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/vesta_halo.png");

	public static final ResourceLocation CERES_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/ceres.png");
	public static final ResourceLocation CERES_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/ceres_halo.png");

	public static final ResourceLocation JUPITER_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/jupiter.png");
	public static final ResourceLocation JUPITER_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/jupiter_halo.png");
	
	public static final ResourceLocation SATURN_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/saturn.png");
	public static final ResourceLocation SATURN_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/saturn_halo.png");
	
	public static final ResourceLocation URANUS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/uranus.png");
	public static final ResourceLocation URANUS_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/uranus_halo.png");
	
	public static final ResourceLocation NEPTUNE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/neptune.png");
	public static final ResourceLocation NEPTUNE_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/neptune_halo.png");
	
	private static final int TICKS_PER_DAY = 24000;
	
	public static final int EARTH_DAY_LENGTH = TICKS_PER_DAY;
	
	private Optional<Integer> rotationPeriod = Optional.empty();
	private Optional<Atmosphere> atmosphere = Optional.empty();
	
	public Planet(ResourceLocation texture, float size)
	{
		this(texture, size, 0);
	}
	
	public Planet(ResourceLocation texture, float size, int rotationPeriod)
	{
		super(texture, size);
		if(rotationPeriod > 0)
			this.rotationPeriod = Optional.of(rotationPeriod);
	}
	
	public Planet addAtmosphere(Atmosphere atmosphere)
	{
		this.atmosphere = Optional.of(atmosphere);
		
		return this;
	}
	
	public Optional<Integer> getRotationPeriod()
	{
		return this.rotationPeriod;
	}
	
	@Override
	public void renderLocalSky(ClientLevel level, Camera camera, float partialTicks,
			PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		if(atmosphere.isPresent())
			this.atmosphere.get().renderAtmosphere(level, camera, partialTicks, stack, bufferbuilder);
		
		//TODO Make this adjustable to other planets
		Vector3f skyAxisRotation;
		if(rotationPeriod.isPresent())
		{
			double zPos = camera.getEntity().getPosition(partialTicks).z();
			float zRotation = 2 * (float) Math.toDegrees(Math.atan(zPos / (10000 * OverworldConfig.overworld_z_rotation_multiplier.get())));
			
			float defaultYRotation = 360 * level.getTimeOfDay(partialTicks);
			float subtractedYRotation = getAngularVelocity(level, partialTicks) > 0 ?  getAngularVelocity(level, partialTicks) * ((float) level.getDayTime() / TICKS_PER_DAY) : 0;
			
			skyAxisRotation = new Vector3f(180 + defaultYRotation - subtractedYRotation, -90.0F, zRotation);
		}
		else
			skyAxisRotation = new Vector3f(0, 0, 0);
		
		this.renderFrom(this, new Vector3f(0, 0, 0), level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder, skyAxisRotation, new Vector3f(0, 0, 0));
	}
	
	public static class Atmosphere implements StellarViewSkyEffects, StellarViewFogEffects
	{
		protected Optional<ShootingStar> shootingStar = Optional.empty();
		protected Optional<MeteorShower> meteorShower = Optional.empty();
		
		public Atmosphere(ShootingStar shootingStar, MeteorShower meteorShower)
		{
			this.shootingStar = Optional.of(shootingStar);
			this.meteorShower = Optional.of(meteorShower);
		}
		
		public Atmosphere(){}
		
		public void renderAtmosphere(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
		{
			this.renderSkyEvents(level, camera, partialTicks, stack, bufferbuilder);
		}
		
		protected void renderSkyEvents(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
		{
			if(shootingStar.isPresent())
				this.shootingStar.get().render(level, camera, partialTicks, stack, bufferbuilder);
			
			if(meteorShower.isPresent())
				this.meteorShower.get().render(level, camera, partialTicks, stack, bufferbuilder);
		}
	}
}
