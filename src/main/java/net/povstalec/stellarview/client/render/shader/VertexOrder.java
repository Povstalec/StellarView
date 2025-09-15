package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.povstalec.stellarview.common.config.GeneralConfig;

public class VertexOrder
{
	public static boolean texColor()
	{
		return !GeneralConfig.alt_vertex_build_order.get();
	}
	
	public static VertexFormat texColorFormat()
	{
		return texColor() ? DefaultVertexFormat.POSITION_TEX_COLOR :  DefaultVertexFormat.POSITION_COLOR_TEX;
	}
	
	public static ShaderInstance texColorShader()
	{
		return texColor() ? GameRenderer.getPositionTexColorShader() : GameRenderer.getPositionColorTexShader();
	}
}
