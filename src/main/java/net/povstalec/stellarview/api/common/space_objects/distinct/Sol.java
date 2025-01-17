package net.povstalec.stellarview.api.common.space_objects.distinct;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.Star;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.StellarCoordinates.Equatorial;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Sol extends Star
{
	public static final Codec<Sol> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Sol::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Sol::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Sol::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Sol::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(Sol::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(Sol::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(Sol::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(Sol::getMinStarAlpha),
			
			SupernovaInfo.CODEC.optionalFieldOf("supernova_info").forGetter(Sol::getSupernovaInfo)
			).apply(instance, Sol::new));
	
	public Sol(Optional<ResourceLocation> parent, Either<SpaceCoords, Equatorial> coords,
			AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers,
			FadeOutHandler fadeOutHandler, float minStarSize, float maxStarAlpha, float minStarAlpha,
			Optional<SupernovaInfo> supernovaInfo)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha, supernovaInfo);
	}
}
