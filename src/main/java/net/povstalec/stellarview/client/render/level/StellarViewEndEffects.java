package net.povstalec.stellarview.client.render.level;

import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.api.client.render.level.StellarViewSpecialEffects;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StellarViewEndEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation END_EFFECTS = ResourceLocation.withDefaultNamespace("the_end");
	
	public StellarViewEndEffects()
	{
		super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
	}
	
	@Override
	public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness)
	{
		return fogColor.scale(0.15F);
	}
	
	@Override
	@Nullable
	public float[] getSunriseColor(float timeOfDay, float partialTicks)
	{
		return null;
	}
}
