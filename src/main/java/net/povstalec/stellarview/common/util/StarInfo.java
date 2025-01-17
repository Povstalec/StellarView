package net.povstalec.stellarview.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.api.common.space_objects.StarLike;

public class StarInfo
{
	public static final ResourceLocation DEFAULT_STAR_TEXTURE = new ResourceLocation(StellarView.MODID,"textures/environment/star.png");
	
	public static final String STAR_TEXTURE = "star_texture";
	public static final String LOD1_TYPES = "lod1_types";
	public static final String LOD2_TYPES = "lod2_types";
	public static final String LOD3_TYPES = "lod3_types";
	public static final String LOD1_WEIGHT = "lod1_weight";
	public static final String LOD2_WEIGHT = "lod2_weight";
	public static final String LOD3_WEIGHT = "lod3_weight";
	
	protected ResourceLocation starTexture;
	
	private ArrayList<StarLike.StarType> lod1Types;
	private ArrayList<StarLike.StarType> lod2Types;
	private ArrayList<StarLike.StarType> lod3Types;
	private int lod1Weight = 0;
	private int lod2Weight = 0;
	private int lod3Weight = 0;
	
	public static final StarLike.StarType WHITE_STAR = new StarLike.StarType(new Color.IntRGB(255, 255, 255), 0.15F, 0.25F, (short) 100, (short) 255, 6000000, 1);
	public static final List<StarLike.StarType> DEFAULT_STARS = Arrays.asList(WHITE_STAR);
	public static final StarInfo DEFAULT_STAR_INFO = new StarInfo(DEFAULT_STAR_TEXTURE, DEFAULT_STARS);
	
	public static final Codec<StarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("star_texture", DEFAULT_STAR_TEXTURE).forGetter(StarInfo::getStarTexture),
			StarLike.StarType.CODEC.listOf().fieldOf("star_types").forGetter(starInfo -> new ArrayList<StarLike.StarType>())
			).apply(instance, StarInfo::new));
	
	public StarInfo(ResourceLocation starTexture, List<StarLike.StarType> starTypes)
	{
		this.starTexture = starTexture;
		
		for(StarLike.StarType starType : starTypes)
		{
			switch(StarField.LevelOfDetail.fromDistance(starType.getMaxVisibleDistance()))
			{
				case LOD1:
					if(lod1Types == null)
						this.lod1Types = new ArrayList<StarLike.StarType>(starTypes);
					this.lod1Types.add(starType);
					this.lod1Weight += starType.getWeight();
					break;
				case LOD2:
					if(lod2Types == null)
						this.lod2Types = new ArrayList<StarLike.StarType>(starTypes);
					this.lod2Types.add(starType);
					this.lod2Weight += starType.getWeight();
					break;
				default:
					if(lod3Types == null)
						this.lod3Types = new ArrayList<StarLike.StarType>(starTypes);
					this.lod3Types.add(starType);
					this.lod3Weight += starType.getWeight();
					break;
			}
		}
	}
	
	public StarInfo(ResourceLocation starTexture, List<StarLike.StarType> lod1Types, List<StarLike.StarType> lod2Types, List<StarLike.StarType> lod3Types, int lod1Weight, int lod2Weight, int lod3Weight)
	{
		this.starTexture = starTexture;
		
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
	
	public ResourceLocation getStarTexture()
	{
		return starTexture;
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
	
	private static ArrayList<StarLike.StarType> getLODTypes(CompoundTag tag, String key)
	{
		ArrayList<StarLike.StarType> lodTypes;
		if(tag.contains(key))
		{
			lodTypes = new ArrayList<StarLike.StarType>();
			CompoundTag starTypesTag = tag.getCompound(key);
			for(int i = 0; i < starTypesTag.size(); i++)
			{
				StarLike.StarType starType = StarLike.StarType.fromTag(starTypesTag.getCompound("star_type_" + i));
				lodTypes.add(starType);
			}
		}
		else
			lodTypes = null;
		
		return lodTypes;
	}
	
	public static StarInfo fromTag(CompoundTag tag)
	{
		return new StarInfo(new ResourceLocation(tag.getString(STAR_TEXTURE)), getLODTypes(tag, LOD1_TYPES), getLODTypes(tag, LOD2_TYPES), getLODTypes(tag, LOD3_TYPES), tag.getInt(LOD1_WEIGHT), tag.getInt(LOD2_WEIGHT), tag.getInt(LOD3_WEIGHT));
	}
}
