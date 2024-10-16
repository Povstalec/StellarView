package net.povstalec.stellarview.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class TextureLayer
{
	public static final double MIN_VISUAL_SIZE = 0.05;
	
	private final ResourceLocation texture;
	private final Color.FloatRGBA rgba;
	
	private final boolean blend;
	
	private final double size;
	private final double minSize;
	private final boolean clampAtMinSize;
	
	private final double rotation;
	
	private final UV.Quad uv;
    
    public static final Codec<TextureLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureLayer::texture),
    		Color.FloatRGBA.INT_CODEC.fieldOf("rgba").forGetter(TextureLayer::rgba),
    		
    		Codec.BOOL.fieldOf("blend").forGetter(TextureLayer::shoulBlend),
    		
    		Codec.DOUBLE.fieldOf("size").forGetter(TextureLayer::size),
    		Codec.doubleRange(MIN_VISUAL_SIZE, Double.MAX_VALUE).optionalFieldOf("min_size", MIN_VISUAL_SIZE).forGetter(TextureLayer::minSize),
    		Codec.BOOL.optionalFieldOf("clamp_at_min_size", false).forGetter(TextureLayer::clampAtMinSize),
    		
    		Codec.DOUBLE.fieldOf("rotation").forGetter(TextureLayer::rotation),
    		
    		UV.Quad.CODEC.optionalFieldOf("uv", UV.Quad.DEFAULT_QUAD_UV).forGetter(TextureLayer::uv)
			).apply(instance, TextureLayer::new));
	
	public TextureLayer(ResourceLocation texture, Color.FloatRGBA rgba, boolean blend,
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
	
	public Color.FloatRGBA rgba()
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
	
	public double mulSize(double mulSize)
	{
		return size * mulSize;
	}
	
	public double rotation()
	{
		return rotation;
	}
	
	public double rotation(double addRotation)
	{
		return rotation + addRotation;
	}
	
	public UV.Quad uv()
	{
		return uv;
	}
	
	@Override
	public String toString()
	{
		return texture.toString();
	}
}
