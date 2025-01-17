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
		float brightness = 1.5F * ((7F - brightnessAtBlock) / 15F);
		
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
	
	public static float dayBrightness(ViewCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		if(viewCenter.starsAlwaysVisible())
			return GeneralConfig.bright_stars.get() ? 0.5F * LightEffects.lightSourceStarDimming(level, camera) : 0.5F;
		
		float brightness = level.getStarBrightness(partialTicks) * 2;
		
		if(GeneralConfig.bright_stars.get())
			brightness = brightness * LightEffects.lightSourceStarDimming(level, camera);
		
		if(brightness < viewCenter.dayMaxBrightness && size > viewCenter.dayMinVisibleSize)
		{
			float aboveSize = size >= viewCenter.dayMaxVisibleSize ? viewCenter.dayVisibleSizeRange : size - viewCenter.dayMinVisibleSize;
			float brightnessPercentage = aboveSize / viewCenter.dayVisibleSizeRange;
			
			brightness = brightnessPercentage * viewCenter.dayMaxBrightness;
		}
		
		return brightness * LightEffects.rainDimming(level, partialTicks);
	}
}
