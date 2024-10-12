package net.povstalec.stellarview.client.render.shader;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class StellarViewVertexFormat
{
	public static final VertexFormatElement ELEMENT_STAR_POS = register(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	public static final VertexFormatElement ELEMENT_HEIGHT_WIDTH_SIZE = register(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	public static final VertexFormatElement ELEMENT_COLOR = register(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
	
	// NOTE: The order of elements very much MATTERS!!!
	public static final VertexFormat STAR_POS_COLOR_LY = VertexFormat.builder()
			.add("StarPos", ELEMENT_STAR_POS)
			.add("Color", ELEMENT_COLOR)
			.add("HeightWidthSize", ELEMENT_HEIGHT_WIDTH_SIZE)
			.build();

	private static VertexFormatElement register(int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
		return VertexFormatElement.register(VertexFormatElement.ELEMENTS.size(), index, type, usage, count);
	}
}
