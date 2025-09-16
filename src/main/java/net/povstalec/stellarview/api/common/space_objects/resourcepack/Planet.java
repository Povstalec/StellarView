package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.ViewObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import java.util.List;
import java.util.Optional;

public class Planet extends ViewObject
{
	public static final Codec<Planet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ParentInfo.CODEC.optionalFieldOf("parent").forGetter(Planet::getParentInfo),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Planet::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(planet -> Optional.ofNullable(planet.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Planet::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(Planet::getFadeOutHandler)
			).apply(instance, Planet::new));
	
	public Planet() {}
	
	public Planet(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);
	}
}
