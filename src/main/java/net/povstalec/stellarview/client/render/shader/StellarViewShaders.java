package net.povstalec.stellarview.client.render.shader;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class StellarViewShaders
{
	@Nullable
    private static CelestialShaderInstance rendertypeStarShader;
	private static CelestialShaderInstance rendertypeStarTexShader;
	private static CelestialShaderInstance rendertypeDustCloudShader;
	
	private static CelestialShaderInstance rendertypeStarInstanced;
	private static CelestialShaderInstance rendertypeStarTexInstanced;
	private static CelestialShaderInstance rendertypeDustCloudInstanced;
	
	public static class ShaderInit
	{
		public static void registerShaders(ResourceProvider factory, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> programs) throws IOException
		{
			programs.add(Pair.of(new CelestialShaderInstance(factory, ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star"), StellarViewVertexFormat.STAR_POS_COLOR_LY), (shaderInstance) ->
			{
				rendertypeStarShader = (CelestialShaderInstance) shaderInstance;
			}));
			
			programs.add(Pair.of(new CelestialShaderInstance(factory, ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star_instanced"), DefaultVertexFormat.POSITION), (shaderInstance) ->
			{
				rendertypeStarInstanced = (CelestialShaderInstance) shaderInstance;
			}));
			
			
			
			programs.add(Pair.of(new CelestialShaderInstance(factory, ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star_tex"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX), (shaderInstance) ->
			{
				rendertypeStarTexShader = (CelestialShaderInstance) shaderInstance;
			}));
			
			programs.add(Pair.of(new CelestialShaderInstance(factory, ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star_tex_instanced"), DefaultVertexFormat.POSITION_TEX), (shaderInstance) ->
			{
				rendertypeStarTexInstanced = (CelestialShaderInstance) shaderInstance;
			}));
			
			
			
			programs.add(Pair.of(new CelestialShaderInstance(factory, ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_dust_cloud"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX), (shaderInstance) ->
			{
				rendertypeDustCloudShader = (CelestialShaderInstance) shaderInstance;
			}));
			
			programs.add(Pair.of(new CelestialShaderInstance(factory, ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_dust_cloud_instanced"), DefaultVertexFormat.POSITION_TEX), (shaderInstance) ->
			{
				rendertypeDustCloudInstanced = (CelestialShaderInstance) shaderInstance;
			}));
		}
	}
	
	public static CelestialShaderInstance starShader()
	{
		return rendertypeStarShader;
	}
	
	public static CelestialShaderInstance instancedStarShader()
	{
		return rendertypeStarInstanced;
	}
	
	public static CelestialShaderInstance starTexShader()
	{
		return rendertypeStarTexShader;
	}
	
	public static CelestialShaderInstance starDustCloudShader()
	{
		return rendertypeDustCloudShader;
	}
	
	public static CelestialShaderInstance instancedStarTexShader()
	{
		return rendertypeStarTexInstanced;
	}
	
	public static CelestialShaderInstance instancedDustCloudShader()
	{
		return rendertypeDustCloudInstanced;
	}
}
