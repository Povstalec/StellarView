package net.povstalec.stellarview.client.render.level;

import net.povstalec.stellarview.api.client.render.level.StellarViewSpecialEffects;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
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

import javax.annotation.Nullable;

public class StellarViewNetherEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
	
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
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(NetherConfig.replace_vanilla.get())
			return ViewCenters.renderViewCenterSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return false;
    }
}
