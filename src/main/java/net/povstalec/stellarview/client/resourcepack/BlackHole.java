package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.client.resourcepack.Supernova.Leftover;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class BlackHole extends Leftover
{
	//TODO Black Holes should visually bend space around them
	public BlackHole(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers);
	}
}
