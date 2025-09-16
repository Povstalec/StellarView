package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;
import org.jetbrains.annotations.Nullable;

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
	
	public float sizeMultiplier(ClientLevel level)
	{
		// If the Moon is being viewed from the correct dimension, make it larger
		if(getCompatibility().isPresent() && level.dimension().equals(getCompatibility().get().enhancedCelestialsMoonDimension))
			return EnhancedCelestialsCompatibility.getMoonSize(level, 20) / 20F;
		
		return 1F;
	}
	
	public Color.FloatRGBA moonRGBA(ClientLevel level, float partialTicks)
	{
		// If the Moon is being viewed from the correct dimension, color it differently
		if(getCompatibility().isPresent() && level.dimension().equals(getCompatibility().get().enhancedCelestialsMoonDimension))
			return EnhancedCelestialsCompatibility.getMoonColor(level, partialTicks);
		
		return new Color.FloatRGBA(1F, 1F, 1F);
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
		private ResourceKey<Level> enhancedCelestialsMoonDimension;
		
		public static final Codec<Compatibility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.fieldOf("enhanced_celestials_moon_dimension").forGetter(Compatibility::getEnhancedCelestialsMoonDimension)
				).apply(instance, Compatibility::new));
		
		public Compatibility(ResourceKey<Level> enhancedCelestialsMoonDimension)
		{
			this.enhancedCelestialsMoonDimension = enhancedCelestialsMoonDimension;
		}
		
		public ResourceKey<Level> getEnhancedCelestialsMoonDimension()
		{
			return enhancedCelestialsMoonDimension;
		}
	}
}
