package net.povstalec.stellarview.client.render.level;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.api.client.render.level.StellarViewSpecialEffects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StellarViewOverworldEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation OVERWORLD_EFFECTS = ResourceLocation.withDefaultNamespace("overworld");
	
	public static final float TWILIGHT_START = 0.4F;
	
	protected final float[] sunriseCol = new float[4];
	
	public StellarViewOverworldEffects()
	{
		super(OverworldEffects.CLOUD_LEVEL, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
	}
	
	@Override
	public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness)
	{
		return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
	}
	
	@Override
	@Nullable
	public float[] getSunriseColor(float timeOfDay, float partialTicks)
	{
		float sunXProjection = Mth.cos(timeOfDay * Mth.TWO_PI);
		
		// Twilight starts when the sun projection value is between -0.4 and 0.4
		if(sunXProjection >= -TWILIGHT_START && sunXProjection <= TWILIGHT_START)
		{
			float sunProjectionA = sunXProjection / 0.4F * 0.5F + 0.5F;
			float sunProjectionB = 1.0F - (1.0F - Mth.sin(sunProjectionA * Mth.PI)) * 0.99F;
			sunProjectionB *= sunProjectionB;
			
			this.sunriseCol[0] = sunProjectionA * 0.3F + 0.7F;
			this.sunriseCol[1] = sunProjectionA * sunProjectionA * 0.7F + 0.2F;
			this.sunriseCol[2] = sunProjectionA * sunProjectionA * 0.0F + 0.2F;
			this.sunriseCol[3] = sunProjectionB;
			
			return this.sunriseCol;
		}
		else
			return null;
	}
}
