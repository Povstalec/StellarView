package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Constellation extends SpaceObject
{
	public static final String STAR_TEXTURE = "star_texture";
	public static final String STARS = "stars";
	
	public static final String LOD1_STARS = "lod1_stars";
	public static final String LOD2_STARS = "lod2_stars";
	public static final String LOD3_STARS = "lod3_stars";
	
	private ArrayList<StarDefinition> lod1stars = new ArrayList<>();
	private ArrayList<StarDefinition> lod2stars = new ArrayList<>();
	private ArrayList<StarDefinition> lod3stars = new ArrayList<>();
	
	@Nullable
	protected ResourceLocation starTexture;
	
	public static final Codec<Constellation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ParentInfo.CODEC.optionalFieldOf(PARENT).forGetter(Constellation::getParentInfo),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(constellation -> Either.left(constellation.getCoords())),
			AxisRotation.CODEC.fieldOf(AXIS_ROTATION).forGetter(Constellation::getAxisRotation),
			
			ResourceLocation.CODEC.optionalFieldOf(STAR_TEXTURE).forGetter(constellation -> Optional.of(constellation.starTexture)),
			StarDefinition.CODEC.listOf().fieldOf(STARS).forGetter(constellation -> new ArrayList<>())
	).apply(instance, Constellation::new));
	
	public Constellation() {}
	
	public Constellation(Optional<ParentInfo> parentLocation, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<ResourceLocation> starTexture, List<StarDefinition> stars)
	{
		super(parentLocation, coords, axisRotation);
		
		this.starTexture = starTexture.orElse(null);
		
		for(StarDefinition star : stars)
		{
			switch(StarField.LevelOfDetail.fromDistance(star.maxVisibleDistance()))
			{
				case LOD1:
					this.lod1stars.add(star);
					break;
				case LOD2:
					this.lod2stars.add(star);
					break;
				default:
					this.lod3stars.add(star);
					break;
			}
		}
	}
	
	// Make stars relative to the constellation center coords
	public void relativeStars()
	{
		for(StarDefinition star : this.lod1stars)
		{
			star.offsetCoords(this.coords);
		}
		
		for(StarDefinition star : this.lod2stars)
		{
			star.offsetCoords(this.coords);
		}
		
		for(StarDefinition star : this.lod3stars)
		{
			star.offsetCoords(this.coords);
		}
	}
	
	@Nullable
	public ResourceLocation getStarTexture()
	{
		return starTexture;
	}
	
	public ArrayList<StarDefinition> lod1stars()
	{
		return lod1stars;
	}
	
	public ArrayList<StarDefinition> lod2stars()
	{
		return lod2stars;
	}
	
	public ArrayList<StarDefinition> lod3stars()
	{
		return lod3stars;
	}
	
	public boolean hasStarField()
	{
		return parent instanceof StarField;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	private static CompoundTag serializeLODTypes(ArrayList<StarDefinition> lodTypes)
	{
		CompoundTag starTypesTag = new CompoundTag();
		for(int i = 0; i < lodTypes.size(); i++)
		{
			starTypesTag.put("star_definition_" + i, lodTypes.get(i).serializeNBT());
		}
		
		return starTypesTag;
	}
	
	private static ArrayList<StarDefinition> getLODTypes(CompoundTag tag, String key)
	{
		ArrayList<StarDefinition> lodTypes;
		if(tag.contains(key))
		{
			lodTypes = new ArrayList<StarDefinition>();
			CompoundTag starTypesTag = tag.getCompound(key);
			for(int i = 0; i < starTypesTag.size(); i++)
			{
				StarDefinition starDefinition = new StarDefinition();
				starDefinition.deserializeNBT(starTypesTag.getCompound("star_definition_" + i));
				lodTypes.add(starDefinition);
			}
		}
		else
			lodTypes = null;
		
		return lodTypes;
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		if(this.lod1stars != null)
			tag.put(LOD1_STARS, serializeLODTypes(this.lod1stars));
		if(this.lod2stars != null)
			tag.put(LOD2_STARS, serializeLODTypes(this.lod2stars));
		if(this.lod3stars != null)
			tag.put(LOD3_STARS, serializeLODTypes(this.lod3stars));
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		this.lod1stars = getLODTypes(tag, LOD1_STARS);
		this.lod2stars = getLODTypes(tag, LOD2_STARS);
		this.lod3stars = getLODTypes(tag, LOD3_STARS);
	}
	
	
	
	public static class StarDefinition implements ISerializable
	{
		public static final String RGB = "rgb";
		public static final String SIZE = "size";
		public static final String ROTATION = "rotation";
		public static final String BRIGHTNESS = "brightness";
		public static final String MAX_VISIBLE_DISTANCE = "max_visible_distance";
		
		private SpaceCoords coords;
		private Color.IntRGB rgb;
		private short brightness;
		private float size;
		private double rotation;
		private long maxVisibleDistance;
		
		public static final Codec<StarDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(starDefinition -> Either.left(starDefinition.coords)),
				Color.IntRGB.CODEC.fieldOf(RGB).forGetter(starDefinition -> starDefinition.rgb),
				Codec.SHORT.fieldOf(BRIGHTNESS).forGetter(starDefinition -> starDefinition.brightness),
				Codec.FLOAT.fieldOf(SIZE).forGetter(starDefinition -> starDefinition.size),
				Codec.DOUBLE.fieldOf(ROTATION).forGetter(starDefinition -> starDefinition.rotation),
				Codec.LONG.optionalFieldOf(MAX_VISIBLE_DISTANCE, Long.MAX_VALUE).forGetter(starDefinition -> starDefinition.maxVisibleDistance)
		).apply(instance, StarDefinition::new));
		
		public StarDefinition() {}
		
		public StarDefinition(Either<SpaceCoords, StellarCoordinates.Equatorial> coords, Color.IntRGB rgb, short brightness, float size, double rotation, long maxVisibleDistance)
		{
			if(coords.left().isPresent())
				this.coords = coords.left().get();
			else
				this.coords = coords.right().get().toGalactic().toSpaceCoords();
			
			this.rgb = rgb;
			this.brightness = brightness;
			this.size = size;
			this.rotation = Math.toRadians(rotation);
			this.maxVisibleDistance = maxVisibleDistance;
		}
		
		public void offsetCoords(SpaceCoords offset)
		{
			coords = coords.add(offset);
		}
		
		public SpaceCoords coords()
		{
			return coords;
		}
		
		public Color.IntRGB rgb()
		{
			return rgb;
		}
		
		public short brightness()
		{
			return brightness;
		}
		
		public float size()
		{
			return size;
		}
		
		public double rotation()
		{
			return rotation;
		}
		
		public long maxVisibleDistance()
		{
			return maxVisibleDistance;
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.put(COORDS, coords.serializeNBT());
			
			tag.put(RGB, rgb.serializeNBT());
			
			tag.putShort(BRIGHTNESS, brightness);
			
			tag.putFloat(SIZE, size);
			
			tag.putDouble(ROTATION, rotation);
			
			tag.putLong(MAX_VISIBLE_DISTANCE, maxVisibleDistance);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			this.coords = new SpaceCoords();
			coords.deserializeNBT(tag.getCompound(COORDS));
			
			this.rgb = new  Color.IntRGB();
			this.rgb.deserializeNBT(tag.getCompound(RGB));
			
			this.brightness = tag.getShort(BRIGHTNESS);
			
			this.size = tag.getFloat(SIZE);
			
			this.rotation = tag.getDouble(ROTATION);
			
			this.maxVisibleDistance = tag.getLong(MAX_VISIBLE_DISTANCE);
		}
	}
}
