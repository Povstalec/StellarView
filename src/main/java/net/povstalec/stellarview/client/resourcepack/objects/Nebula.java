package net.povstalec.stellarview.client.resourcepack.objects;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;

import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

public class Nebula extends TexturedObject
{
	public static final float MIN_SIZE = 0.4F;
	
	public static final float MAX_ALPHA = 1F;
	public static final float MIN_ALPHA = MAX_ALPHA * 0.1F;
	
	private float minNebulaSize;

	private float maxNebulaAlpha;
	private float minNebulaAlpha;
	
	public static final Codec<Nebula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Nebula::getParentKey),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Nebula::getAxisRotation),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Nebula::getTextureLayers),

			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(Nebula::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_nebula_size", MIN_SIZE).forGetter(Nebula::getMinNebulaSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_nebula_alpha", MAX_ALPHA).forGetter(Nebula::getMaxNebulaAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_nebula_alpha", MIN_ALPHA).forGetter(Nebula::getMinNebulaAlpha)
			).apply(instance, Nebula::new));
	
	public Nebula(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
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
	
	
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, Tesselator tesselator, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;

		Color.FloatRGBA nebulaRGBA = nebulaRGBA(lyDistance);
		
		if(nebulaRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
			{
				size = (float) textureLayer.minSize();
				
				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = nebulaSize(size, lyDistance);
			}
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), nebulaRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, tesselator, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	public static float dayBrightness(ViewCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		if(viewCenter.starsAlwaysVisible())
			return GeneralConfig.bright_stars.get() ? 0.5F * StellarView.lightSourceStarDimming(level, camera) : 0.5F;
		
		float brightness = level.getStarBrightness(partialTicks) * 2;
		
		if(GeneralConfig.bright_stars.get())
			brightness = brightness * StellarView.lightSourceDustCloudDimming(level, camera);
		
		if(brightness < viewCenter.dayMaxBrightness && size > viewCenter.dayMinVisibleSize)
		{
			float aboveSize = size >= viewCenter.dayMaxVisibleSize ? viewCenter.dayVisibleSizeRange : size - viewCenter.dayMinVisibleSize;
			float brightnessPercentage = aboveSize / viewCenter.dayVisibleSizeRange;
			
			brightness = brightnessPercentage * viewCenter.dayMaxBrightness;
		}
		
		return brightness * StellarView.rainDimming(level, partialTicks);
	}
}
