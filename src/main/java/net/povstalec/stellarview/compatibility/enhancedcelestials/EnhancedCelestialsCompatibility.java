package net.povstalec.stellarview.compatibility.enhancedcelestials;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import dev.corgitaco.enhancedcelestials.api.client.ColorSettings;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials.client.ECWorldRenderer;
import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.client.render.level.util.StellarViewLightmapEffects;
import net.povstalec.stellarview.common.util.Color;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.concurrent.atomic.AtomicReference;

public class EnhancedCelestialsCompatibility
{
	public static final float getMoonSize(ClientLevel level, float defaultSize)
	{
		return ECWorldRenderer.getMoonSize(defaultSize);
	}

	public static final Color.FloatRGBA getMoonColor(ClientLevel level, float partialTicks)
	{
		AtomicReference<Color.FloatRGBA> result = new AtomicReference<>(new Color.FloatRGBA(1, 1, 1));
		/*
		 * Shamelessly copy pasted from
		 * https://github.com/CorgiTaco/Enhanced-Celestials/blob/1.20.X/common/src/main/java/dev/corgitaco/enhancedcelestials/client/ECWorldRenderer.java
		 * because what else am I supposed to do to have it work the same way for the sake of compatibility?
		 */
		EnhancedCelestials.lunarForecastWorldData(level).ifPresent(data -> {
			ColorSettings lastColorSettings = data.lastLunarEventHolder().value().getClientSettings().colorSettings();
			ColorSettings currentColorSettings = data.currentLunarEventHolder().value().getClientSettings().colorSettings();

			Vector3f lastGLColor = lastColorSettings.getGLMoonColor();
			Vector3f currentGLColor = currentColorSettings.getGLMoonColor();

			float blend = data.getBlend();

			float r = Mth.clampedLerp(lastGLColor.x(), currentGLColor.x(), blend);
			float g = Mth.clampedLerp(lastGLColor.y(), currentGLColor.y(), blend);
			float b = Mth.clampedLerp(lastGLColor.z(), currentGLColor.z(), blend);
			RenderSystem.setShaderColor(r, g, b, 1.0F - level.getRainLevel(partialTicks));

			result.set(new Color.FloatRGBA(r > 1F ? 1F : r, g > 1F ? 1F : g, b > 1F ? 1F : b));
		});
		return result.get();
	}
	
	public static final void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
	{
		/*
		 * Shamelessly copy pasted from
		 * https://github.com/CorgiTaco/Enhanced-Celestials/blob/1.20.X/common/src/main/java/dev/corgitaco/enhancedcelestials/client/ECWorldRenderer.java
		 * because, again, what else am I supposed to do to have it work the same way?
		 */
		EnhancedCelestials.lunarForecastWorldData(level).ifPresent(data -> {
			LunarEvent lastEvent = data.lastLunarEvent();
			LunarEvent currentEvent = data.currentLunarEvent();

			ColorSettings colorSettings = currentEvent.getClientSettings().colorSettings();
			ColorSettings lastColorSettings = lastEvent.getClientSettings().colorSettings();

			Vector3f glSkyLightColor = lastColorSettings.getGLSkyLightColor();
			Vector3f targetColor = new Vector3f(glSkyLightColor.x(), glSkyLightColor.y(), glSkyLightColor.z());

			float eventBlend = data.getBlend() - skyDarken;
			targetColor.lerp(colorSettings.getGLSkyLightColor(), eventBlend);

			float skyBlend = (1 - skyDarken) - level.getRainLevel(partialTicks);
			colors.lerp(targetColor, skyBlend);
		});
	}
}
