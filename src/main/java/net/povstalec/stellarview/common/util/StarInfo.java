package net.povstalec.stellarview.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.api.common.space_objects.StarLike;

public class StarInfo implements INBTSerializable<CompoundTag>
{
	public static final String LOD1_TYPES = "lod1_types";
	public static final String LOD2_TYPES = "lod2_types";
	public static final String LOD3_TYPES = "lod3_types";
	public static final String LOD1_WEIGHT = "lod1_weight";
	public static final String LOD2_WEIGHT = "lod2_weight";
	public static final String LOD3_WEIGHT = "lod3_weight";
	
	private ArrayList<StarLike.StarType> lod1Types;
	private ArrayList<StarLike.StarType> lod2Types;
	private ArrayList<StarLike.StarType> lod3Types;
	private int lod1Weight = 0;
	private int lod2Weight = 0;
	private int lod3Weight = 0;
	
	public static final StarLike.StarType WHITE_STAR = new StarLike.StarType(new Color.IntRGB(255, 255, 255), 0.15F, 0.25F, (short) 100, (short) 255, 6000000, 1);
	public static final List<StarLike.StarType> DEFAULT_STARS = Arrays.asList(WHITE_STAR);
	public static final StarInfo DEFAULT_STAR_INFO = new StarInfo(DEFAULT_STARS);
	
	public static final Codec<StarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			StarLike.StarType.CODEC.listOf().fieldOf("star_types").forGetter(starInfo -> new ArrayList<StarLike.StarType>())
			).apply(instance, StarInfo::new));
	
	public StarInfo() {}
	
	public StarInfo(List<StarLike.StarType> starTypes)
	{
		for(StarLike.StarType starType : starTypes)
		{
			switch(StarField.LevelOfDetail.fromDistance(starType.getMaxVisibleDistance()))
			{
				case LOD1:
					if(lod1Types == null)
						this.lod1Types = new ArrayList<StarLike.StarType>();
					this.lod1Types.add(starType);
					this.lod1Weight += starType.getWeight();
					break;
				case LOD2:
					if(lod2Types == null)
						this.lod2Types = new ArrayList<StarLike.StarType>();
					this.lod2Types.add(starType);
					this.lod2Weight += starType.getWeight();
					break;
				default:
					if(lod3Types == null)
						this.lod3Types = new ArrayList<StarLike.StarType>();
					this.lod3Types.add(starType);
					this.lod3Weight += starType.getWeight();
					break;
			}
		}
	}
	
	public StarInfo(List<StarLike.StarType> lod1Types, List<StarLike.StarType> lod2Types, List<StarLike.StarType> lod3Types, int lod1Weight, int lod2Weight, int lod3Weight)
	{
		if(lod1Types != null)
			this.lod1Types = new ArrayList<StarLike.StarType>(lod1Types);
		if(lod2Types != null)
			this.lod2Types = new ArrayList<StarLike.StarType>(lod2Types);
		if(lod3Types != null)
			this.lod3Types = new ArrayList<StarLike.StarType>(lod3Types);
		
		this.lod1Weight = lod1Weight;
		this.lod2Weight = lod2Weight;
		this.lod3Weight = lod3Weight;
	}
	
	private StarLike.StarType randomStarType(ArrayList<StarLike.StarType> lodTypes, int totalWeight, Random random)
	{
		if(lodTypes == null || lodTypes.isEmpty())
			return WHITE_STAR;
		
		int i = 0;
		for(int weight = random.nextInt(0, totalWeight); i < lodTypes.size() - 1; i++)
		{
			weight -= lodTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return lodTypes.get(i);
	}
	
	public StarLike.StarType randomLOD1StarType(Random random)
	{
		return randomStarType(lod1Types, lod1Weight, random);
	}
	
	public StarLike.StarType randomLOD2StarType(Random random)
	{
		return randomStarType(lod2Types, lod2Weight, random);
	}
	
	public StarLike.StarType randomLOD3StarType(Random random)
	{
		return randomStarType(lod3Types, lod3Weight, random);
	}
	
	public int totalWeight()
	{
		return lod1Weight + lod2Weight + lod3Weight;
	}
	
	public int lod1Weight()
	{
		return lod1Weight;
	}
	
	public int lod2Weight()
	{
		return lod2Weight;
	}
	
	public int lod3Weight()
	{
		return lod3Weight;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	private static CompoundTag serializeLODTypes(ArrayList<StarLike.StarType> lodTypes)
	{
		CompoundTag starTypesTag = new CompoundTag();
		for(int i = 0; i < lodTypes.size(); i++)
		{
			starTypesTag.put("star_type_" + i, lodTypes.get(i).serializeNBT());
		}
		
		return starTypesTag;
	}
	
	private static ArrayList<StarLike.StarType> getLODTypes(CompoundTag tag, String key)
	{
		ArrayList<StarLike.StarType> lodTypes;
		if(tag.contains(key))
		{
			lodTypes = new ArrayList<StarLike.StarType>();
			CompoundTag starTypesTag = tag.getCompound(key);
			for(int i = 0; i < starTypesTag.size(); i++)
			{
				StarLike.StarType starType = new StarLike.StarType();
				starType.deserializeNBT(starTypesTag.getCompound("star_type_" + i));
				lodTypes.add(starType);
			}
		}
		else
			lodTypes = null;
		
		return lodTypes;
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		if(this.lod1Types != null)
			tag.put(LOD1_TYPES, serializeLODTypes(this.lod1Types));
		if(this.lod2Types != null)
			tag.put(LOD2_TYPES, serializeLODTypes(this.lod2Types));
		if(this.lod3Types != null)
			tag.put(LOD3_TYPES, serializeLODTypes(this.lod3Types));
		
		tag.putInt(LOD1_WEIGHT, lod1Weight);
		tag.putInt(LOD2_WEIGHT, lod2Weight);
		tag.putInt(LOD3_WEIGHT, lod3Weight);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		this.lod1Types = getLODTypes(tag, LOD1_TYPES);
		this.lod2Types = getLODTypes(tag, LOD2_TYPES);
		this.lod3Types = getLODTypes(tag, LOD3_TYPES);
		
		this.lod1Weight = tag.getInt(LOD1_WEIGHT);
		this.lod2Weight = tag.getInt(LOD2_WEIGHT);
		this.lod3Weight = tag.getInt(LOD3_WEIGHT);
	}
}
