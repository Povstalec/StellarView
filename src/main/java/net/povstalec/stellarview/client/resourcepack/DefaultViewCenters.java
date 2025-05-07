package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.client.resourcepack.effects.MeteorEffect;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.config.EndConfig;
import net.povstalec.stellarview.common.config.NetherConfig;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.util.AxisRotation;

public final class DefaultViewCenters
{
	public static class Overworld extends ViewCenter
	{
		public static final Codec<Overworld> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		SpaceObject.RESOURCE_KEY_CODEC.optionalFieldOf("view_center").forGetter(ViewCenter::getViewCenterKey),
				Skybox.CODEC.listOf().optionalFieldOf("skyboxes").forGetter(ViewCenter::getSkyboxes),
				
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(ViewCenter::getAxisRotation),
				Codec.LONG.optionalFieldOf("rotation_period", 0L).forGetter(ViewCenter::getRotationPeriod),
				
				DayBlending.CODEC.optionalFieldOf("day_blending", DayBlending.DAY_BLENDING).forGetter(viewCenter -> viewCenter.dayBlending),
				DayBlending.CODEC.optionalFieldOf("sun_day_blending", DayBlending.SUN_DAY_BLENDING).forGetter(viewCenter -> viewCenter.sunDayBlending),
				
				MeteorEffect.ShootingStar.CODEC.optionalFieldOf("shooting_star").forGetter(viewCenter -> Optional.ofNullable(viewCenter.shootingStar)),
				MeteorEffect.MeteorShower.CODEC.optionalFieldOf("meteor_shower").forGetter(viewCenter -> Optional.ofNullable(viewCenter.meteorShower)),
				
				Codec.BOOL.optionalFieldOf("create_horizon", true).forGetter(viewCenter -> viewCenter.createHorizon),
				Codec.BOOL.optionalFieldOf("create_void", true).forGetter(viewCenter -> viewCenter.createVoid),
				
				Codec.BOOL.optionalFieldOf("stars_always_visible", false).forGetter(viewCenter -> viewCenter.starsAlwaysVisible),
				Codec.BOOL.optionalFieldOf("stars_ignore_fog", false).forGetter(viewCenter -> viewCenter.starsIgnoreFog),
				Codec.BOOL.optionalFieldOf("stars_ignore_rain", false).forGetter(viewCenter -> viewCenter.starsIgnoreRain),
				Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("z_rotation_multiplier", 30000000).forGetter(viewCenter -> viewCenter.zRotationMultiplier)
				).apply(instance, Overworld::new));
		
		public Overworld(Optional<ResourceKey<SpaceObject>> viewCenterKey, Optional<List<Skybox>> skyboxes, AxisRotation axisRotation,
				long rotationPeriod, DayBlending dayBlending, DayBlending sunDayBlending,
						 Optional<MeteorEffect.ShootingStar> shootingStar, Optional<MeteorEffect.MeteorShower> meteorShower,
				boolean createHorizon, boolean createVoid,
				boolean starsAlwaysVisible, boolean starsIgnoreFog, boolean starsIgnoreRain, int zRotationMultiplier)
		{
			super(viewCenterKey, skyboxes, axisRotation,
					rotationPeriod, dayBlending,sunDayBlending,
					shootingStar, meteorShower, createHorizon, createVoid,
					starsAlwaysVisible, starsIgnoreFog, starsIgnoreRain, zRotationMultiplier);
		}
		
		public double zRotationMultiplier()
		{
			return OverworldConfig.config_priority.get() ? 10000 * OverworldConfig.overworld_z_rotation_multiplier.get() : zRotationMultiplier;
		}
		
		public boolean overrideMeteorEffects()
		{
			return OverworldConfig.config_priority.get();
		}
		
		public double overrideShootingStarRarity()
		{
			return OverworldConfig.shooting_star_chance.get();
		}
		
		public double overrideMeteorShowerRarity()
		{
			return OverworldConfig.meteor_shower_chance.get();
		}
	}
	
	public static class Nether extends ViewCenter
	{
		public static final Codec<Nether> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		SpaceObject.RESOURCE_KEY_CODEC.optionalFieldOf("view_center").forGetter(ViewCenter::getViewCenterKey),
				Skybox.CODEC.listOf().optionalFieldOf("skyboxes").forGetter(ViewCenter::getSkyboxes),
				
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(ViewCenter::getAxisRotation),
				Codec.LONG.optionalFieldOf("rotation_period", 0L).forGetter(ViewCenter::getRotationPeriod),
				
				DayBlending.CODEC.optionalFieldOf("day_blending", DayBlending.DAY_BLENDING).forGetter(viewCenter -> viewCenter.dayBlending),
				DayBlending.CODEC.optionalFieldOf("sun_day_blending", DayBlending.SUN_DAY_BLENDING).forGetter(viewCenter -> viewCenter.sunDayBlending),
				
				MeteorEffect.ShootingStar.CODEC.optionalFieldOf("shooting_star").forGetter(viewCenter -> Optional.ofNullable(viewCenter.shootingStar)),
				MeteorEffect.MeteorShower.CODEC.optionalFieldOf("meteor_shower").forGetter(viewCenter -> Optional.ofNullable(viewCenter.meteorShower)),
				
				Codec.BOOL.optionalFieldOf("create_horizon", true).forGetter(viewCenter -> viewCenter.createHorizon),
				Codec.BOOL.optionalFieldOf("create_void", true).forGetter(viewCenter -> viewCenter.createVoid),
				
				Codec.BOOL.optionalFieldOf("stars_always_visible", false).forGetter(viewCenter -> viewCenter.starsAlwaysVisible),
				Codec.BOOL.optionalFieldOf("stars_ignore_fog", false).forGetter(viewCenter -> viewCenter.starsIgnoreFog),
				Codec.BOOL.optionalFieldOf("stars_ignore_rain", false).forGetter(viewCenter -> viewCenter.starsIgnoreRain),
				Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("z_rotation_multiplier", 0).forGetter(viewCenter -> viewCenter.zRotationMultiplier)
				).apply(instance, Nether::new));
		
		public Nether(Optional<ResourceKey<SpaceObject>> viewCenterKey, Optional<List<Skybox>> skyboxes, AxisRotation axisRotation,
				long rotationPeriod, DayBlending dayBlending, DayBlending sunDayBlending,
					  Optional<MeteorEffect.ShootingStar> shootingStar, Optional<MeteorEffect.MeteorShower> meteorShower,
				boolean createHorizon, boolean createVoid,
					  boolean starsAlwaysVisible, boolean starsIgnoreFog, boolean starsIgnoreRain, int zRotationMultiplier)
		{
			super(viewCenterKey, skyboxes, axisRotation,
					rotationPeriod, dayBlending,sunDayBlending,
					shootingStar, meteorShower, createHorizon, createVoid,
					starsAlwaysVisible, starsIgnoreFog, starsIgnoreRain, zRotationMultiplier);
		}
		
		public boolean overrideMeteorEffects()
		{
			return NetherConfig.config_priority.get();
		}
		
		public double overrideShootingStarRarity()
		{
			return NetherConfig.shooting_star_chance.get();
		}
		
		public double overrideMeteorShowerRarity()
		{
			return NetherConfig.meteor_shower_chance.get();
		}
	}
	
	public static class End extends ViewCenter
	{
		public static final Codec<End> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		SpaceObject.RESOURCE_KEY_CODEC.optionalFieldOf("view_center").forGetter(ViewCenter::getViewCenterKey),
				Skybox.CODEC.listOf().optionalFieldOf("skyboxes").forGetter(ViewCenter::getSkyboxes),
				
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(ViewCenter::getAxisRotation),
				Codec.LONG.optionalFieldOf("rotation_period", 0L).forGetter(ViewCenter::getRotationPeriod),
				
				DayBlending.CODEC.optionalFieldOf("day_blending", DayBlending.DAY_BLENDING).forGetter(viewCenter -> viewCenter.dayBlending),
				DayBlending.CODEC.optionalFieldOf("sun_day_blending", DayBlending.SUN_DAY_BLENDING).forGetter(viewCenter -> viewCenter.sunDayBlending),
				
				MeteorEffect.ShootingStar.CODEC.optionalFieldOf("shooting_star").forGetter(viewCenter -> Optional.ofNullable(viewCenter.shootingStar)),
				MeteorEffect.MeteorShower.CODEC.optionalFieldOf("meteor_shower").forGetter(viewCenter -> Optional.ofNullable(viewCenter.meteorShower)),
				
				Codec.BOOL.optionalFieldOf("create_horizon", true).forGetter(viewCenter -> viewCenter.createHorizon),
				Codec.BOOL.optionalFieldOf("create_void", true).forGetter(viewCenter -> viewCenter.createVoid),
				
				Codec.BOOL.optionalFieldOf("stars_always_visible", false).forGetter(viewCenter -> viewCenter.starsAlwaysVisible),
				Codec.BOOL.optionalFieldOf("stars_ignore_fog", false).forGetter(viewCenter -> viewCenter.starsIgnoreFog),
				Codec.BOOL.optionalFieldOf("stars_ignore_rain", false).forGetter(viewCenter -> viewCenter.starsIgnoreRain),
				Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("z_rotation_multiplier", 0).forGetter(viewCenter -> viewCenter.zRotationMultiplier)
				).apply(instance, End::new));
		
		public End(Optional<ResourceKey<SpaceObject>> viewCenterKey, Optional<List<Skybox>> skyboxes, AxisRotation axisRotation,
				long rotationPeriod, DayBlending dayBlending, DayBlending sunDayBlending,
				   Optional<MeteorEffect.ShootingStar> shootingStar, Optional<MeteorEffect.MeteorShower> meteorShower,
				boolean createHorizon, boolean createVoid,
				   boolean starsAlwaysVisible, boolean starsIgnoreFog, boolean starsIgnoreRain, int zRotationMultiplier)
		{
			super(viewCenterKey, skyboxes, axisRotation,
					rotationPeriod, dayBlending,sunDayBlending,
					shootingStar, meteorShower, createHorizon, createVoid,
					starsAlwaysVisible, starsIgnoreFog, starsIgnoreRain, zRotationMultiplier);
		}
		
		public boolean overrideMeteorEffects()
		{
			return EndConfig.config_priority.get();
		}
		
		public double overrideShootingStarRarity()
		{
			return EndConfig.shooting_star_chance.get();
		}
		
		public double overrideMeteorShowerRarity()
		{
			return EndConfig.meteor_shower_chance.get();
		}
	}
}
