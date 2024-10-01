package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Nebula extends SpaceObject
{
	public static final Codec<Nebula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Nebula::getParentKey),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Nebula::getAxisRotation),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Nebula::getTextureLayers),

			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(Nebula::getFadeOutHandler)
			).apply(instance, Nebula::new));
	
	public Nebula(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, List<TextureLayer> textureLayers,
			FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, textureLayers, fadeOutHandler);
	}
}
