package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Supernova extends Star
{
	private SupernovaInfo supernovaInfo;
	
	public static final Codec<Supernova> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Supernova::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(Supernova::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Supernova::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Supernova::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Supernova::getTextureLayers),
			
			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(Supernova::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(Supernova::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(Supernova::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(Supernova::getMinStarAlpha),
			
			SupernovaInfo.CODEC.fieldOf("supernova_info").forGetter(Supernova::getSupernovaInfo)
			).apply(instance, Supernova::new));
	
	public Supernova(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
			float minStarSize, float maxStarAlpha, float minStarAlpha,
			SupernovaInfo supernovaInfo)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha);
		
		this.supernovaInfo = supernovaInfo;
	}
	
	public SupernovaInfo getSupernovaInfo()
	{
		return supernovaInfo;
	}
	
	public boolean supernovaStarted(long ticks)
	{
		return ticks > supernovaInfo.getStartTicks();
	}
	
	public boolean supernovaEnded(long ticks)
	{
		return ticks > supernovaInfo.getEndTicks();
	}
	
	public long lifetime(long ticks)
	{
		return ticks - supernovaInfo.getStartTicks();
	}
	
	public float supernovaSize(float size, long ticks, double lyDistance)
	{
		if(!supernovaStarted(ticks))
			return size;

		if(supernovaEnded(ticks))
			return 0;
		
		long lifetime = lifetime(ticks);
		float sizeMultiplier = supernovaInfo.getMaxSizeMultiplier() * (float) Math.sin(Math.PI * lifetime / supernovaInfo.getDurationTicks());
		
		return sizeMultiplier > 1 || (float) lifetime > supernovaInfo.getDurationTicks() / 2 ? sizeMultiplier * size : size;
	}
	
	public float rotation(long ticks)
	{
		if(!supernovaStarted(ticks))
			return 0;
		
		return (float) (Math.PI * lifetime(ticks) / supernovaInfo.getDurationTicks());
	}
	
	
	public Color.FloatRGBA supernovaRGBA(long ticks, double lyDistance)
	{
		Color.FloatRGBA starRGBA = super.starRGBA(lyDistance);
		
		if(!supernovaStarted(ticks))
			return starRGBA;

		if(supernovaEnded(ticks))
			return starRGBA;
		
		float alphaDif = Color.MAX_FLOAT_VALUE - starRGBA.alpha(); // Difference between current star alpha and max alpha
		
		float alpha = starRGBA.alpha() + alphaDif * (float) Math.sin(Math.PI * lifetime(ticks) / supernovaInfo.getDurationTicks());
		starRGBA.setAlpha(alpha <= Color.MIN_FLOAT_VALUE ? Color.MIN_FLOAT_VALUE : alpha >= Color.MAX_FLOAT_VALUE ? Color.MAX_FLOAT_VALUE : alpha);
		
		return starRGBA;
	}
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.LY_TO_KM;
		
		Color.FloatRGBA rgba = supernovaRGBA(ticks, lyDistance);
		
		if(rgba.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
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
		
		size = supernovaSize(size, ticks, lyDistance);
		
		float rotation = (float) textureLayer.rotation(rotation(ticks));
		
		Vector3f corner00 = StellarCoordinates.placeOnSphere(-size, -size, sphericalCoords, rotation);
		Vector3f corner10 = StellarCoordinates.placeOnSphere(size, -size, sphericalCoords, rotation);
		Vector3f corner11 = StellarCoordinates.placeOnSphere(size, size, sphericalCoords, rotation);
		Vector3f corner01 = StellarCoordinates.placeOnSphere(-size, size, sphericalCoords, rotation);
	
	
		if(textureLayer.shoulBlend())
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		else
			RenderSystem.defaultBlendFunc();
		
		RenderSystem.setShaderColor(rgba.red() * textureLayer.rgba().red() / 255F, rgba.green() * textureLayer.rgba().green() / 255F, rgba.blue() * textureLayer.rgba().blue() / 255F, dayBrightness(size, ticks, level, camera, partialTicks) * rgba.alpha() * textureLayer.rgba().alpha() / 255F);
		
		RenderSystem.setShaderTexture(0, textureLayer.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(textureLayer.uv().topRight().u(ticks), textureLayer.uv().topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(textureLayer.uv().bottomRight().u(ticks), textureLayer.uv().bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(textureLayer.uv().bottomLeft().u(ticks), textureLayer.uv().bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(textureLayer.uv().topLeft().u(ticks), textureLayer.uv().topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
        RenderSystem.defaultBlendFunc();
	}
	
	
	
	public static class SupernovaInfo
	{
		protected Nebula nebula; //TODO Leave a Nebula where there used to be a Supernova
		protected Leftover supernovaLeftover; // Whatever is left after Supernova dies
		
		protected float maxSizeMultiplier;
		protected long startTicks;
		protected long durationTicks;
		
		protected long endTicks;
		
		public static final Codec<SupernovaInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Nebula.CODEC.fieldOf("nebula").forGetter(SupernovaInfo::getNebula),
				Leftover.CODEC.fieldOf("supernova_leftover").forGetter(SupernovaInfo::getSupernovaLeftover),
				Codec.FLOAT.fieldOf("max_size_multiplier").forGetter(SupernovaInfo::getMaxSizeMultiplier),
				Codec.LONG.fieldOf("start_ticks").forGetter(SupernovaInfo::getStartTicks),
				Codec.LONG.fieldOf("duration_ticks").forGetter(SupernovaInfo::getDurationTicks)
				).apply(instance, SupernovaInfo::new));
		
		public SupernovaInfo(Nebula nebula, Leftover supernovaLeftover, float maxSizeMultiplier, long startTicks, long durationTicks)
		{
			this.nebula = nebula;
			this.supernovaLeftover = supernovaLeftover;
			
			this.maxSizeMultiplier = maxSizeMultiplier;
			this.startTicks = startTicks;
			this.durationTicks = durationTicks;
			
			this.endTicks = startTicks + durationTicks;
		}
		
		public Nebula getNebula()
		{
			return nebula;
		}
		
		public Leftover getSupernovaLeftover()
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
	}
	
	// This mainly exists to make sure that whatever is left after a Supernova can't be a Supernova itself
	public static class Leftover extends Star
	{
		public static final Codec<Leftover> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Leftover::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(Leftover::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Leftover::getAxisRotation),
				OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Leftover::getOrbitInfo),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Leftover::getTextureLayers),
				
				SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(Leftover::getFadeOutHandler),
				
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(Leftover::getMinStarSize),
				Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(Leftover::getMaxStarAlpha),
				Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(Leftover::getMinStarAlpha)
				).apply(instance, Leftover::new));
		
		public Leftover(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
				float minStarSize, float maxStarAlpha, float minStarAlpha)
		{
			super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha);
		}
		
	}
}
