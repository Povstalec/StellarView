package net.povstalec.stellarview.client.render.level;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.util.StellarViewLightmapEffects;
import net.povstalec.stellarview.client.render.ViewCenters;
import net.povstalec.stellarview.common.config.NetherConfig;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;

public class StellarViewNetherEffects extends DimensionSpecialEffects.NetherEffects
{
	public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
	
	public StellarViewNetherEffects() {}
	
	/*@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(NetherConfig.replace_vanilla.get())
			return ViewCenters.renderViewCenterSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
        return false;
    }
	
	@Override
	public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
    {
		if(NetherConfig.replace_vanilla.get())
		{
			//StellarViewLightmapEffects.defaultLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
			
			if(StellarView.isEnhancedCelestialsLoaded())
				EnhancedCelestialsCompatibility.adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
		}
	}*/
}
