package net.povstalec.stellarview.mixin;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(VertexFormatElement.class)
public interface VertexFormatElementMixin
{
	@Accessor("ELEMENTS")
	static List<VertexFormatElement> getElements()
	{
		throw new AssertionError();
	}
}
