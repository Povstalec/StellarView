package net.povstalec.stellarview.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
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
	
	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"), allow = 1)
	private ResourceLocation modifyId(String id, Operation<ResourceLocation> original)
	{
		if ((Object) this instanceof StarShaderInstance || (Object) this instanceof DustCloudShaderInstance)
			return FabricShaderProgram.rewriteAsId(id, name);
		
		return original.call(id);
	}
}
