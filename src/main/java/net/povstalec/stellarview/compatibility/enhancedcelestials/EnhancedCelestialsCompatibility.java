package net.povstalec.stellarview.compatibility.enhancedcelestials;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.api.client.ColorSettings;
import corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import corgitaco.enhancedcelestials.client.ECWorldRenderer;
import corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.api.StellarViewSpecialEffects;

public class EnhancedCelestialsCompatibility
{
	public static final float getMoonSize(float partialTicks)
	{
		return ECWorldRenderer.getMoonSize(partialTicks);
	}
	
	public static final float[] getMoonColor(ClientLevel level, float partialTicks)
	{
		/*
		 * Shamelessly copy pasted from
		 * https://github.com/CorgiTaco/Enhanced-Celestials/blob/1.20.X/Common/src/main/java/corgitaco/enhancedcelestials/client/ECWorldRenderer.java#L19C9-L34C91
		 * because what else am I supposed to do to have it work the same way for the sake of compatibility?
		 */
		EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) level).getLunarContext();
        if(enhancedCelestialsContext != null)
        {
            LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
            
            ColorSettings lastColorSettings = lunarForecast.getMostRecentEvent().value().getClientSettings().colorSettings();
            ColorSettings currentColorSettings = lunarForecast.getCurrentEvent(level.getRainLevel(1) < 1).value().getClientSettings().colorSettings();
            
            Vector3f lastGLColor = lastColorSettings.getGLMoonColor();
            Vector3f currentGLColor = currentColorSettings.getGLMoonColor();
            
            float blend = lunarForecast.getBlend();
            
            float r = Mth.clampedLerp(lastGLColor.x(), currentGLColor.x(), blend);
            float g = Mth.clampedLerp(lastGLColor.y(), currentGLColor.y(), blend);
            float b = Mth.clampedLerp(lastGLColor.z(), currentGLColor.z(), blend);
            
            return new float[] {r, g, b};
        }
        
        return new float[] {1, 1, 1};
	}
	
	public static final void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
    {
		float darkMultiplier = StellarViewSpecialEffects.getSkyDarken(level, 1.0F);
		
		boolean darkerWorld = true;
		if(darkerWorld)
		{
			float trueSkyDarken;
			if(level.getSkyFlashTime() > 0)
				trueSkyDarken = 1.0F;
			else
				trueSkyDarken = darkMultiplier * 0.95F + 0.05F;
			
			Vector3f skyVector = (new Vector3f(trueSkyDarken, trueSkyDarken, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
			
			/*
			 * Shamelessly copy pasted from
			 * https://github.com/CorgiTaco/Enhanced-Celestials/blob/1.20.X/Common/src/main/java/corgitaco/enhancedcelestials/client/ECWorldRenderer.java#L61
			 * because, again, what else am I supposed to do to have it work the same way?
			 */
			EnhancedCelestialsWorldData enhancedCelestialsWorldData = (EnhancedCelestialsWorldData) level;
			if(enhancedCelestialsWorldData != null)
			{
				EnhancedCelestialsContext enhancedCelestialsContext = enhancedCelestialsWorldData.getLunarContext();
				if(enhancedCelestialsContext != null)
				{
					LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
					LunarEvent lastEvent = lunarForecast.getMostRecentEvent().value();
					LunarEvent currentEvent = lunarForecast.getCurrentEvent(level.getRainLevel(1) < 1).value();

					ColorSettings colorSettings = currentEvent.getClientSettings().colorSettings();
					ColorSettings lastColorSettings = lastEvent.getClientSettings().colorSettings();

					Vector3f glSkyLightColor = lastColorSettings.getGLSkyLightColor();
					Vector3f targetColor = new Vector3f(glSkyLightColor.x(), glSkyLightColor.y(), glSkyLightColor.z());

					skyDarken = (level.getSkyDarken(1.0F) - 0.2F) / 0.8F;
					float eventBlend = lunarForecast.getBlend() - skyDarken;
					targetColor.lerp(colorSettings.getGLSkyLightColor(), eventBlend);

					float skyBlend = (1 - skyDarken) - level.getRainLevel(partialTicks);
		            
					skyVector.lerp(targetColor, skyBlend);
				}
			}
			
			Vector3f lightColor = new Vector3f();
			float naturalLight = LightTexture.getBrightness(level.dimensionType(), pixelY) * trueSkyDarken; // pixelY represents natural light
			float artificialLight = LightTexture.getBrightness(level.dimensionType(), pixelX) * skyLight; // pixelX represents artificial light
			float f10 = artificialLight * ((artificialLight * 0.6F + 0.4F) * 0.6F + 0.4F);
			float f11 = artificialLight * (artificialLight * artificialLight * 0.6F + 0.4F);
			lightColor.set(artificialLight, f10, f11);
			if(level.effects().forceBrightLightmap())
			{
				lightColor.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
				StellarViewSpecialEffects.clampColor(lightColor);
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
