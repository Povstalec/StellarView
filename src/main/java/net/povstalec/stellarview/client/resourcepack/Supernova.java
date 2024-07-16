package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Supernova extends Star
{

	public Supernova(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers);
	}
	
	
	
	public static class SupernovaInfo
	{
		protected ResourceLocation nebulaTexture; //TODO Leave a Nebula where there used to be a Supernova
		protected Supernova leftOver; // Whatever is left after Supernova dies
		
		protected float maxSize;
		protected long start;
		protected long duration;
		
		public SupernovaInfo(ResourceLocation nebulaTexture, float maxSize, long start, long duration)
		{
			this.maxSize = maxSize;
			this.start = start;
			this.duration = duration;
		}
	}
	
	public static abstract class SupernovaLeftover extends Supernova
	{

		public SupernovaLeftover(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords,
				AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
		{
			super(parent, coords, axisRotation, orbitInfo, textureLayers);
		}
		
	}
	
	public static class BlackHole extends SupernovaLeftover
	{

		public BlackHole(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
		{
			super(parent, coords, axisRotation, orbitInfo, textureLayers);
		}
		
	}
}
