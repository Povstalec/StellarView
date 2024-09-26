package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.objects.Star;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates.Equatorial;
import net.povstalec.stellarview.common.util.TextureLayer;

public class DefaultObjects
{
	public static class Sol extends Star
	{
		public Sol(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, Equatorial> coords,
				AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers,
				FadeOutHandler fadeOutHandler, float minStarSize, float maxStarAlpha, float minStarAlpha,
				Optional<SupernovaInfo> supernovaInfo)
		{
			super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha,
					supernovaInfo);
		}
		
		
	}
}
