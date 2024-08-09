package net.povstalec.stellarview.client.render.shader;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class StellarViewVertexFormat
{
	public static final VertexFormatElement ELEMENT_POSITION = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	public static final VertexFormatElement ELEMENT_COLOR = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
	public static final VertexFormatElement ELEMENT_STAR_POS = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	
	// NOTE: The order of elements very much MATTERS!!!
	public static final VertexFormat STAR = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("StarPos", ELEMENT_STAR_POS).build());
}
