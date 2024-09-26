package net.povstalec.stellarview.client.render.shader;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

public class StarShaderInstance extends ShaderInstance
{
	@Nullable
	public final Uniform RELATIVE_SPACE_LY;
	public final Uniform RELATIVE_SPACE_KM;
	
	public StarShaderInstance(ResourceProvider provider, ResourceLocation shaderLocation, VertexFormat format)
			throws IOException
	{
		super(provider, shaderLocation, format);
		this.RELATIVE_SPACE_LY = this.getUniform("RelativeSpaceLy");
		this.RELATIVE_SPACE_KM = this.getUniform("RelativeSpaceKm");
	}
}
