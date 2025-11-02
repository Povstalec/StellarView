package net.povstalec.stellarview.client.render.level;

import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.api.client.render.level.StellarViewSpecialEffects;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.config.EndConfig;

import javax.annotation.Nullable;

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
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(EndConfig.replace_vanilla.get())
			return super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
		
        return false;
    }
}
