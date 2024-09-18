package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;

/**
 * A subtype of planet that should be compatible with enhanced celestials
 */
public class Moon extends Planet
{
	@Nullable
	private Compatibility compatibility;
	
	public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Moon::getParentKey),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Moon::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Moon::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Moon::getTextureLayers),
			
			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(Moon::getFadeOutHandler),
			
			Compatibility.CODEC.optionalFieldOf("compatibility").forGetter(Moon::getCompatibility)
			).apply(instance, Moon::new));
	
	public Moon(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
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
	
	public Color.FloatRGBA rgba(ClientLevel level, float partialTicks)
	{
		// If the Moon is being viewed from the correct dimension, color it differently
		if(getCompatibility().isPresent() && level.dimension().equals(getCompatibility().get().enhancedCelestialsMoonDimension))
			return EnhancedCelestialsCompatibility.getMoonColor(level, partialTicks);
		
		return new Color.FloatRGBA(1F, 1F, 1F);
	}
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		if(!StellarView.isEnhancedCelestialsLoaded())
		{
			super.renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, ticks, distance, partialTicks);
			return;
		}
		
		Color.FloatRGBA rgba = rgba(level, partialTicks);
		
		if(rgba.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
				size = (float) textureLayer.minSize();
			else
				return;
		}
		
		size *= sizeMultiplier(level);
		
		float rotation = (float) textureLayer.rotation();
		
		Vector3f corner00 = StellarCoordinates.placeOnSphere(-size, -size, sphericalCoords, rotation);
		Vector3f corner10 = StellarCoordinates.placeOnSphere(size, -size, sphericalCoords, rotation);
		Vector3f corner11 = StellarCoordinates.placeOnSphere(size, size, sphericalCoords, rotation);
		Vector3f corner01 = StellarCoordinates.placeOnSphere(-size, size, sphericalCoords, rotation);
	
	
		if(textureLayer.shoulBlend())
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		else
			RenderSystem.defaultBlendFunc();
		
		RenderSystem.setShaderColor(rgba.red() * textureLayer.rgba().red() / 255F, rgba.green() * textureLayer.rgba().green() / 255F, rgba.blue() * textureLayer.rgba().blue() / 255F, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * rgba.alpha() * textureLayer.rgba().alpha() / 255F);
		
		RenderSystem.setShaderTexture(0, textureLayer.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(textureLayer.uv().topRight().u(ticks), textureLayer.uv().topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(textureLayer.uv().bottomRight().u(ticks), textureLayer.uv().bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(textureLayer.uv().bottomLeft().u(ticks), textureLayer.uv().bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(textureLayer.uv().topLeft().u(ticks), textureLayer.uv().topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
        RenderSystem.defaultBlendFunc();
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
