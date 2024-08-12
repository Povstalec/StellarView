package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Nebula extends SpaceObject
{
	public static final Codec<Nebula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Nebula::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(Nebula::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Nebula::getAxisRotation),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Nebula::getTextureLayers),

			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(Nebula::getFadeOutHandler)
			).apply(instance, Nebula::new));
	
	public Nebula(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation, List<TextureLayer> textureLayers,
			FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, textureLayers, fadeOutHandler);
	}
}
