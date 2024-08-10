package net.povstalec.stellarview.common.util;

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

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

public class TextureLayer
{
	public static final double MIN_VISUAL_SIZE = 0.05;
	
	private final ResourceLocation texture;
	private final Color.IntRGBA rgba;
	
	private final boolean blend;
	
	private final double size;
	private final double minSize;
	private final boolean clampAtMinSize;
	
	private final double rotation;
	
	private final UV.Quad uv;
    
    public static final Codec<TextureLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureLayer::texture),
    		Color.IntRGBA.CODEC.fieldOf("rgba").forGetter(TextureLayer::rgba),
    		
    		Codec.BOOL.fieldOf("blend").forGetter(TextureLayer::shoulBlend),
    		
    		Codec.DOUBLE.fieldOf("size").forGetter(TextureLayer::size),
    		Codec.doubleRange(MIN_VISUAL_SIZE, Double.MAX_VALUE).optionalFieldOf("min_size", MIN_VISUAL_SIZE).forGetter(TextureLayer::minSize),
    		Codec.BOOL.optionalFieldOf("clamp_at_min_size", false).forGetter(TextureLayer::clampAtMinSize),
    		
    		Codec.DOUBLE.fieldOf("rotation").forGetter(TextureLayer::rotation),
    		
    		UV.Quad.CODEC.optionalFieldOf("uv", UV.Quad.DEFAULT_QUAD_UV).forGetter(TextureLayer::uv)
			).apply(instance, TextureLayer::new));
	
	public TextureLayer(ResourceLocation texture, Color.IntRGBA rgba, boolean blend,
		double size, double minSize, boolean clampAtMinSize,
		double rotation, UV.Quad uv)
	{
		this.texture = texture;
		this.rgba = rgba;
		
		this.blend = blend;
		
		this.size = size;
		this.minSize = minSize;
		this.clampAtMinSize = clampAtMinSize;
		
		this.rotation = Math.toRadians(rotation);
		this.uv = uv;
	}
	
	public ResourceLocation texture()
	{
		return texture;
	}
	
	public Color.IntRGBA rgba()
	{
		return rgba;
	}
	
	public boolean shoulBlend()
	{
		return blend;
	}
	
	public double size()
	{
		return size;
	}
	
	public double minSize()
	{
		return minSize;
	}
	
	public boolean clampAtMinSize()
	{
		return clampAtMinSize;
	}
	
	private float mulSize(double mulSize)
	{
		return (float) (size * mulSize);
	}
	
	public double rotation()
	{
		return rotation;
	}
	
	private float rotation(long ticks, double addRotation)
	{
		return (float) (rotation + addRotation);
	}
	
	public UV.Quad uv()
	{
		return uv;
	}
	
	public void render(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, Color.FloatRGBA rgba, long ticks, double distanceSize, double sizeMultiplier, double addRotation)
	{
		if(rgba.alpha() <= 0.0F || this.rgba().alpha() <= 0)
			return;
		
		float size = this.mulSize(distanceSize);
		
		if(size < minSize)
		{
			if(clampAtMinSize)
				size = (float) minSize;
			else
				return;
		}
		
		size *= sizeMultiplier;
		
		float rotation = this.rotation(ticks, addRotation);
		
		Vector3f corner00 = StellarCoordinates.placeOnSphere(-size, -size, sphericalCoords, rotation);
		Vector3f corner10 = StellarCoordinates.placeOnSphere(size, -size, sphericalCoords, rotation);
		Vector3f corner11 = StellarCoordinates.placeOnSphere(size, size, sphericalCoords, rotation);
		Vector3f corner01 = StellarCoordinates.placeOnSphere(-size, size, sphericalCoords, rotation);
	
	
		if(this.shoulBlend())
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		RenderSystem.setShaderColor(rgba.red() * this.rgba().red() / 255F, rgba.green() * this.rgba().green() / 255F, rgba.blue() * this.rgba().blue() / 255F, rgba.alpha() * this.rgba().alpha() / 255F);
		
		RenderSystem.setShaderTexture(0, this.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(this.uv().topRight().u(ticks), this.uv().topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(this.uv().bottomRight().u(ticks), this.uv().bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(this.uv().bottomLeft().u(ticks), this.uv().bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(this.uv().topLeft().u(ticks), this.uv().topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
        RenderSystem.defaultBlendFunc();
	}
	
	public void render(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, Color.FloatRGBA rgba, long ticks)
	{
		this.render(bufferbuilder, lastMatrix, sphericalCoords, rgba, ticks, 1, 1, 0);
	}
	
	@Override
	public String toString()
	{
		return texture.toString();
	}
}
