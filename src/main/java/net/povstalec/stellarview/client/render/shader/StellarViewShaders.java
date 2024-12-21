package net.povstalec.stellarview.client.render.shader;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.povstalec.stellarview.StellarView;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class StellarViewShaders
{
	@Nullable
    private static StarShaderInstance rendertypeStarShader;
	private static StarShaderInstance rendertypeStarTexShader;
	private static DustCloudShaderInstance rendertypeDustCloudShader;
	
	@EventBusSubscriber(modid = StellarView.MODID, value = Dist.CLIENT, bus= EventBusSubscriber.Bus.MOD)
    public static class ShaderInit
    {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException
        {
            event.registerShader(new StarShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star"), StellarViewVertexFormat.STAR_POS_COLOR_LY.get()),
            		(shaderInstance) ->
            		{
            			rendertypeStarShader = (StarShaderInstance) shaderInstance;
            		});
			
			event.registerShader(new StarShaderInstance(event.getResourceProvider(), new ResourceLocation(StellarView.MODID,"rendertype_star_tex"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX),
					(shaderInstance) ->
					{
						rendertypeStarTexShader = (StarShaderInstance) shaderInstance;
					});
			
			event.registerShader(new DustCloudShaderInstance(event.getResourceProvider(), new ResourceLocation(StellarView.MODID,"rendertype_dust_cloud"), StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX),
					(shaderInstance) ->
					{
						rendertypeDustCloudShader = (DustCloudShaderInstance) shaderInstance;
					});
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
