package net.povstalec.stellarview.client.render.shader;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class StellarViewShaders
{
	@Nullable
    private static StarShaderInstance rendertypeStarShader;
	private static StarShaderInstance rendertypeStarTexShader;
	private static DustCloudShaderInstance rendertypeDustCloudShader;
	
    public static class ShaderInit
    {
        public static void registerShaders(ResourceProvider factory, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> programs) throws IOException
        {
			programs.add(Pair.of(new StarShaderInstance(factory, new ResourceLocation(StellarView.MODID,"rendertype_star"), StellarViewVertexFormat.STAR_POS_COLOR_LY), (shaderInstance) ->
			{
				rendertypeStarShader = (StarShaderInstance) shaderInstance;
			}));
			
			programs.add(Pair.of(new StarShaderInstance(factory, new ResourceLocation(StellarView.MODID,"rendertype_star_tex"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX), (shaderInstance) ->
			{
				rendertypeStarTexShader = (StarShaderInstance) shaderInstance;
			}));
			
			programs.add(Pair.of(new DustCloudShaderInstance(factory, new ResourceLocation(StellarView.MODID,"rendertype_dust_cloud"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX), (shaderInstance) ->
			{
				rendertypeDustCloudShader = (DustCloudShaderInstance) shaderInstance;
			}));
        }
    }
	
	public static StarShaderInstance starShader()
	{
		return rendertypeStarShader;
	}
	
	public static StarShaderInstance starTexShader()
	{
		return rendertypeStarTexShader;
	}
	
	public static DustCloudShaderInstance starDustCloudShader()
	{
		return rendertypeDustCloudShader;
	}
}
