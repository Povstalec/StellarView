package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.povstalec.stellarview.StellarView;

@EventBusSubscriber(modid = StellarView.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StellarViewVertexFormat
{
	public static final Lazy<VertexFormatElement> ELEMENT_HEIGHT_WIDTH_SIZE_DISTANCE = register(VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	
	// NOTE: The order of elements very much MATTERS!!!
	public static final Lazy<VertexFormat> STAR_POS_COLOR_LY = Lazy.of(() -> VertexFormat.builder()
			.add("StarPos", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("HeightWidthSizeDistance", ELEMENT_HEIGHT_WIDTH_SIZE_DISTANCE.get())
			.build());
	
	public static final Lazy<VertexFormat> STAR_POS_COLOR_LY_TEX = Lazy.of(() -> VertexFormat.builder()
			.add("StarPos", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("HeightWidthSizeDistance", ELEMENT_HEIGHT_WIDTH_SIZE_DISTANCE.get())
			.add("UV0", VertexFormatElement.UV0)
			.build());

	// NOTE: VertexFormatElements now require an ID, and this ID can only be between 0 and 31. The ELEMENTS list here is AT-ed to be public
	// so this method can access the size. What this means though is that if enough mods add VertexFormatElements the game will not be able to
	// start. The reason the IDs are limited is that later on, an Integer is used as a bitmap for indexing which VertexFormatElements are used
	// which then means that you can only have 32 different ones, as an Integer is 32 bits. This is a new limitation and hopefully will be adapted
	// in the future in a different manner.
	private static Lazy<VertexFormatElement> register(VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count)
	{
        return Lazy.of(() -> {
			final int index = (int) VertexFormatElement.ELEMENTS.stream().filter((el) -> el.usage().equals(usage)).count();
			return VertexFormatElement.register(getNextVertexFormatElementId(), index, type, usage, count);
		});
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event)
	{
		// The registering of VertexFormatElements is not threadsafe so it needs to be done later
		event.enqueueWork(() -> {
			ELEMENT_HEIGHT_WIDTH_SIZE_DISTANCE.get();
			STAR_POS_COLOR_LY.get();
			STAR_POS_COLOR_LY_TEX.get();
		});
	}

	private static int getNextVertexFormatElementId() {
		int id = VertexFormatElement.ELEMENTS.size();
		while (VertexFormatElement.byId(id) != null) {
			if (++id >= VertexFormatElement.MAX_COUNT) {
				throw new RuntimeException("Too many mods registering VertexFormatElements");
			}
		}
		return id;
	}
}
