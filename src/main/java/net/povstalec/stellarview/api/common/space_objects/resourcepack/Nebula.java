package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Nebula extends TexturedObject
{
	public static final String MIN_NEBULA_SIZE = "min_nebula_size";
	public static final String MAX_NEBULA_ALPHA = "max_nebula_alpha";
	public static final String MIN_NEBULA_ALPHA = "min_nebula_alpha";
	
	public static final float MIN_SIZE = 0.4F;
	
	public static final float MAX_ALPHA = 1F;
	public static final float MIN_ALPHA = MAX_ALPHA * 0.1F;
	
	private float minNebulaSize;

	private float maxNebulaAlpha;
	private float minNebulaAlpha;
	
	public static final Codec<Nebula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Nebula::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Nebula::getAxisRotation),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Nebula::getTextureLayers),

			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_NEBULA_HANDLER).forGetter(Nebula::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf(MIN_NEBULA_SIZE, MIN_SIZE).forGetter(Nebula::getMinNebulaSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf(MAX_NEBULA_ALPHA, MAX_ALPHA).forGetter(Nebula::getMaxNebulaAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf(MIN_NEBULA_ALPHA, MIN_ALPHA).forGetter(Nebula::getMinNebulaAlpha)
			).apply(instance, Nebula::new));
	
	public Nebula() {}
	
	public Nebula(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
			List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler, float minNebulaSize, float maxNebulaAlpha, float minNebulaAlpha)
	{
		super(parent, coords, axisRotation, textureLayers, fadeOutHandler);
		
		this.minNebulaSize = minNebulaSize;

		this.maxNebulaAlpha = maxNebulaAlpha;
		this.minNebulaAlpha = minNebulaAlpha;
	}
	
	public float getMinNebulaSize()
	{
		return minNebulaSize;
	}
	
	public float getMaxNebulaAlpha()
	{
		return maxNebulaAlpha;
	}
	
	public float getMinNebulaAlpha()
	{
		return minNebulaAlpha;
	}
	
	public float nebulaSize(float size, double lyDistance)
	{
		size -= size * lyDistance / 1000000.0;
		
		if(size < getMinNebulaSize())
			return getMinNebulaSize();
		
		return size;
	}
	
	public Color.FloatRGBA nebulaRGBA(double lyDistance)
	{
		float alpha = getMaxNebulaAlpha();
		
		alpha -= lyDistance / 100000;
		
		if(alpha < getMinNebulaAlpha())
				alpha = getMinNebulaAlpha();
		
		return new Color.FloatRGBA(1, 1, 1, alpha);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider)
	{
		CompoundTag tag = super.serializeNBT(provider);
		
		tag.putFloat(MIN_NEBULA_SIZE, minNebulaSize);
		tag.putFloat(MAX_NEBULA_ALPHA, maxNebulaAlpha);
		tag.putFloat(MIN_NEBULA_ALPHA, minNebulaAlpha);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
	{
		super.deserializeNBT(provider, tag);
		
		minNebulaSize = tag.getFloat(MIN_NEBULA_SIZE);
		maxNebulaAlpha = tag.getFloat(MAX_NEBULA_ALPHA);
		minNebulaAlpha = tag.getFloat(MIN_NEBULA_ALPHA);
	}
}
