package net.povstalec.stellarview.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class TextureLayer
{
	public static final String TEXTURE = "texture";
	public static final String RGBA = "rgba";
	
	public static final String BLEND = "blend";
	
	public static final String SIZE = "size";
	public static final String MIN_SIZE = "min_size";
	public static final String CLAMP_AT_MIN_SIZE = "clamp_at_min_size";
	
	public static final String ROTATION = "rotation";
	
	public static final String UV_QUAD = "uv";
	
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
			ResourceLocation.CODEC.fieldOf(TEXTURE).forGetter(TextureLayer::texture),
			Color.FloatRGBA.INT_CODEC.fieldOf(RGBA).forGetter(TextureLayer::rgba),
			
			Codec.BOOL.fieldOf(BLEND).forGetter(TextureLayer::shoulBlend),
			
			Codec.DOUBLE.fieldOf(SIZE).forGetter(TextureLayer::size),
			Codec.doubleRange(MIN_VISUAL_SIZE, Double.MAX_VALUE).optionalFieldOf(MIN_SIZE, MIN_VISUAL_SIZE).forGetter(TextureLayer::minSize),
			Codec.BOOL.optionalFieldOf(CLAMP_AT_MIN_SIZE, false).forGetter(TextureLayer::clampAtMinSize),
			
			Codec.DOUBLE.fieldOf(ROTATION).forGetter(TextureLayer::rotation),
			
			UV.Quad.CODEC.optionalFieldOf(UV_QUAD, UV.Quad.DEFAULT_QUAD_UV).forGetter(TextureLayer::uv)
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
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	public CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putString(TEXTURE, texture.toString());
		
		tag.put(RGBA, rgba.serializeNBT());
		
		tag.putBoolean(BLEND, blend);
		
		tag.putDouble(SIZE, size);
		tag.putDouble(MIN_SIZE, minSize);
		tag.putBoolean(CLAMP_AT_MIN_SIZE, clampAtMinSize);
		
		tag.putDouble(ROTATION, rotation);
		
		tag.put(UV_QUAD, uv.serialize());
		
		return tag;
	}
	
	public static TextureLayer deserialize(CompoundTag tag)
	{
		ResourceLocation texture = new ResourceLocation(tag.getString(TEXTURE));
		
		Color.FloatRGBA rgba = new Color.FloatRGBA(0, 0, 0);
		rgba.deserializeNBT(tag.getCompound(RGBA));
		
		boolean blend = tag.getBoolean(BLEND);
		
		double size = tag.getDouble(SIZE);
		double minSize = tag.getDouble(MIN_SIZE);
		boolean clampAtMinSize = tag.getBoolean(CLAMP_AT_MIN_SIZE);
		
		double rotation = tag.getDouble(ROTATION);
		
		UV.Quad uv = UV.Quad.deserialize(tag.getCompound(UV_QUAD));
		
		
		return new TextureLayer(texture, rgba, blend, size, minSize, clampAtMinSize, rotation, uv);
	}
}