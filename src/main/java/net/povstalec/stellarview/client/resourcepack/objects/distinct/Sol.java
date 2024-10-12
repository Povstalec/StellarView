package net.povstalec.stellarview.client.resourcepack.objects.distinct;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.objects.Star;
import net.povstalec.stellarview.common.util.*;
import net.povstalec.stellarview.common.util.StellarCoordinates.Equatorial;

import java.util.List;
import java.util.Optional;

public class Sol extends Star
{
	public static final Codec<Sol> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Sol::getParentKey),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Sol::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Sol::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Sol::getTextureLayers),
			
			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(Sol::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(Sol::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(Sol::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(Sol::getMinStarAlpha),
			
			SupernovaInfo.CODEC.optionalFieldOf("supernova_info").forGetter(Sol::getSupernovaInfo)
			).apply(instance, Sol::new));
	
	public Sol(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, Equatorial> coords,
			AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers,
			FadeOutHandler fadeOutHandler, float minStarSize, float maxStarAlpha, float minStarAlpha,
			Optional<SupernovaInfo> supernovaInfo)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha, supernovaInfo);
	}
}
