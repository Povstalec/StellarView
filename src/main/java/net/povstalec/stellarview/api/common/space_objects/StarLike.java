package net.povstalec.stellarview.api.common.space_objects;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.LightEffects;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
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
	
	public StarLike(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo,
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
	
	
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  double fade, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;

		Color.FloatRGBA starRGBA = starRGBA(lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
			{
				size = (float) textureLayer.minSize();
				
				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = starSize(size, lyDistance);
			}
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, LightEffects.dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	
	
	/**
	 * Returns the brightness of stars in the current Player location
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float getStarBrightness(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks)
	{
		float starBrightness = level.getStarBrightness(partialTicks);
		
		if(viewCenter.starsAlwaysVisible() && starBrightness < 0.5F)
			starBrightness = 0.5F;
		
		if(GeneralConfig.bright_stars.get())
			starBrightness = starBrightness * LightEffects.lightSourceStarDimming(level, camera);
		
		starBrightness = starBrightness * LightEffects.rainDimming(level, partialTicks);
		
		return starBrightness;
	}
	
	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		
		minStarSize = tag.getFloat(MIN_STAR_SIZE);
		maxStarAlpha = tag.getFloat(MAX_STAR_ALPHA);
		minStarAlpha = tag.getFloat(MIN_STAR_ALPHA);
	}
	
	
	
	//TODO Use this again
	public double starWidthFunction(double aLocation, double bLocation, double sinRandom, double cosRandom, double sinTheta, double cosTheta, double sinPhi, double cosPhi)
	{
		if(true)
			return cosPhi  > 0.0 ? cosPhi * 8 * (bLocation * cosRandom + aLocation * sinRandom) : bLocation * cosRandom + aLocation * sinRandom;
		
		return bLocation * cosRandom + aLocation * sinRandom;
	}
	
	
	
	public static class StarType
	{
		public static final String RGB = "rgb";
		public static final String MIN_SIZE = "min_size";
		public static final String MAX_SIZE = "max_size";
		public static final String MIN_BRIGHTNESS = "min_brightness";
		public static final String MAX_BRIGHTNESS = "max_brightness";
		public static final String MAX_VISIBLE_DISTANCE = "max_visible_distance";
		public static final String WEIGHT = "weight";
		
		private final Color.IntRGB rgb;

		private final float minSize;
		private final float maxSize;

		private final short minBrightness;
		private final short maxBrightness;
		
		public final long maxVisibleDistance;
		
		public final int weight;
		
		public static final Codec<StarType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Color.IntRGB.CODEC.fieldOf(RGB).forGetter(StarType::getRGB),
				
				Codec.FLOAT.fieldOf(MIN_SIZE).forGetter(starType -> starType.minSize),
				Codec.FLOAT.fieldOf(MAX_SIZE).forGetter(starType -> starType.maxSize),
				
				Codec.SHORT.fieldOf(MIN_BRIGHTNESS).forGetter(starType -> starType.minBrightness),
				Codec.SHORT.fieldOf(MAX_BRIGHTNESS).forGetter(starType -> starType.maxBrightness),
				
				Codec.LONG.optionalFieldOf(MAX_VISIBLE_DISTANCE, Long.MAX_VALUE).forGetter(StarType::getMaxVisibleDistance),
				
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf(WEIGHT).forGetter(StarType::getWeight)
				).apply(instance, StarType::new));
		
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
		
		public static StarType fromTag(CompoundTag tag)
		{
			Color.IntRGB rgb = new Color.IntRGB();
			rgb.fromTag(tag.getCompound(RGB));
			
			return new StarType(rgb, tag.getFloat(MIN_SIZE), tag.getFloat(MAX_SIZE), tag.getShort(MIN_BRIGHTNESS), tag.getShort(MAX_BRIGHTNESS), tag.getLong(MAX_VISIBLE_DISTANCE), tag.getInt(WEIGHT));
		}
	}
}
