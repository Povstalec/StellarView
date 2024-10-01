package net.povstalec.stellarview.client.render.level.util;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class StellarViewLightmapEffects
{
	public static float getSkyDarken(ClientLevel level, float partialTicks)
	{
		float timeOfDay = level.getTimeOfDay(partialTicks);
		float darken = 1.0F - (Mth.cos(timeOfDay * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
		darken = Mth.clamp(darken, 0.0F, 1.0F);
		darken = 1.0F - darken;
		darken *= 1.0F - level.getRainLevel(partialTicks) * 5.0F / 16.0F;
		darken *= 1.0F - level.getThunderLevel(partialTicks) * 5.0F / 16.0F;
		
		float darkMultiplier = 0.8F;
		
		return darken * darkMultiplier + 0.2F;
	}
	
	/**
	 * @param color
	 * @see Copied from LightTexture#clampColor(Vector3f)
	 */
	public static void clampColor(Vector3f color)
	{
		color.set(Mth.clamp(color.x, 0.0F, 1.0F), Mth.clamp(color.y, 0.0F, 1.0F), Mth.clamp(color.z, 0.0F, 1.0F));
	}
	
	public static void defaultLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
    {
		float darkMultiplier = getSkyDarken(level, 1.0F);
		
		boolean darkerWorld = true;
		if(darkerWorld)
		{
			float trueSkyDarken;
			if(level.getSkyFlashTime() > 0)
				trueSkyDarken = 1.0F;
			else
				trueSkyDarken = darkMultiplier * 0.95F + 0.05F;
			
			Vector3f skyVector = (new Vector3f(trueSkyDarken, trueSkyDarken, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
			Vector3f lightColor = new Vector3f();
			float naturalLight = LightTexture.getBrightness(level.dimensionType(), pixelY) * trueSkyDarken; // pixelY represents natural light
			float artificialLight = LightTexture.getBrightness(level.dimensionType(), pixelX) * skyLight; // pixelX represents artificial light
			float f10 = artificialLight * ((artificialLight * 0.6F + 0.4F) * 0.6F + 0.4F);
			float f11 = artificialLight * (artificialLight * artificialLight * 0.6F + 0.4F);
			lightColor.set(artificialLight, f10, f11);
			if(level.effects().forceBrightLightmap())
			{
				lightColor.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
				clampColor(lightColor);
			}
			else
			{
				Minecraft minecraft = Minecraft.getInstance();
				Vector3f vector3f2 = (new Vector3f((Vector3fc)skyVector)).mul(naturalLight);
				lightColor.add(vector3f2);
				lightColor.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
				if(minecraft.gameRenderer.getDarkenWorldAmount(partialTicks) > 0.0F)
				{
					float f12 = minecraft.gameRenderer.getDarkenWorldAmount(partialTicks);
					Vector3f vector3f3 = (new Vector3f((Vector3fc)lightColor)).mul(0.7F, 0.6F, 0.6F);
					lightColor.lerp(vector3f3, f12);
				}
			}
			colors.set(lightColor);
		}
	}
}
