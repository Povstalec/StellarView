package net.povstalec.stellarview.api.common.space_objects.distinct;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Moon;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import java.util.List;
import java.util.Optional;

public class Luna extends Moon
{
	public static final Codec<Luna> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ParentInfo.CODEC.optionalFieldOf("parent").forGetter(Luna::getParentInfo),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Luna::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(moon -> Optional.ofNullable(moon.orbitInfo())),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Luna::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(Luna::getFadeOutHandler),
			
			Compatibility.CODEC.optionalFieldOf("compatibility").forGetter(Luna::getCompatibility)
	).apply(instance, Luna::new));
	
	public Luna() {}
	
	public Luna(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
				Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
				Optional<Compatibility> compatibility)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, compatibility);
	}
	
	
}
