package net.povstalec.stellarview.api.common.space_objects;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class StarLike extends OrbitingObject
{
	public static final String MIN_STAR_SIZE = "min_star_size";
	public static final String MAX_STAR_ALPHA = "max_star_alpha";
	public static final String MIN_STAR_ALPHA = "min_star_alpha";
	
	public static final float MIN_SIZE = 0.08F;
	
	public static final float MAX_ALPHA = 1F;
	public static final float MIN_ALPHA = MAX_ALPHA * 0.1F; // Previously used (MAX_ALPHA - 0.66F) * 2 / 5;
	
	private float minStarSize;

	private float maxStarAlpha;
	private float minStarAlpha;
	
	public StarLike() {}
	
	public StarLike(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo,
					List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
					float minStarSize, float maxStarAlpha, float minStarAlpha)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);
		
		this.minStarSize = minStarSize;
		this.maxStarAlpha = maxStarAlpha;
		this.minStarAlpha = minStarAlpha;
	}
	
	public float getMinStarSize()
	{
		return minStarSize;
	}
	
	public float getMaxStarAlpha()
	{
		return maxStarAlpha;
	}
	
	public float getMinStarAlpha()
	{
		return minStarAlpha;
	}
	
	public float starSize(float size, double lyDistance)
	{
		size -= size * lyDistance / 1000000.0;
		
		if(size < getMinStarSize())
			return getMinStarSize();
		
		return size;
	}
	
	public Color.FloatRGBA starRGBA(double lyDistance)
	{
		float alpha = getMaxStarAlpha();
		
		alpha -= lyDistance / 100000;
		
		if(alpha < getMinStarAlpha())
				alpha = getMinStarAlpha();
		
		return new Color.FloatRGBA(1, 1, 1, alpha);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.putFloat(MIN_STAR_SIZE, minStarSize);
		tag.putFloat(MAX_STAR_ALPHA, maxStarAlpha);
		tag.putFloat(MIN_STAR_ALPHA, minStarAlpha);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		minStarSize = tag.getFloat(MIN_STAR_SIZE);
		maxStarAlpha = tag.getFloat(MAX_STAR_ALPHA);
		minStarAlpha = tag.getFloat(MIN_STAR_ALPHA);
	}
	
	
	
	public static class StarType implements INBTSerializable<CompoundTag>
	{
		public static final String RGB = "rgb";
		public static final String MIN_SIZE = "min_size";
		public static final String MAX_SIZE = "max_size";
		public static final String MIN_BRIGHTNESS = "min_brightness";
		public static final String MAX_BRIGHTNESS = "max_brightness";
		public static final String MAX_VISIBLE_DISTANCE = "max_visible_distance";
		public static final String WEIGHT = "weight";
		
		private Color.IntRGB rgb;

		private float minSize;
		private float maxSize;

		private short minBrightness;
		private short maxBrightness;
		
		public long maxVisibleDistance;
		
		public int weight;
		
		public static final Codec<StarType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Color.IntRGB.CODEC.fieldOf(RGB).forGetter(StarType::getRGB),
				
				Codec.FLOAT.fieldOf(MIN_SIZE).forGetter(starType -> starType.minSize),
				Codec.FLOAT.fieldOf(MAX_SIZE).forGetter(starType -> starType.maxSize),
				
				Codec.SHORT.fieldOf(MIN_BRIGHTNESS).forGetter(starType -> starType.minBrightness),
				Codec.SHORT.fieldOf(MAX_BRIGHTNESS).forGetter(starType -> starType.maxBrightness),
				
				Codec.LONG.optionalFieldOf(MAX_VISIBLE_DISTANCE, Long.MAX_VALUE).forGetter(StarType::getMaxVisibleDistance),
				
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf(WEIGHT).forGetter(StarType::getWeight)
				).apply(instance, StarType::new));
		
		public StarType() {}
		
		public StarType(Color.IntRGB rgb, float minSize, float maxSize, short minBrightness, short maxBrightness, long maxVisibleDistance, int weight)
		{
			this.rgb = rgb;
			
			this.minSize = minSize;
			this.maxSize = maxSize;

			this.minBrightness = minBrightness;
			this.maxBrightness = maxBrightness;
			
			this.maxVisibleDistance = maxVisibleDistance;

			this.weight = weight;
		}
		
		public Color.IntRGB getRGB() // TODO Maybe random RGB?
		{
			return rgb;
		}
		
		public int getWeight()
		{
			return weight;
		}
		
		public float randomSize(Random random)
		{
			if(minSize == maxSize)
				return maxSize;
			
			return random.nextFloat(minSize, maxSize);
		}
		
		public short randomBrightness(Random random)
		{
			if(minBrightness == maxBrightness)
				return maxBrightness;
			
			return (short) random.nextInt(minBrightness, maxBrightness + 1);
		}
		
		public long getMaxVisibleDistance()
		{
			return maxVisibleDistance;
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.put(RGB, rgb.serializeNBT());
			
			tag.putFloat(MIN_SIZE, minSize);
			tag.putFloat(MAX_SIZE, maxSize);
			
			tag.putShort(MIN_BRIGHTNESS, minBrightness);
			tag.putShort(MAX_BRIGHTNESS, maxBrightness);
			
			tag.putLong(MAX_VISIBLE_DISTANCE, maxVisibleDistance);
			
			tag.putInt(WEIGHT, weight);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			rgb = new Color.IntRGB();
			rgb.deserializeNBT(tag.getCompound(RGB));
			
			minSize = tag.getFloat(MIN_SIZE);
			maxSize = tag.getFloat(MAX_SIZE);
			
			minBrightness = tag.getShort(MIN_BRIGHTNESS);
			maxBrightness = tag.getShort(MAX_BRIGHTNESS);
			
			maxVisibleDistance = tag.getLong(MAX_VISIBLE_DISTANCE);
			
			weight = tag.getInt(WEIGHT);
		}
	}
}
