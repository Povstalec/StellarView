package net.povstalec.stellarview.client.render.level;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.StellarViewSpecialEffects;
import net.povstalec.stellarview.api.init.SolarSystemInit;
import net.povstalec.stellarview.api.init.StarFieldInit;

public class StellarViewBetaEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation BETA_EFFECTS = new ResourceLocation(StellarView.MODID, "beta_effects");
	
	public StellarViewBetaEffects()
	{
		super(new StellarViewSky(SolarSystemInit.SOL_SYSTEM).starField(StarFieldInit.MILKY_WAY),
				192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
	}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		super.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return true;
    }
}
