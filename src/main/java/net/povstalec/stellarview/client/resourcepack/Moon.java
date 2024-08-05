package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

/**
 * A subtype of planet that should be compatible with enhanced celestials
 */
public class Moon extends Planet
{

	public Moon(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers);
	}

}
