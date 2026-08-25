package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A subtype of planet that should be compatible with enhanced celestials
 */
public class Moon extends Planet
{
	@Nullable
	private Compatibility compatibility;
	
	public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ParentInfo.CODEC.optionalFieldOf("parent").forGetter(Moon::getParentInfo),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Moon::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(moon -> Optional.ofNullable(moon.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Moon::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(Moon::getFadeOutHandler),
			
			Compatibility.CODEC.optionalFieldOf("compatibility").forGetter(Moon::getCompatibility)
			).apply(instance, Moon::new));
	
	public Moon() {}
	
	public Moon(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
				Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
				Optional<Compatibility> compatibility)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);

		if(compatibility.isPresent())
			this.compatibility = compatibility.get();
	}
	
	public Optional<Compatibility> getCompatibility()
	{
		return Optional.ofNullable(compatibility);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		//TODO Serialize Compatibility
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		//TODO Deserialize Compatibility
	}
	
	
	
	public static class Compatibility
	{
		@Nullable
		private ResourceKey<Level> enhancedCelestialsMoonDimension;
		private ResourceKey<Level> lunarMoonDimension;
		
		public static final Codec<Compatibility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.optionalFieldOf("enhanced_celestials_moon_dimension").forGetter(Compatibility::getEnhancedCelestialsMoonDimension),
				Level.RESOURCE_KEY_CODEC.optionalFieldOf("lunar_moon_dimension").forGetter(Compatibility::getLunarMoonDimension)
				).apply(instance, Compatibility::new));
		
		public Compatibility(Optional<ResourceKey<Level>> enhancedCelestialsMoonDimension, Optional<ResourceKey<Level>> lunarMoonDimension)
		{
			this.enhancedCelestialsMoonDimension = enhancedCelestialsMoonDimension.orElse(null);
			this.lunarMoonDimension = lunarMoonDimension.orElse(null);
		}
		
		public Optional<ResourceKey<Level>> getEnhancedCelestialsMoonDimension()
		{
			return Optional.ofNullable(enhancedCelestialsMoonDimension);
		}
		
		public Optional<ResourceKey<Level>> getLunarMoonDimension()
		{
			return Optional.ofNullable(lunarMoonDimension);
		}
	}
}
