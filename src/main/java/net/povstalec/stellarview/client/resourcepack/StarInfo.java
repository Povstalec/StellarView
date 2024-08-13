package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.povstalec.stellarview.common.util.Color;

public class StarInfo
{
	private final ArrayList<Star.StarType> starTypes;
	private int totalWeight = 0;
	
	public static final Star.StarType WHITE_STAR = new Star.StarType(new Color.IntRGB(255, 255, 255), 0.15F, 0.25F, (short) 100, (short) 255, 1);
	public static final List<Star.StarType> DEFAULT_STARS = Arrays.asList(WHITE_STAR);
	public static final StarInfo DEFAULT_STAR_INFO = new StarInfo(DEFAULT_STARS);
	
	public static final Codec<StarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Star.StarType.CODEC.listOf().fieldOf("star_types").forGetter(starInfo -> starInfo.starTypes)
			).apply(instance, StarInfo::new));
	
	public StarInfo(List<Star.StarType> starTypes)
	{
		this.starTypes = new ArrayList<Star.StarType>(starTypes);
		
		for(Star.StarType starType : starTypes)
		{
			this.totalWeight += starType.getWeight();
		}
	}
	
	public Star.StarType getRandomStarType(long seed)
	{
		Random random = new Random(seed);
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalWeight); i < starTypes.size() - 1; i++)
		{
			weight -= starTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return starTypes.get(i);
	}
}
