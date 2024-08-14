package net.povstalec.stellarview.client.render.level;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.client.resourcepack.ViewCenters;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class StellarViewEndEffects extends DimensionSpecialEffects
{
	public StellarViewEndEffects()
	{
		super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
	}

    public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight)
    {
       return biomeFogColor;
    }

    public boolean isFoggyAt(int x, int y)
    {
       return true;
    }
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(StellarViewConfig.replace_end.get())
			return ViewCenters.renderViewCenterSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return false;
    }
}
