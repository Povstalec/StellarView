package net.povstalec.stellarview.client.resourcepack.objects;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import java.util.List;
import java.util.Optional;

public class ViewCenterObject extends OrbitingObject
{
	public ViewCenterObject(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo,
						  List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);
	}
}
