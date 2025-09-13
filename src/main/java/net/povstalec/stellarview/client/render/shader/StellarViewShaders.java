package net.povstalec.stellarview.client.render.shader;

import java.io.IOException;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.stellarview.StellarView;

public class StellarViewShaders
{
	@Nullable
    private static CelestialShaderInstance rendertypeStarShader;
	private static CelestialShaderInstance rendertypeStarTexShader;
	private static CelestialShaderInstance rendertypeDustCloudShader;
	
	private static CelestialShaderInstance rendertypeInstanced;
	
	@Mod.EventBusSubscriber(modid = StellarView.MODID, value = Dist.CLIENT, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class ShaderInit
    {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException
        {
            event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), new ResourceLocation(StellarView.MODID,"rendertype_star"), StellarViewVertexFormat.STAR_POS_COLOR_LY),
            		(shaderInstance) ->
            		{
            			rendertypeStarShader = (CelestialShaderInstance) shaderInstance;
            		});
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), new ResourceLocation(StellarView.MODID,"rendertype_star_tex"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX),
					(shaderInstance) ->
					{
						rendertypeStarTexShader = (CelestialShaderInstance) shaderInstance;
					});
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), new ResourceLocation(StellarView.MODID,"rendertype_dust_cloud"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX),
					(shaderInstance) ->
					{
						rendertypeDustCloudShader = (CelestialShaderInstance) shaderInstance;
					});
			
			event.registerShader(new CelestialShaderInstance(event.getResourceProvider(), new ResourceLocation(StellarView.MODID,"rendertype_instanced"), DefaultVertexFormat.POSITION_TEX),
					(shaderInstance) ->
					{
						rendertypeInstanced = (CelestialShaderInstance) shaderInstance;
					});
        }
    }
	
	public static CelestialShaderInstance starShader()
	{
		return rendertypeStarShader;
	}
	
	public static CelestialShaderInstance starTexShader()
	{
		return rendertypeStarTexShader;
	}
	
	public static CelestialShaderInstance starDustCloudShader()
	{
		return rendertypeDustCloudShader;
	}
	
	public static CelestialShaderInstance instancedShader()
	{
		return rendertypeInstanced;
	}
}
