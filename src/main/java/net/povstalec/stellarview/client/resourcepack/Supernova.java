package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Supernova extends Star
{
	private SupernovaInfo supernovaInfo;
	
	public static final Codec<Supernova> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Supernova::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(Supernova::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Supernova::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Supernova::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Supernova::getTextureLayers),
			SupernovaInfo.CODEC.fieldOf("supernova_info").forGetter(Supernova::getSupernovaInfo)
			).apply(instance, Supernova::new));
	
	public Supernova(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, SupernovaInfo supernovaInfo)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers);
		
		this.supernovaInfo = supernovaInfo;
	}
	
	public SupernovaInfo getSupernovaInfo()
	{
		return supernovaInfo;
	}
	
	public boolean supernovaStarted(long ticks)
	{
		return ticks > supernovaInfo.getStartTicks();
	}
	
	public boolean supernovaEnded(long ticks)
	{
		return ticks > supernovaInfo.getEndTicks();
	}
	
	public long lifetime(long ticks)
	{
		return ticks - supernovaInfo.getStartTicks();
	}
	
	@Override
	public float sizeMultiplier(ClientLevel level, Camera camera, long ticks, float partialTicks)
	{
		if(!supernovaStarted(ticks))
			return 1;

		if(supernovaEnded(ticks))
			return 0;
		
		long lifetime = lifetime(ticks);
		float sizeMultiplier = supernovaInfo.getMaxSizeMultiplier() * (float) Math.sin(Math.PI * lifetime / supernovaInfo.getDurationTicks());
		
		return sizeMultiplier > 1 || (float) lifetime > supernovaInfo.getDurationTicks() / 2 ? sizeMultiplier : 1;
	}
	
	@Override
	public float rotation(ClientLevel level, Camera camera, long ticks, float partialTicks)
	{
		if(!supernovaStarted(ticks))
			return 0;
		
		return (float) (Math.PI * lifetime(ticks) / supernovaInfo.getDurationTicks());
	}
	
	public static class SupernovaInfo
	{
		protected Nebula nebula; //TODO Leave a Nebula where there used to be a Supernova
		protected Leftover supernovaLeftover; // Whatever is left after Supernova dies
		
		protected float maxSizeMultiplier;
		protected long startTicks;
		protected long durationTicks;
		
		protected long endTicks;
		
		public static final Codec<SupernovaInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Nebula.CODEC.fieldOf("nebula").forGetter(SupernovaInfo::getNebula),
				Leftover.CODEC.fieldOf("supernova_leftover").forGetter(SupernovaInfo::getSupernovaLeftover),
				Codec.FLOAT.fieldOf("max_size_multiplier").forGetter(SupernovaInfo::getMaxSizeMultiplier),
				Codec.LONG.fieldOf("start_ticks").forGetter(SupernovaInfo::getStartTicks),
				Codec.LONG.fieldOf("duration_ticks").forGetter(SupernovaInfo::getDurationTicks)
				).apply(instance, SupernovaInfo::new));
		
		public SupernovaInfo(Nebula nebula, Leftover supernovaLeftover, float maxSizeMultiplier, long startTicks, long durationTicks)
		{
			this.nebula = nebula;
			this.supernovaLeftover = supernovaLeftover;
			
			this.maxSizeMultiplier = maxSizeMultiplier;
			this.startTicks = startTicks;
			this.durationTicks = durationTicks;
			
			this.endTicks = startTicks + durationTicks;
		}
		
		public Nebula getNebula()
		{
			return nebula;
		}
		
		public Leftover getSupernovaLeftover()
		{
			return supernovaLeftover;
		}
		
		public float getMaxSizeMultiplier()
		{
			return maxSizeMultiplier;
		}
		
		public long getStartTicks()
		{
			return startTicks;
		}
		
		public long getDurationTicks()
		{
			return durationTicks;
		}
		
		public long getEndTicks()
		{
			return endTicks;
		}
	}
	
	// This mainly exists to make sure that whatever is left after a Supernova can't be a Supernova itself
	public static class Leftover extends Star
	{
		public static final Codec<Leftover> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Star::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(Star::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Star::getAxisRotation),
				OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Star::getOrbitInfo),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Star::getTextureLayers)
				).apply(instance, Leftover::new));
		
		public Leftover(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords,
				AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
		{
			super(parent, coords, axisRotation, orbitInfo, textureLayers);
		}
		
	}
}
