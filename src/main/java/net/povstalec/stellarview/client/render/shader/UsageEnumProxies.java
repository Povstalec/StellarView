package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class UsageEnumProxies
{
	public static final EnumProxy<VertexFormatElement.Usage> HEIGHT_WIDTH_SIZE_DISTANCE_ENUM_PROXY =
			new EnumProxy<>(VertexFormatElement.Usage.class, "stellarview:hwsd", getSetupState());
	
	public static VertexFormatElement.Usage.SetupState getSetupState()
	{
		return (size, type, stride, pointer, index) ->
				GlStateManager._vertexAttribPointer(index, size, type, false, stride, pointer);
	}
}
