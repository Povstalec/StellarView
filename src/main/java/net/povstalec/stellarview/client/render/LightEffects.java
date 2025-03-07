package net.povstalec.stellarview.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;

public class LightEffects
{
	private static float starBrightness = 0F;
	private static float dustCloudBrightness = 0F;
	
	public static float lightSourceStarDimming(ClientLevel level, Camera camera)
	{
		// Brightness of the position where the player is standing, 15 is subtracted from the ambient skylight, that way only block light is accounted for
		int brightnessAtBlock = level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15);
		float brightness = 0.5F + 1.5F * ((15F - brightnessAtBlock) / 15F);
		
		if(starBrightness < brightness)
		{
			starBrightness += 0.01F;
			
			if(starBrightness > brightness)
				starBrightness = brightness;
		}
		else if(starBrightness > brightness)
		{
			starBrightness -= 0.01F;
			
			if(starBrightness < brightness)
				starBrightness = brightness;
		}
		
		return starBrightness;
	}
	
	public static float lightSourceDustCloudDimming(ClientLevel level, Camera camera)
	{
		// Brightness of the position where the player is standing, 15 is subtracted from the ambient skylight, that way only block light is accounted for
		int brightnessAtBlock = level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15);
		float brightness = 2F * ((7F - brightnessAtBlock) / 7F);
		
		if(brightness < 0)
			brightness = 0;
		
		if(dustCloudBrightness < brightness)
		{
			dustCloudBrightness += 0.001F;
			
			if(dustCloudBrightness > brightness)
				dustCloudBrightness = brightness;
		}
		else if(dustCloudBrightness > brightness)
		{
			dustCloudBrightness -= 0.001F;
			
			if(dustCloudBrightness < brightness)
				dustCloudBrightness = brightness;
		}
		
		return dustCloudBrightness;
	}
	
	public static float rainDimming(ClientLevel level, float partialTicks)
	{
		return 1F - level.getRainLevel(partialTicks);
	}
	
	public static float getStarBrightness(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = GeneralConfig.star_brightness.get() / 100F;
		
		if(!viewCenter.starsAlwaysVisible())
			brightness *= level.getStarBrightness(partialTicks);
		else
			brightness *= 2F;
		
		if(GeneralConfig.light_pollution.get())
			brightness *= LightEffects.lightSourceStarDimming(level, camera);
		else
			brightness *= 2F;
		
		return brightness;
	}
	
	public static float getDustBrightness(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = GeneralConfig.dust_cloud_brightness.get() / 100F;
		
		if(!viewCenter.starsAlwaysVisible())
			brightness *= level.getStarBrightness(partialTicks);
		else
			brightness *= 2F;
		
		if(GeneralConfig.light_pollution.get())
			brightness *= LightEffects.lightSourceDustCloudDimming(level, camera);
		else
			brightness *= 2F;
		
		return brightness;
	}
	
	
	
	public static float dayBrightness(ViewCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = getStarBrightness(viewCenter, level, camera, partialTicks);
		
		if(brightness < viewCenter.dayBlending().dayMaxBrightness() && size > viewCenter.dayBlending().dayMinVisibleSize())
		{
			float aboveSize = size >= viewCenter.dayBlending().dayMaxVisibleSize() ? viewCenter.dayBlending().dayVisibleRange() : size - viewCenter.dayBlending().dayMinVisibleSize();
			float brightnessPercentage = aboveSize / viewCenter.dayBlending().dayVisibleRange();
			float minBrightness = brightnessPercentage * viewCenter.dayBlending().dayMaxBrightness();
			
			if(brightness < minBrightness)
				brightness = minBrightness;
		}
		
		return viewCenter.starsIgnoreRain() ? brightness : brightness * LightEffects.rainDimming(level, partialTicks);
	}
	
	public static float starDayBrightness(ViewCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = getStarBrightness(viewCenter, level, camera, partialTicks);
		
		if(brightness < viewCenter.sunDayBlending().dayMaxBrightness() && size > viewCenter.sunDayBlending().dayMinVisibleSize())
		{
			float aboveSize = size >= viewCenter.sunDayBlending().dayMaxVisibleSize() ? viewCenter.sunDayBlending().dayVisibleRange() : size - viewCenter.sunDayBlending().dayMinVisibleSize();
			float brightnessPercentage = aboveSize / viewCenter.sunDayBlending().dayVisibleRange();
			float minBrightness = brightnessPercentage * viewCenter.sunDayBlending().dayMaxBrightness();
			
			if(brightness < minBrightness)
				brightness = minBrightness;
		}
		
		return viewCenter.starsIgnoreRain() ? brightness : brightness * LightEffects.rainDimming(level, partialTicks);
	}
	
	public static float dustCloudBrightness(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = getDustBrightness(viewCenter, level, camera, partialTicks);
		
		return viewCenter.starsIgnoreRain() ? brightness : brightness * LightEffects.rainDimming(level, partialTicks);
	}
	
	public static float nebulaBrightness(ViewCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = getDustBrightness(viewCenter, level, camera, partialTicks);
		
		if(brightness < viewCenter.dayBlending().dayMaxBrightness() && size > viewCenter.dayBlending().dayMinVisibleSize())
		{
			float aboveSize = size >= viewCenter.dayBlending().dayMaxVisibleSize() ? viewCenter.dayBlending().dayVisibleRange() : size - viewCenter.dayBlending().dayMinVisibleSize();
			float brightnessPercentage = aboveSize / viewCenter.dayBlending().dayVisibleRange();
			float minBrightness = brightnessPercentage * viewCenter.dayBlending().dayMaxBrightness();
			
			if(brightness < minBrightness)
				brightness = minBrightness;
		}
		
		return viewCenter.starsIgnoreRain() ? brightness : brightness * LightEffects.rainDimming(level, partialTicks);
	}
	
	/**
	 * Returns the brightness of stars in the current Player location
	 * @param level The Level the Player is currently in
	 * @param camera Player Camera
	 * @param partialTicks
	 * @return
	 */
	public static float starBrightness(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = getStarBrightness(viewCenter, level, camera, partialTicks);
		
		return viewCenter.starsIgnoreRain() ? brightness : brightness * LightEffects.rainDimming(level, partialTicks);
	}
}
