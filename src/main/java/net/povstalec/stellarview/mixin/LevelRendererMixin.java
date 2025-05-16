package net.povstalec.stellarview.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.ViewCenters;
import net.povstalec.stellarview.common.config.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin
{
	@Shadow
	@Nullable
	private ClientLevel level;
	
	@Shadow
	private int ticks;
	
	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	private void renderSky(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTicks, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo info)
	{
		if(renderViewCenterSky(level) && ViewCenters.renderViewCenterSky(level, ticks, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog))
			info.cancel();
	}
	
	private boolean renderViewCenterSky(ClientLevel level)
	{
		// I guess this is an attempt at making it a tiny bit more efficient?
		switch(level.dimension().location().getNamespace())
		{
			case "minecraft":
				return switch(level.dimension().location().getPath())
				{
					case "overworld" -> OverworldConfig.replace_vanilla.get();
					case "the_end" -> EndConfig.replace_vanilla.get();
					case "the_nether" -> NetherConfig.replace_vanilla.get();
					default -> true;
				};
			case StellarView.AETHER_MODID:
				if("the_aether".equals(level.dimension().location().getPath()))
					return AetherConfig.replace_default.get();
				break;
			case StellarView.TWILIGHT_FOREST_MODID:
				if("twilight_forest".equals(level.dimension().location().getPath()))
					return TwilightForestConfig.replace_default.get();
		}
		
		return true;
	}
}
