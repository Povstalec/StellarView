package net.povstalec.stellarview.compatibility.aether;

import com.aetherteam.aether.client.renderer.level.AetherSkyRenderEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.util.StellarViewLightmapEffects;
import net.povstalec.stellarview.client.resourcepack.ViewCenters;
import net.povstalec.stellarview.common.config.AetherConfig;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class StellarViewAetherEffects extends AetherSkyRenderEffects
{
	public static final ResourceLocation AETHER_EFFECTS = new ResourceLocation(StellarView.AETHER_MODID, "the_aether");
	
	public StellarViewAetherEffects() {}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(AetherConfig.replace_default.get())
			return ViewCenters.renderViewCenterSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return super.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
    }
	
	@Override
	public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
    {
		if(AetherConfig.replace_default.get())
		{
			StellarViewLightmapEffects.defaultLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
			
			if(StellarView.isEnhancedCelestialsLoaded())
				EnhancedCelestialsCompatibility.adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
		}
		else
			super.adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
	}
}
