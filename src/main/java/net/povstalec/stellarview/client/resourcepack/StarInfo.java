package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.objects.StarField;
import net.povstalec.stellarview.client.resourcepack.objects.StarLike;
import net.povstalec.stellarview.common.util.Color;

public class StarInfo
{
	public static final ResourceLocation DEFAULT_STAR_TEXTURE = new ResourceLocation(StellarView.MODID,"textures/environment/star.png");
	
	protected ResourceLocation starTexture;
	
	private final ArrayList<StarLike.StarType> starTypes;
	private int totalWeight = 0;
	
	public static final StarLike.StarType WHITE_STAR = new StarLike.StarType(new Color.IntRGB(255, 255, 255), 0.15F, 0.25F, (short) 100, (short) 255, 1);
	public static final List<StarLike.StarType> DEFAULT_STARS = Arrays.asList(WHITE_STAR);
	public static final StarInfo DEFAULT_STAR_INFO = new StarInfo(DEFAULT_STAR_TEXTURE, DEFAULT_STARS);
	
	public static final Codec<StarInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("star_texture", DEFAULT_STAR_TEXTURE).forGetter(StarInfo::getStarTexture),
			StarLike.StarType.CODEC.listOf().fieldOf("star_types").forGetter(starInfo -> starInfo.starTypes)
			).apply(instance, StarInfo::new));
	
	public StarInfo(ResourceLocation starTexture, List<StarLike.StarType> starTypes)
	{
		this.starTexture = starTexture;
		this.starTypes = new ArrayList<StarLike.StarType>(starTypes);
		
		for(StarLike.StarType starType : starTypes)
		{
			this.totalWeight += starType.getWeight();
		}
	}
	
	public ResourceLocation getStarTexture()
	{
		return starTexture;
	}
	
	public StarLike.StarType getRandomStarType(long seed)
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
