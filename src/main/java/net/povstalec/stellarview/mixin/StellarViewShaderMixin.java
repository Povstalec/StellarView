package net.povstalec.stellarview.mixin;

import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.povstalec.stellarview.client.render.shader.DustCloudShaderInstance;
import net.povstalec.stellarview.client.render.shader.StarShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShaderInstance.class)
public class StellarViewShaderMixin
{
	@Shadow
	@Final
	private String name;
	
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"), allow = 1)
	private String modifyProgramId(String id)
	{
		if ((Object) this instanceof StarShaderInstance || (Object) this instanceof DustCloudShaderInstance)
			return FabricShaderProgram.rewriteAsId(id, name);
		
		return id;
	}
}
