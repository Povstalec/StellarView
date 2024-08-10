package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;

/**
 * A subtype of planet that should be compatible with enhanced celestials
 */
public class Moon extends Planet
{
	public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Moon::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(Moon::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Moon::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Moon::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Moon::getTextureLayers)
			).apply(instance, Moon::new));
	
	public Moon(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers);
	}
	
	@Override
	public float sizeMultiplier(ClientLevel level, Camera camera, long ticks, float partialTicks)
	{
		return EnhancedCelestialsCompatibility.getMoonSize(partialTicks) / 20F;
	}
	
	@Override
	public Color.FloatRGBA rgba(ClientLevel level, Camera camera, long ticks, float partialTicks)
	{
		return EnhancedCelestialsCompatibility.getMoonColor(level, partialTicks);
	}
}
