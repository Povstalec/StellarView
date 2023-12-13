package net.povstalec.stellarview.api.celestials.orbiting;

import java.util.Optional;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.sky_effects.MeteorShower;
import net.povstalec.stellarview.api.sky_effects.ShootingStar;
import net.povstalec.stellarview.client.render.level.misc.StellarViewFogEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkyEffects;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class Planet extends OrbitingCelestialObject
{
	public static final ResourceLocation VENUS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/venus.png");
	//TODO Change
	public static final ResourceLocation EARTH_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/venus.png");
	
	public static final ResourceLocation MARS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/mars.png");
	public static final ResourceLocation MARS_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/mars_halo.png");
	
	public static final ResourceLocation JUPITER_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/planet/jupiter.png");
	
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
	public void renderLocalSky(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		if(atmosphere.isPresent())
			this.atmosphere.get().renderAtmosphere(level, camera, partialTicks, stack, bufferbuilder);
		
		//TODO Make this adjustable to other planets
		Vector3f skyAxisRotation;
		if(rotationPeriod.isPresent())
		{
			double zPos = camera.getEntity().getPosition(partialTicks).z();
			float zRotation = 2 * (float) Math.toDegrees(Math.atan(zPos / (100000 * StellarViewConfig.rotation_multiplier.get())));
			
			float defaultYRotation = 360 * level.getTimeOfDay(partialTicks);
			float subtractedYRotation = this.angularVelocity > 0 ? this.angularVelocity * ((float) level.getDayTime() / TICKS_PER_DAY) : 0;
			
			skyAxisRotation = new Vector3f(180 + defaultYRotation - subtractedYRotation, -90.0F, zRotation);
		}
		else
			skyAxisRotation = new Vector3f(0, 0, 0);
		
		this.renderFrom(this, new Vector3f(0, 0, 0), level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation);
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
