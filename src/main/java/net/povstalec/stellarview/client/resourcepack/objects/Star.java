package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Star extends StarLike
{
	@Nullable
	private SupernovaInfo supernovaInfo;
	
	public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Star::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Star::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Star::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Star::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(Star::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(Star::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(Star::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(Star::getMinStarAlpha),
			
			SupernovaInfo.CODEC.optionalFieldOf("supernova_info").forGetter(Star::getSupernovaInfo)
			).apply(instance, Star::new));
	
	public Star() {}
	
	public Star(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
			float minStarSize, float maxStarAlpha, float minStarAlpha,
			Optional<SupernovaInfo> supernovaInfo)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha);
		
		if(supernovaInfo.isPresent())
			this.supernovaInfo = supernovaInfo.get();
		else
			this.supernovaInfo = null;
	}
	
	public boolean isSupernova()
	{
		return this.supernovaInfo != null;
	}
	
	public Optional<SupernovaInfo> getSupernovaInfo()
	{
		return Optional.ofNullable(supernovaInfo);
	}
	
	public float supernovaSize(float size, long ticks, double lyDistance)
	{
		if(supernovaInfo.supernovaEnded(ticks))
			return 0;
		
		if(!supernovaInfo.supernovaStarted(ticks))
			return size;

		long lifetime = supernovaInfo.lifetime(ticks);
		float sizeMultiplier = supernovaInfo.getMaxSizeMultiplier() * (float) Math.sin(Math.PI * lifetime / supernovaInfo.getDurationTicks());
		
		return sizeMultiplier > 1 || (float) lifetime > supernovaInfo.getDurationTicks() / 2 ? sizeMultiplier * size : size;
	}
	
	public float rotation(long ticks)
	{
		if(!isSupernova() || !supernovaInfo.supernovaStarted(ticks))
			return 0;
		
		return (float) (Math.PI * supernovaInfo.lifetime(ticks) / supernovaInfo.getDurationTicks());
	}
	
	
	public Color.FloatRGBA supernovaRGBA(long ticks, double lyDistance)
	{
		Color.FloatRGBA starRGBA = super.starRGBA(lyDistance);
		
		if(!isSupernova() || supernovaInfo.supernovaEnded(ticks) || !supernovaInfo.supernovaStarted(ticks))
			return starRGBA;
		
		float alphaDif = Color.MAX_FLOAT_VALUE - starRGBA.alpha(); // Difference between current star alpha and max alpha
		
		float alpha = starRGBA.alpha() + alphaDif * (float) Math.sin(Math.PI * supernovaInfo.lifetime(ticks) / supernovaInfo.getDurationTicks());
		starRGBA.setAlpha(alpha <= Color.MIN_FLOAT_VALUE ? Color.MIN_FLOAT_VALUE : alpha >= Color.MAX_FLOAT_VALUE ? Color.MAX_FLOAT_VALUE : alpha);
		
		return starRGBA;
	}
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  double fade, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;
		
		Color.FloatRGBA starRGBA = supernovaRGBA(ticks, lyDistance);
		
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
		
		if(isSupernova())
			size = supernovaSize(size, ticks, lyDistance);
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation() + rotation(ticks), textureLayer.shoulBlend());
	}
	
	@Override
	protected void renderTextureLayers(ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double fade = fadeOut(distance);
		
		if(fade <= 0 || isSupernova() && supernovaInfo.supernovaEnded(ticks))
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : textureLayers)
		{
			renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, fade, ticks, distance, partialTicks);
		}
	}
	
	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		
		supernovaInfo = null; //TODO
	}
	
	
	
	public static class SupernovaInfo
	{
		protected Nebula nebula; //TODO Leave a Nebula where there used to be a Supernova
		protected SupernovaLeftover supernovaLeftover; // Whatever is left after Supernova dies
		
		protected float maxSizeMultiplier;
		protected long startTicks;
		protected long durationTicks;
		
		protected long endTicks;
		
		public static final Codec<SupernovaInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("max_size_multiplier").forGetter(SupernovaInfo::getMaxSizeMultiplier),
				Codec.LONG.fieldOf("start_ticks").forGetter(SupernovaInfo::getStartTicks),
				Codec.LONG.fieldOf("duration_ticks").forGetter(SupernovaInfo::getDurationTicks),
				
				Nebula.CODEC.fieldOf("nebula").forGetter(SupernovaInfo::getNebula),
				SupernovaLeftover.CODEC.fieldOf("supernova_leftover").forGetter(SupernovaInfo::getSupernovaLeftover)
				).apply(instance, SupernovaInfo::new));
		
		public SupernovaInfo(float maxSizeMultiplier, long startTicks, long durationTicks, Nebula nebula, SupernovaLeftover supernovaLeftover)
		{
			this.maxSizeMultiplier = maxSizeMultiplier;
			this.startTicks = startTicks;
			this.durationTicks = durationTicks;
			
			this.endTicks = startTicks + durationTicks;
			
			this.nebula = nebula;
			this.supernovaLeftover = supernovaLeftover;
		}
		
		public Nebula getNebula()
		{
			return nebula;
		}
		
		public SupernovaLeftover getSupernovaLeftover()
		{
			return supernovaLeftover;
		}
		
		public float getMaxSizeMultiplier()
		{
			return maxSizeMultiplier;
		}
		
		public long getStartTicks()
		{
			return startTicks;
		}
		
		public long getDurationTicks()
		{
			return durationTicks;
		}
		
		public long getEndTicks()
		{
			return endTicks;
		}
		
		
		
		public boolean supernovaStarted(long ticks)
		{
			return ticks > getStartTicks();
		}
		
		public boolean supernovaEnded(long ticks)
		{
			return ticks > getEndTicks();
		}
		
		public long lifetime(long ticks)
		{
			return ticks - getStartTicks();
		}
		
		//TODO
		/*public static SupernovaInfo fromTag(CompoundTag tag)
		{
		
		}*/
	}
}
