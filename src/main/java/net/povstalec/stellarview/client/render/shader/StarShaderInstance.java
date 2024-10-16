package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import javax.annotation.Nullable;
import java.io.IOException;

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
