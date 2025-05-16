package net.povstalec.stellarview.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.povstalec.stellarview.client.resourcepack.ResourcepackReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ResourcepackReloadMixin
{
	@Shadow
	private ReloadableResourceManager resourceManager;
	
	private static ResourcepackReloadListener.ReloadListener listener = new ResourcepackReloadListener.ReloadListener();
	
	@Inject(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V", shift = At.Shift.BEFORE)
	)
	private void reload(CallbackInfo info)
	{
		resourceManager.registerReloadListener(listener);
	}
}
