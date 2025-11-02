package net.povstalec.stellarview.client.render.level;

import net.povstalec.stellarview.api.client.render.level.StellarViewSpecialEffects;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StellarViewNetherEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation NETHER_EFFECTS = ResourceLocation.withDefaultNamespace("the_nether");
	
	public StellarViewNetherEffects()
	{
		super(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, true);
	}
	
	@Override
	@Nullable
	public float[] getSunriseColor(float timeOfDay, float partialTicks)
	{
		return null;
	}
}
