package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.api.common.space_objects.StarLike;
import net.povstalec.stellarview.api.common.space_objects.SupernovaLeftover;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class BlackHole extends SupernovaLeftover
{
	public static final Codec<BlackHole> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ParentInfo.CODEC.optionalFieldOf("parent").forGetter(BlackHole::getParentInfo),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(BlackHole::getAxisRotation),
			OrbitingObject.OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(blackHole -> Optional.ofNullable(blackHole.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(BlackHole::getTextureLayers),
			
			TexturedObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", TexturedObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(BlackHole::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_black_hole_size", StarLike.MIN_SIZE).forGetter(BlackHole::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_black_hole_alpha", StarLike.MAX_ALPHA).forGetter(BlackHole::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_black_hole_alpha", StarLike.MIN_ALPHA).forGetter(BlackHole::getMinStarAlpha),
			
			Codec.floatRange(1F, Float.MAX_VALUE).optionalFieldOf(LENSING_INTENSITY, 8F).forGetter(BlackHole::getLensingIntensity),
			Codec.DOUBLE.optionalFieldOf(MAX_LENSING_DISTANCE, 10000000000D).forGetter(BlackHole::getMaxLensingDistance)
			).apply(instance, BlackHole::new));
	
	public BlackHole() {}
	
	public BlackHole(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					 Optional<OrbitingObject.OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, TexturedObject.FadeOutHandler fadeOutHandler,
					 float minStarSize, float maxStarAlpha, float minStarAlpha,
					 float lensingIntensity, double maxLensingDistance)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha, lensingIntensity, maxLensingDistance);
	}
}
