package net.povstalec.stellarview.client.render.shader;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.povstalec.stellarview.StellarView;
import java.io.IOException;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.jetbrains.annotations.Nullable;

public class StellarViewShaders
{
	@Nullable
    private static CelestialShaderInstance rendertypeStarShader;
	private static CelestialShaderInstance rendertypeStarTexShader;
	private static CelestialShaderInstance rendertypeDustCloudShader;
	
	private static CelestialShaderInstance rendertypeStarInstanced;
	private static CelestialShaderInstance rendertypeStarTexInstanced;
	private static CelestialShaderInstance rendertypeDustCloudInstanced;
	
	@EventBusSubscriber(modid = StellarView.MODID, value = Dist.CLIENT, bus= EventBusSubscriber.Bus.MOD)
	public static class ShaderInit
	{
		@SubscribeEvent
		public static void registerShaders(RegisterShadersEvent event) throws IOException
		{
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star"), StellarViewVertexFormat.STAR_POS_COLOR_LY.get()),
					(shaderInstance) ->
					{
						rendertypeStarShader = (CelestialShaderInstance) shaderInstance;
					});
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star_instanced"), DefaultVertexFormat.POSITION),
					(shaderInstance) ->
					{
						rendertypeStarInstanced = (CelestialShaderInstance) shaderInstance;
					});
			
			
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star_tex"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get()),
					(shaderInstance) ->
					{
						rendertypeStarTexShader = (CelestialShaderInstance) shaderInstance;
					});
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star_tex_instanced"), DefaultVertexFormat.POSITION_TEX),
					(shaderInstance) ->
					{
						rendertypeStarTexInstanced = (CelestialShaderInstance) shaderInstance;
					});
			
			
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_dust_cloud"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get()),
					(shaderInstance) ->
					{
						rendertypeDustCloudShader = (CelestialShaderInstance) shaderInstance;
					});
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_dust_cloud_instanced"), DefaultVertexFormat.POSITION_TEX),
					(shaderInstance) ->
					{
						rendertypeDustCloudInstanced = (CelestialShaderInstance) shaderInstance;
					});
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
