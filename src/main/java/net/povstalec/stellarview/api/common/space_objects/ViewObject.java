package net.povstalec.stellarview.api.common.space_objects;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

import java.util.List;
import java.util.Optional;

public class ViewObject extends OrbitingObject
{
	public ViewObject() {}
	
	public ViewObject(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo,
					  List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);
	}
}
