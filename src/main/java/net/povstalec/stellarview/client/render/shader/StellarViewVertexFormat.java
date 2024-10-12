package net.povstalec.stellarview.client.render.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.povstalec.stellarview.StellarView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@EventBusSubscriber(modid = StellarView.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StellarViewVertexFormat
{
	public static final DeferredVertexThing<VertexFormatElement> ELEMENT_HEIGHT_WIDTH_SIZE = register(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	
	// NOTE: The order of elements very much MATTERS!!!
	public static final DeferredVertexThing<VertexFormat> STAR_POS_COLOR_LY = new DeferredVertexThing<>(() -> VertexFormat.builder()
			.add("StarPos", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("HeightWidthSize", ELEMENT_HEIGHT_WIDTH_SIZE.get())
			.build());

	// NOTE: VertexFormatElements now require an ID, and this ID can only be between 0 and 31. The ELEMENTS list here is AT-ed to be public
	// so this method can access the size. What this means though is that if enough mods add VertexFormatElements the game will not be able to
	// start. The reason the IDs are limited is that later on, an Integer is used as a bitmap for indexing which VertexFormatElements are used
	// which then means that you can only have 32 different ones, as an Integer is 32 bits. This is a new limitation and hopefully will be adapted
	// in the future in a different manner.
	private static DeferredVertexThing<VertexFormatElement> register(int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
		return new DeferredVertexThing<>(() -> VertexFormatElement.register(VertexFormatElement.ELEMENTS.size(), index, type, usage, count));
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		// The registering of VertexFormatElements is not threadsafe so it needs to be done later
		event.enqueueWork(() -> {
			ELEMENT_HEIGHT_WIDTH_SIZE.initialize();
			STAR_POS_COLOR_LY.initialize();
		});
	}

	public static class DeferredVertexThing<T> implements Supplier<T> {
		private @Nullable T computed;
		private Supplier<T> compute;

		DeferredVertexThing(Supplier<T> compute) {
			this.compute = compute;
		}

		@Override
		public @NotNull T get() {
			if (computed != null) return computed;
			throw new IllegalStateException("Tried to access DeferredVertexThing before it was initialized");
		}

		void initialize() {
			this.computed = compute.get();
		}
	}
}
