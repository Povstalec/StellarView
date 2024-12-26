package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.resourcepack.DustCloudInfo;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.StellarView;
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
	public static final float MIN_SIZE = 0.08F;
	
	public static final float MAX_ALPHA = 1F;
	public static final float MIN_ALPHA = MAX_ALPHA * 0.1F; // Previously used (MAX_ALPHA - 0.66F) * 2 / 5;
	
	private float minStarSize;

	private float maxStarAlpha;
	private float minStarAlpha;
	
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
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
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
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
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
			starBrightness = starBrightness * StellarView.lightSourceStarDimming(level, camera);
		
		starBrightness = starBrightness * StellarView.rainDimming(level, partialTicks);
		
		return starBrightness;
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
		public static final String WEIGHT = "weight";
		
		private final Color.IntRGB rgb;

		private final float minSize;
		private final float maxSize;

		private final short minBrightness;
		private final short maxBrightness;
		
		public final int weight;
		
		public static final Codec<StarType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Color.IntRGB.CODEC.fieldOf("rgb").forGetter(StarType::getRGB),
				
				Codec.FLOAT.fieldOf("min_size").forGetter(starType -> starType.minSize),
				Codec.FLOAT.fieldOf("max_size").forGetter(starType -> starType.maxSize),
				
				Codec.SHORT.fieldOf("min_brightness").forGetter(starType -> starType.minBrightness),
				Codec.SHORT.fieldOf("max_brightness").forGetter(starType -> starType.maxBrightness),
				
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(StarType::getWeight)
				).apply(instance, StarType::new));
		
		public StarType(Color.IntRGB rgb, float minSize, float maxSize, short minBrightness, short maxBrightness, int weight)
		{
			this.rgb = rgb;
			
			this.minSize = minSize;
			this.maxSize = maxSize;

			this.minBrightness = minBrightness;
			this.maxBrightness = (short) (maxBrightness + 1);

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
		
		public float randomSize(long seed)
		{
			if(minSize == maxSize)
				return maxSize;
			
			Random random = new Random(seed);
			
			return random.nextFloat(minSize, maxSize);
		}
		
		public short randomBrightness(long seed)
		{
			if(minBrightness == maxBrightness)
				return maxBrightness;
			
			Random random = new Random(seed);
			
			return (short) random.nextInt(minBrightness, maxBrightness);
		}
		
		public static StarType fromTag(CompoundTag tag)
		{
			Color.IntRGB rgb = new Color.IntRGB();
			rgb.fromTag(tag.getCompound(RGB));
			
			return new StarType(rgb, tag.getFloat(MIN_SIZE), tag.getFloat(MAX_SIZE), tag.getShort(MIN_BRIGHTNESS), tag.getShort(MAX_BRIGHTNESS), tag.getInt(WEIGHT));
		}
	}
}
