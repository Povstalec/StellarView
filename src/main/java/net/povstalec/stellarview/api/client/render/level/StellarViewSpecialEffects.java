package net.povstalec.stellarview.api.client.render.level;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.ViewCenters;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.NetherConfig;
import net.povstalec.stellarview.compatibility.enhancedcelestials.EnhancedCelestialsCompatibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class StellarViewSpecialEffects extends DimensionSpecialEffects
{
	@Nullable
	protected Optional<ViewCenter> viewCenter = null;
	
	public StellarViewSpecialEffects(float cloudLevel, boolean hasGround, SkyType skyType, boolean forceBrightLightmap, boolean constantAmbientLight)
	{
		super(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
	}
	
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(viewCenter == null)
			viewCenter = Optional.ofNullable(ViewCenters.getViewCenter(level));
		
		return viewCenter.isPresent() && viewCenter.get().renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
	}
	
	@Override
	public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness)
	{
		return fogColor;
	}
	
	@Override
	public boolean isFoggyAt(int x, int y)
	{
		if(NetherConfig.replace_vanilla.get() && viewCenter != null && viewCenter.isPresent())
			return viewCenter.get().fog().isFoggyAt(x, y);
		
		return false;
	}
	
	public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
	{
		if(NetherConfig.replace_vanilla.get())
		{
			//StellarViewLightmapEffects.defaultLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
			
			if(StellarView.isEnhancedCelestialsLoaded())
				EnhancedCelestialsCompatibility.adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
		}
	}
}
