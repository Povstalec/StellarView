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
    private static StarShaderInstance rendertypeStarShater;
	
	@EventBusSubscriber(modid = StellarView.MODID, value = Dist.CLIENT, bus= EventBusSubscriber.Bus.MOD)
    public static class ShaderInit
    {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException
        {
            event.registerShader(new StarShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"rendertype_star"), StellarViewVertexFormat.STAR_POS_COLOR_LY.get()),
            		(shaderInstance) ->
            {
            	rendertypeStarShater = (StarShaderInstance) shaderInstance;
            });
        }
    }
	
	public static StarShaderInstance starShader()
	{
		return rendertypeStarShater;
	}
}
