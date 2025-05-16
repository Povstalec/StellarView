package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.povstalec.stellarview.mixin.VertexFormatElementMixin;

public class StellarViewVertexFormat
{
	public static VertexFormatElement ELEMENT_HEIGHT_WIDTH_SIZE;
	
	// NOTE: The order of elements very much MATTERS!!!
	public static VertexFormat STAR_POS_COLOR_LY;
	
	public static VertexFormat STAR_POS_COLOR_LY_TEX;
	
	// NOTE: VertexFormatElements now require an ID, and this ID can only be between 0 and 31. The ELEMENTS list here is AT-ed to be public
	// so this method can access the size. What this means though is that if enough mods add VertexFormatElements the game will not be able to
	// start. The reason the IDs are limited is that later on, an Integer is used as a bitmap for indexing which VertexFormatElements are used
	// which then means that you can only have 32 different ones, as an Integer is 32 bits. This is a new limitation and hopefully will be adapted
	// in the future in a different manner.
	private static VertexFormatElement register(VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count)
	{
		final int index = (int) VertexFormatElementMixin.getElements().stream().filter((el) -> el.usage().equals(usage)).count();
		return VertexFormatElement.register(getNextVertexFormatElementId(), index, type, usage, count);
	}
	
	public static void setupVertexFormats()
	{
		ELEMENT_HEIGHT_WIDTH_SIZE = register(VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3);
		
		STAR_POS_COLOR_LY = VertexFormat.builder()
				.add("StarPos", VertexFormatElement.POSITION)
				.add("Color", VertexFormatElement.COLOR)
				.add("HeightWidthSize", ELEMENT_HEIGHT_WIDTH_SIZE)
				.add("UV0", VertexFormatElement.UV0)
				.build();
		
		STAR_POS_COLOR_LY_TEX = VertexFormat.builder()
				.add("StarPos", VertexFormatElement.POSITION)
				.add("Color", VertexFormatElement.COLOR)
				.add("HeightWidthSize", ELEMENT_HEIGHT_WIDTH_SIZE)
				.add("UV0", VertexFormatElement.UV0)
				.build();
	}
	
	private static int getNextVertexFormatElementId()
	{
		int id = VertexFormatElementMixin.getElements().size();
		while (VertexFormatElement.byId(id) != null)
		{
			if (++id >= VertexFormatElement.MAX_COUNT)
				throw new RuntimeException("Too many mods registering VertexFormatElements");
		}
		return id;
	}
}
