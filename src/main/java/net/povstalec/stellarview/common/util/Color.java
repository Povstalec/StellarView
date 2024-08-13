package net.povstalec.stellarview.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Color
{
	public static final int MAX_INT_VALUE = 255;
	public static final int MIN_INT_VALUE = 0;
	
	public static final float MAX_FLOAT_VALUE = 1F;
	public static final float MIN_FLOAT_VALUE = 0F;
	
	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String BLUE = "blue";
	public static final String ALPHA = "alpha";
	
	private static void checkIntValue(int value)
	{
		if(value > MAX_INT_VALUE)
			throw(new IllegalArgumentException("Value may not be higher than 255"));
		else if(value < MIN_INT_VALUE)
			throw(new IllegalArgumentException("Value may not be lower than 0"));
	}
	
	private static void checkFloatValue(float value)
	{
		if(value > MAX_FLOAT_VALUE)
			throw(new IllegalArgumentException("Value may not be higher than 1.0"));
		else if(value < MIN_FLOAT_VALUE)
			throw(new IllegalArgumentException("Value may not be lower than 0.0"));
	}
	
	public static class IntRGB
	{
		protected int red;
		protected int green;
		protected int blue;
		
		public static final Codec<Color.IntRGB> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(RED).forGetter(Color.IntRGB::red),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(GREEN).forGetter(Color.IntRGB::green),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(BLUE).forGetter(Color.IntRGB::blue)
				).apply(instance, Color.IntRGB::new));
		
		public IntRGB(int red, int green, int blue)
		{
			if(red > MAX_INT_VALUE || green > MAX_INT_VALUE || blue > MAX_INT_VALUE)
				throw(new IllegalArgumentException("No value may be higher than 255"));
			else if(red < MIN_INT_VALUE || green < MIN_INT_VALUE || blue < MIN_INT_VALUE)
				throw(new IllegalArgumentException("No value may be lower than 0"));
			
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public void setRed(int red)
		{
			checkIntValue(red);
			
			this.red = red;
		}
		
		public int red()
		{
			return red;
		}
		
		public void setGreen(int green)
		{
			checkIntValue(green);
			
			this.green = green;
		}
		
		public int green()
		{
			return green;
		}
		
		public void setBlue(int blue)
		{
			checkIntValue(blue);
			
			this.blue = blue;
		}
		
		public int blue()
		{
			return blue;
		}
	}
	
	public static class IntRGBA extends IntRGB
	{
	    public static final Codec<Color.IntRGBA> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(RED).forGetter(Color.IntRGBA::red),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(GREEN).forGetter(Color.IntRGBA::green),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).fieldOf(BLUE).forGetter(Color.IntRGBA::blue),
				Codec.intRange(MIN_INT_VALUE, MAX_INT_VALUE).optionalFieldOf(ALPHA, MAX_INT_VALUE).forGetter(Color.IntRGBA::alpha)
				).apply(instance, Color.IntRGBA::new));
	    
	    protected int alpha;
		
		public IntRGBA(int red, int green, int blue, int alpha)
		{
			super(red, green, blue);
			
			if(alpha > MAX_INT_VALUE)
				throw(new IllegalArgumentException("No value may be higher than 255"));
			else if(alpha < MIN_INT_VALUE)
				throw(new IllegalArgumentException("No value may be lower than 0"));
			
			this.alpha = alpha;
		}
		
		public IntRGBA(int red, int green, int blue)
		{
			this(red, green, blue, 255);
		}
		
		public void setAlpha(int alpha)
		{
			checkIntValue(alpha);
			
			this.alpha = alpha;
		}
		
		public int alpha()
		{
			return alpha;
		}
	}
	
	public static class FloatRGB
	{
		protected float red;
		protected float green;
		protected float blue;
		
		public FloatRGB(float red, float green, float blue)
		{
			if(red > MAX_FLOAT_VALUE || green > MAX_FLOAT_VALUE || blue > MAX_FLOAT_VALUE)
				throw(new IllegalArgumentException("No value may be higher than 1.0"));
			else if(red < MIN_FLOAT_VALUE || green < MIN_FLOAT_VALUE || blue < MIN_FLOAT_VALUE)
				throw(new IllegalArgumentException("No value may be lower than 0.0"));
			
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public void setRed(float red)
		{
			checkFloatValue(red);
			
			this.red = red;
		}
		
		public float red()
		{
			return red;
		}
		
		public void setGreen(float green)
		{
			checkFloatValue(green);
			
			this.green = green;
		}
		
		public float green()
		{
			return green;
		}
		
		public void setBlue(float blue)
		{
			checkFloatValue(blue);
			
			this.blue = blue;
		}
		
		public float blue()
		{
			return blue;
		}
	}
	
	public static class FloatRGBA extends FloatRGB
	{
	    public static final Codec<Color.FloatRGBA> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.floatRange(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE).fieldOf(RED).forGetter(Color.FloatRGBA::red),
				Codec.floatRange(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE).fieldOf(GREEN).forGetter(Color.FloatRGBA::green),
				Codec.floatRange(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE).fieldOf(BLUE).forGetter(Color.FloatRGBA::blue),
				Codec.floatRange(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE).optionalFieldOf(ALPHA, MAX_FLOAT_VALUE).forGetter(Color.FloatRGBA::alpha)
				).apply(instance, Color.FloatRGBA::new));
	    
	    protected float alpha;
		
		public FloatRGBA(float red, float green, float blue, float alpha)
		{
			super(red, green, blue);
			
			if(alpha > MAX_FLOAT_VALUE)
				throw(new IllegalArgumentException("No value may be higher than 1.0"));
			else if(alpha < MIN_FLOAT_VALUE)
				throw(new IllegalArgumentException("No value may be lower than 0.0"));
			
			this.alpha = alpha;
		}
		
		public FloatRGBA(float red, float green, float blue)
		{
			this(red, green, blue, 1);
		}
		
		public void setAlpha(float alpha)
		{
			checkFloatValue(alpha);
			
			this.alpha = alpha;
		}
		
		public float alpha()
		{
			return alpha;
		}
	}
}
