package net.povstalec.stellarview.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

public class MinMax
{
	@Nullable
	private Float min;
	@Nullable
	private Float max;
	
	public static final Codec<MinMax> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("min", Float.NEGATIVE_INFINITY).forGetter(MinMax::getMin),
			Codec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY).forGetter(MinMax::getMax)
	).apply(instance, MinMax::new));
	
	public MinMax(float min, float max)
	{
		this.min = min;
		this.max = max;
	}
	
	public float getMin()
	{
		return min;
	}
	
	public Float getMax()
	{
		return max;
	}
	
	public boolean isInBounds(float value)
	{
		return value >= min && value <= max;
	}
}
