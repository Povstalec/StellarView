package net.povstalec.stellarview.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class StellarViewShadersMixin // Based on GameRendererMixin
{
	@Inject(
			method = "reloadShaders",
			at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, shift = At.Shift.AFTER),
			slice = @Slice(from = @At(value = "NEW", target = "net/minecraft/client/renderer/ShaderInstance", ordinal = 0)),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void registerStellarViewShaders(ResourceProvider resourceProvider, CallbackInfo info, List<?> shaderStages, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> programs) throws IOException
	{
		StellarViewShaders.ShaderInit.registerShaders(resourceProvider, programs);
	}
}