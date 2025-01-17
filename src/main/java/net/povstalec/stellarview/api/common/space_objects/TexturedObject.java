package net.povstalec.stellarview.api.common.space_objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import com.mojang.datafixers.util.Either;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class TexturedObject extends SpaceObject
{
	public static final String TEXTURE_LAYERS = "texture_layers";
	
	protected ArrayList<TextureLayer> textureLayers;
	protected FadeOutHandler fadeOutHandler;
	
	public TexturedObject()
	{
		textureLayers = new ArrayList<TextureLayer>();
	}
	
	public TexturedObject(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords,
			AxisRotation axisRotation, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation);
		
		this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
		this.fadeOutHandler = fadeOutHandler;
	}
	
	public ArrayList<TextureLayer> getTextureLayers()
	{
		return textureLayers;
	}
	
	public FadeOutHandler getFadeOutHandler()
	{
		return fadeOutHandler;
	}
	
	public double fadeOut(double distance)
	{
		double fadeOutEnd = getFadeOutHandler().getFadeOutEndDistance().toKm();
		if(distance > fadeOutEnd)
			return 0;
		
		double fadeOutStart = getFadeOutHandler().getFadeOutStartDistance().toKm();
		if(distance < fadeOutStart)
			return 1;
		
		return (distance - fadeOutStart) / (fadeOutEnd - fadeOutStart);
	}
	
	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		
		// Deserialize Texture Layers
		CompoundTag textureLayerTag = tag.getCompound(TEXTURE_LAYERS);
		for(int i = 0; i < textureLayerTag.size(); i++)
		{
			textureLayers.add(TextureLayer.fromTag(textureLayerTag.getCompound(String.valueOf(i))));
		}
		
		this.fadeOutHandler = FadeOutHandler.fromTag(tag.getCompound(FADE_OUT_HANDLER));
	}
	
	
	
	public static class FadeOutHandler
	{
		public static final String FADE_OUT_START_DISTANCE = "fade_out_start_distance";
		public static final String FADE_OUT_END_DISTANCE = "fade_out_end_distance";
		public static final String MAX_CHILD_RENDER_DISTANCE = "max_child_render_distance";
		
		public static final FadeOutHandler DEFAULT_PLANET_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(70000000000D), new SpaceCoords.SpaceDistance(100000000000D), new SpaceCoords.SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(5000000L), new SpaceCoords.SpaceDistance(10000000L), new SpaceCoords.SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_NEBULA_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(1000000L), new SpaceCoords.SpaceDistance(2000000L), new SpaceCoords.SpaceDistance(5000000L));
		
		private SpaceCoords.SpaceDistance fadeOutStartDistance;
		private SpaceCoords.SpaceDistance fadeOutEndDistance;
		private SpaceCoords.SpaceDistance maxChildRenderDistance;
		
		public static final Codec<FadeOutHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpaceCoords.SpaceDistance.CODEC.fieldOf(FADE_OUT_START_DISTANCE).forGetter(FadeOutHandler::getFadeOutStartDistance),
				SpaceCoords.SpaceDistance.CODEC.fieldOf(FADE_OUT_END_DISTANCE).forGetter(FadeOutHandler::getFadeOutEndDistance),
				SpaceCoords.SpaceDistance.CODEC.fieldOf(MAX_CHILD_RENDER_DISTANCE).forGetter(FadeOutHandler::getMaxChildRenderDistance)
		).apply(instance, FadeOutHandler::new));
		
		public FadeOutHandler(SpaceCoords.SpaceDistance fadeOutStartDistance, SpaceCoords.SpaceDistance fadeOutEndDistance, SpaceCoords.SpaceDistance maxChildRenderDistance)
		{
			this.fadeOutStartDistance = fadeOutStartDistance;
			this.fadeOutEndDistance = fadeOutEndDistance;
			this.maxChildRenderDistance = maxChildRenderDistance;
		}
		
		public SpaceCoords.SpaceDistance getFadeOutStartDistance()
		{
			return fadeOutStartDistance;
		}
		
		public SpaceCoords.SpaceDistance getFadeOutEndDistance()
		{
			return fadeOutEndDistance;
		}
		
		public SpaceCoords.SpaceDistance getMaxChildRenderDistance()
		{
			return maxChildRenderDistance;
		}
		
		public static FadeOutHandler fromTag(CompoundTag tag)
		{
			return new FadeOutHandler(SpaceCoords.SpaceDistance.fromTag(tag.getCompound(FADE_OUT_START_DISTANCE)), SpaceCoords.SpaceDistance.fromTag(tag.getCompound(FADE_OUT_END_DISTANCE)), SpaceCoords.SpaceDistance.fromTag(tag.getCompound(MAX_CHILD_RENDER_DISTANCE)));
		}
	}
}
