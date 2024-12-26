package net.povstalec.stellarview.client.resourcepack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.objects.StarLike;
import net.povstalec.stellarview.common.util.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DustCloudInfo
{
	public static final String DUST_CLOUD_TYPES = "dust_cloud_types";
	public static final String TOTAL_WEIGHT = "total_weight";
	
	private ArrayList<DustCloudType> dustCloudTypes;
	private int totalWeight = 0;
	
	public static final DustCloudType WHITE_DUST_CLOUD = new DustCloudType(new Color.IntRGB(107, 107, 107), 2.0F, 7.0F, (short) 255, (short) 255, 1);
	public static final List<DustCloudType> DEFAULT_DUST_CLOUDS = Arrays.asList(WHITE_DUST_CLOUD);
	public static final DustCloudInfo DEFAULT_DUST_CLOUD_INFO = new DustCloudInfo(DEFAULT_DUST_CLOUDS);
	
	public static final Codec<DustCloudInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			DustCloudType.CODEC.listOf().fieldOf("dust_cloud_types").forGetter(dustCloudInfo -> dustCloudInfo.dustCloudTypes)
			).apply(instance, DustCloudInfo::new));
	
	public DustCloudInfo(List<DustCloudType> dustCloudTypes, int totalWeight)
	{
		this.dustCloudTypes = new ArrayList<DustCloudType>(dustCloudTypes);
		this.totalWeight = totalWeight;
	}
	
	public DustCloudInfo(List<DustCloudType> dustCloudTypes)
	{
		this.dustCloudTypes = new ArrayList<DustCloudType>(dustCloudTypes);
		
		for(DustCloudType dustCloudType : dustCloudTypes)
		{
			this.totalWeight += dustCloudType.getWeight();
		}
	}
	
	public DustCloudType getRandomDustCloudType(long seed)
	{
		if(dustCloudTypes.isEmpty())
			return WHITE_DUST_CLOUD;
		
		Random random = new Random(seed);
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalWeight); i < dustCloudTypes.size() - 1; i++)
		{
			weight -= dustCloudTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return dustCloudTypes.get(i);
	}
	
	public static DustCloudInfo fromTag(CompoundTag tag)
	{
		ArrayList<DustCloudType> dustCloudTypes = new ArrayList<DustCloudType>();
		CompoundTag dustCloudTypesTag = tag.getCompound(DUST_CLOUD_TYPES);
		for(int i = 0; i < dustCloudTypesTag.size(); i++)
		{
			DustCloudType dustCloudType = DustCloudType.fromTag(dustCloudTypesTag.getCompound("dust_cloud_type_" + i));
			dustCloudTypes.add(dustCloudType);
		}
		
		return new DustCloudInfo(dustCloudTypes, tag.getInt(TOTAL_WEIGHT));
	}
	
	
	
	public static class DustCloudType
	{
		public static final String RGB = "rgb";
		public static final String MAX_SIZE = "max_size";
		public static final String MIN_SIZE = "min_size";
		public static final String MAX_BRIGHTNESS = "max_brightness";
		public static final String MIN_BRIGHTNESS = "min_brightness";
		public static final String WEIGHT = "weight";
		
		private final Color.IntRGB rgb;
		
		private final float minSize;
		private final float maxSize;
		
		private final short minBrightness;
		private final short maxBrightness;
		
		public final int weight;
		
		public static final Codec<DustCloudType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Color.IntRGB.CODEC.fieldOf("rgb").forGetter(DustCloudType::getRGB),
				
				Codec.FLOAT.fieldOf("min_size").forGetter(dustCloudType -> dustCloudType.minSize),
				Codec.FLOAT.fieldOf("max_size").forGetter(dustCloudType -> dustCloudType.maxSize),
				
				Codec.SHORT.fieldOf("min_brightness").forGetter(dustCloudType -> dustCloudType.minBrightness),
				Codec.SHORT.fieldOf("max_brightness").forGetter(dustCloudType -> dustCloudType.maxBrightness),
				
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(DustCloudType::getWeight)
		).apply(instance, DustCloudType::new));
		
		public DustCloudType(Color.IntRGB rgb, float minSize, float maxSize, short minBrightness, short maxBrightness, int weight)
		{
			this.rgb = rgb;
			
			this.minSize = minSize;
			this.maxSize = maxSize;
			
			this.minBrightness = minBrightness;
			this.maxBrightness = (short) (maxBrightness + 1);
			
			this.weight = weight;
		}
		
		public Color.IntRGB getRGB() // TODO Maybe random RGB?
		{
			return rgb;
		}
		
		public int getWeight()
		{
			return weight;
		}
		
		public float randomSize(long seed)
		{
			if(minSize == maxSize)
				return maxSize;
			
			Random random = new Random(seed);
			
			return random.nextFloat(minSize, maxSize);
		}
		
		public short randomBrightness(long seed)
		{
			if(minBrightness == maxBrightness)
				return maxBrightness;
			
			Random random = new Random(seed);
			
			return (short) random.nextInt(minBrightness, maxBrightness);
		}
		
		public static DustCloudType fromTag(CompoundTag tag)
		{
			Color.IntRGB rgb = new Color.IntRGB();
			rgb.fromTag(tag.getCompound(RGB));
			
			return new DustCloudType(rgb, tag.getFloat(MIN_SIZE), tag.getFloat(MAX_SIZE), tag.getShort(MIN_BRIGHTNESS), tag.getShort(MAX_BRIGHTNESS), tag.getInt(WEIGHT));
		}
	}
}
