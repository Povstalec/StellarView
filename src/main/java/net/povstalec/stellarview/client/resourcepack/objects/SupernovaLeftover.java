package net.povstalec.stellarview.client.resourcepack.objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class SupernovaLeftover extends GravityLense
{
	public static final Codec<SupernovaLeftover> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(SupernovaLeftover::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(SupernovaLeftover::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(SupernovaLeftover::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(SupernovaLeftover::getTextureLayers),
			
			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_HANDLER).forGetter(SupernovaLeftover::getFadeOutHandler),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_star_size", MIN_SIZE).forGetter(SupernovaLeftover::getMinStarSize),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("max_star_alpha", MAX_ALPHA).forGetter(SupernovaLeftover::getMaxStarAlpha),
			Codec.floatRange(0, Color.MAX_FLOAT_VALUE).optionalFieldOf("min_star_alpha", MIN_ALPHA).forGetter(SupernovaLeftover::getMinStarAlpha),
			
			Codec.floatRange(1F, Float.MAX_VALUE).optionalFieldOf("lensing_intensity", 2F).forGetter(SupernovaLeftover::getLensingIntensity),
			Codec.DOUBLE.optionalFieldOf("max_lensing_distance", 10000000000D).forGetter(SupernovaLeftover::getMaxLensingDistance)
	).apply(instance, SupernovaLeftover::new));
	
	public SupernovaLeftover() {}
	
	public SupernovaLeftover(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
			float minStarSize, float maxStarAlpha, float minStarAlpha,
			float lensingIntensity, double maxLensingDistance)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha, lensingIntensity, maxLensingDistance);
	}
}
