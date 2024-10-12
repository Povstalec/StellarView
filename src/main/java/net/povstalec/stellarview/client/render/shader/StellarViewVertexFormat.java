package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class StellarViewVertexFormat
{
	public static final VertexFormatElement ELEMENT_HEIGHT_WIDTH_SIZE = register(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	
	// NOTE: The order of elements very much MATTERS!!!
	public static final VertexFormat STAR_POS_COLOR_LY = VertexFormat.builder()
			.add("StarPos", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("HeightWidthSize", VertexFormatElement.POSITION)
			.build();

	// NOTE: VertexFormatElements now require an ID, and this ID can only be between 0 and 31. The ELEMENTS list here is AT-ed to be public
	// so this method can access the size. What this means though is that if enough mods add VertexFormatElements the game will not be able to
	// start. The reason the IDs are limited is that later on, an Integer is used as a bitmap for indexing which VertexFormatElements are used
	// which then means that you can only have 32 different ones, as an Integer is 32 bits. This is a new limitation and hopefully will be adapted
	// in the future in a different manner.
	private static VertexFormatElement register(int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
		return VertexFormatElement.register(VertexFormatElement.ELEMENTS.size(), index, type, usage, count);
	}
}
