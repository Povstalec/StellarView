package net.povstalec.stellarview.api.common.space_objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import com.mojang.datafixers.util.Either;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class TexturedObject extends SpaceObject
{
	public static final String TEXTURE_LAYERS = "texture_layers";
	public static final String FADE_OUT_HANDLER = "fade_out_handler";
	
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
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		// Serialize Texture Layers
		CompoundTag textureLayerTag = new CompoundTag();
		int i = 0;
		for(TextureLayer textureLayer : textureLayers)
		{
			textureLayerTag.put(String.valueOf(i), textureLayer.serializeNBT());
			i++;
		}
		tag.put(TEXTURE_LAYERS, textureLayerTag);
		
		tag.put(FADE_OUT_HANDLER, fadeOutHandler.serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		// Deserialize Texture Layers
		CompoundTag textureLayerTag = tag.getCompound(TEXTURE_LAYERS);
		for(int i = 0; i < textureLayerTag.size(); i++)
		{
			TextureLayer textureLayer = new TextureLayer();
			textureLayer.deserializeNBT(textureLayerTag.getCompound(String.valueOf(i)));
			textureLayers.add(textureLayer);
		}
		
		this.fadeOutHandler = new FadeOutHandler();
		this.fadeOutHandler.deserializeNBT(tag.getCompound(FADE_OUT_HANDLER));
	}
	
	
	
	public static class FadeOutHandler implements INBTSerializable<CompoundTag>
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
		
		public FadeOutHandler() {}
		
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
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.put(FADE_OUT_START_DISTANCE, fadeOutStartDistance.serializeNBT());
			tag.put(FADE_OUT_END_DISTANCE, fadeOutEndDistance.serializeNBT());
			tag.put(MAX_CHILD_RENDER_DISTANCE, maxChildRenderDistance.serializeNBT());
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			fadeOutStartDistance = new SpaceCoords.SpaceDistance();
			fadeOutStartDistance.deserializeNBT(tag.getCompound(FADE_OUT_START_DISTANCE));
			
			fadeOutEndDistance = new SpaceCoords.SpaceDistance();
			fadeOutEndDistance.deserializeNBT(tag.getCompound(FADE_OUT_END_DISTANCE));
			
			maxChildRenderDistance = new SpaceCoords.SpaceDistance();
			maxChildRenderDistance.deserializeNBT(tag.getCompound(MAX_CHILD_RENDER_DISTANCE));
		}
	}
}
