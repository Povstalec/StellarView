package net.povstalec.stellarview.client.render.shader;

import java.io.IOException;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.povstalec.stellarview.StellarView;
import org.jetbrains.annotations.Nullable;

public class StarShaderInstance extends ShaderInstance
{
	@Nullable
	public final Uniform RELATIVE_SPACE_LY;
	@Nullable
	public final Uniform RELATIVE_SPACE_KM;
	
	@Nullable
	public final Uniform LENSING_MAT;
	@Nullable
	public final Uniform LENSING_MAT_INV;
	@Nullable
	public final Uniform LENSING_INTENSITY;
	
	public StarShaderInstance(ResourceProvider provider, ResourceLocation shaderLocation, VertexFormat format)
			throws IOException
	{
		super(provider, shaderLocation.toString(), format);
		this.RELATIVE_SPACE_LY = this.getUniform("RelativeSpaceLy");
		this.RELATIVE_SPACE_KM = this.getUniform("RelativeSpaceKm");
		
		this.LENSING_MAT = this.getUniform("LensingMat");
		this.LENSING_MAT_INV = this.getUniform("LensingMatInv");
		this.LENSING_INTENSITY = this.getUniform("LensingIntensity");
	}
}
