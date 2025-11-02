package net.povstalec.stellarview.client.render.level;

import net.povstalec.stellarview.api.client.render.level.StellarViewSpecialEffects;
import org.joml.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.config.NetherConfig;

import javax.annotation.Nullable;

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
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(NetherConfig.replace_vanilla.get())
			super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
		
        return false;
    }
}
