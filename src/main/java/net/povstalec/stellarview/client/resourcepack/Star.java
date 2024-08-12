package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;
import java.util.Random;

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
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Star extends OrbitingObject
{
	public static final float MIN_SIZE = 0.4F;
	
	public static final float MAX_ALPHA = 1F;
	public static final float MIN_ALPHA = (MAX_ALPHA - 0.66F) * 2 / 5;
	
	private float minStarSize;

	private float maxStarAlpha;
	private float minStarAlpha;
	
	public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Star::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(Star::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Star::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Star::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Star::getTextureLayers),
			
			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(Star::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(Star::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(Star::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(Star::getMinStarAlpha)
			).apply(instance, Star::new));
	
	public Star(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo,
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
		
		if(size < 0.01F)
			return 0.01F;
		
		return size;
	}
	
	public Color.FloatRGBA starRGBA(double lyDistance)
	{
		float alpha = 1;
		float minAlpha = (alpha - 0.66F) * 2 / 5; // Previously used (alpha - 0.66) * 2 / 3
		
		//if(lyDistance > 10000) // Stars more than 10000 light years away appear dimmer
		alpha -= lyDistance / 100000;
		
		if(alpha < minAlpha)
				alpha = minAlpha;
		
		return new Color.FloatRGBA(1, 1, 1, alpha);
	}
	
	
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.LY_TO_KM;
		
		Color.FloatRGBA rgba = starRGBA(lyDistance);
		
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
		
		float rotation = (float) textureLayer.rotation();
		
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
	
	
	
	/**
	 * Returns the brightness of stars in the current Player location
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float getStarBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float starBrightness = level.getStarBrightness(partialTicks);
		
		starBrightness = StellarViewConfig.day_stars.get() && starBrightness < 0.5F ? 0.5F : starBrightness;
		
		if(StellarViewConfig.bright_stars.get())
			starBrightness = starBrightness * StellarView.lightSourceDimming(level, camera);
		
		starBrightness = starBrightness * StellarView.rainDimming(level, partialTicks);
		
		return starBrightness;
	}
	
	
	
	private static final short CONTRAST = 8;
	
	private static final float MIN_STAR_SIZE = 0.15F;
	private static final float MAX_STAR_SIZE = 0.25F;
	
	private static final short MIN_STAR_BRIGHTNESS = 170; // 0xAA
	private static final short MAX_STAR_BRIGHTNESS = 255; // 0xFF
	
	public enum Type
	{
		O(255 - (CONTRAST * 6), 255 - (CONTRAST * 6), 255, MAX_STAR_SIZE, MAX_STAR_SIZE, MAX_STAR_BRIGHTNESS, MAX_STAR_BRIGHTNESS), // 0.00003%
		B(255 - (CONTRAST * 4), 255 - (CONTRAST * 4), 255, MIN_STAR_SIZE + 0.09F, MAX_STAR_SIZE, MIN_STAR_BRIGHTNESS + 65, MAX_STAR_BRIGHTNESS), // 0.12%
		A(255 - (CONTRAST * 2), 255 - (CONTRAST * 2), 255, MIN_STAR_SIZE + 0.05F, MIN_STAR_SIZE + 0.07F, MIN_STAR_BRIGHTNESS + 45, MIN_STAR_BRIGHTNESS + 65), // 0.61%
		F(255, 255, 255, MIN_STAR_SIZE + 0.035F, MIN_STAR_SIZE + 0.05F, MIN_STAR_BRIGHTNESS + 30, MIN_STAR_BRIGHTNESS + 45), // 3.0%
		G(255, 255, 255 - (CONTRAST * 4), MIN_STAR_SIZE + 0.02F, MIN_STAR_SIZE + 0.035F, MIN_STAR_BRIGHTNESS + 20, MIN_STAR_BRIGHTNESS + 30), // 7.6%
		K(255, 255 - (CONTRAST * 2), 255 - (CONTRAST * 4), MIN_STAR_SIZE + 0.01F, MIN_STAR_SIZE + 0.02F, MIN_STAR_BRIGHTNESS + 10, MIN_STAR_BRIGHTNESS + 20), // 12%
		M(255, 255 - (CONTRAST * 4), 255 - (CONTRAST * 4), MIN_STAR_SIZE, MIN_STAR_SIZE + 0.01F, MIN_STAR_BRIGHTNESS, MIN_STAR_BRIGHTNESS + 10); // 76%
		
		private final short red;
		private final short green;
		private final short blue;

		private final float minSize;
		private final float maxSize;

		private final short minBrightness;
		private final short maxBrightness;
		
		Type(int red, int green, int blue, float minSize, float maxSize, int minBrightness, int maxBrightness)
		{
			this.red = (short) red;
			this.green = (short) green;
			this.blue = (short) blue;

			this.minSize = minSize;
			this.maxSize = maxSize;

			this.minBrightness = (short) minBrightness;
			this.maxBrightness = (short) maxBrightness;
		}
		
		public short red()
		{
			return red;
		}
		
		public short green()
		{
			return green;
		}
		
		public short blue()
		{
			return blue;
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
		
		public static Type randomSpectralType(long seed)
		{
			Random random = new Random(seed);
			
			if(StellarViewConfig.equal_spectral_types.get())
			{
				Type[] spectralTypes = Type.values();
				return spectralTypes[random.nextInt(0, spectralTypes.length)];
			}
			
			int value = random.nextInt(0, 100);
			
			// Slightly adjusted percentage values that can be found in SpectralType comments
			if(value < 74)
				return M;
			else if(value < (74 + 12))
				return K;
			else if(value < (74 + 12 + 7))
				return G;
			else if(value < (74 + 12 + 7 + 3))
				return F;
			else if(value < (74 + 12 + 7 + 3 + 1))
				return A;
			else if(value < (74 + 12 + 7 + 3 + 1 + 1))
				return B;
			
			return O;
		}
	}
}
