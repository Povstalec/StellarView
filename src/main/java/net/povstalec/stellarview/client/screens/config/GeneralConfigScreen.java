package net.povstalec.stellarview.client.screens.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.povstalec.stellarview.client.screens.config.ConfigList.BooleanConfigEntry;
import net.povstalec.stellarview.client.util.CelestialInstancedBuffer;
import net.povstalec.stellarview.common.config.GeneralConfig;
import org.jetbrains.annotations.Nullable;

public class GeneralConfigScreen extends Screen
{
	private final Screen parentScreen;
	private ConfigList configList;

    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int BACK_BUTTON_HEIGHT = 20;
    private static final int BACK_BUTTON_TOP_OFFSET = 26;
    
    private static final int OPTIONS_LIST_TOP_HEIGHT = 24;
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

	
	public GeneralConfigScreen(@Nullable Screen parentScreen)
	{
		super(Component.translatable("gui.stellarview.config.general"));
		this.parentScreen = parentScreen;
	}

	
	@Override
    public void init()
    {
		super.init();
		
		this.configList = new ConfigList(minecraft, this.width, this.height, 
				OPTIONS_LIST_TOP_HEIGHT, this.height - OPTIONS_LIST_BOTTOM_OFFSET, OPTIONS_LIST_ITEM_HEIGHT);
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.static_sky"),
				this.width, GeneralConfig.static_sky));
		if(CelestialInstancedBuffer.INSTANCING_PREREQUISITES)
		{
			this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.instancing"),
					this.width, GeneralConfig.instancing));
		}
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.alt_vertex_build_order"),
				this.width, GeneralConfig.alt_vertex_build_order));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.use_game_ticks"),
				this.width, GeneralConfig.use_game_ticks));
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.tick_multiplier").append(Component.literal(": ")),
				Component.empty(),
				this.width, GeneralConfig.tick_multiplier));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_view_center_rotation"),
				this.width, GeneralConfig.disable_view_center_rotation));
		
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.space_region_render_distance").append(Component.literal(": ")),
				Component.empty(),
				this.width, GeneralConfig.space_region_render_distance));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.gravitational_lensing"),
				this.width, GeneralConfig.gravitational_lensing));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.dust_clouds"),
				this.width, GeneralConfig.dust_clouds));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_stars"), 
				this.width, GeneralConfig.disable_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.textured_stars"),
				this.width, GeneralConfig.textured_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.light_pollution"),
				this.width, GeneralConfig.light_pollution));
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.star_brightness").append(Component.literal(": ")),
				Component.literal("%"),
				this.width, GeneralConfig.star_brightness));
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.dust_cloud_brightness").append(Component.literal(": ")),
				Component.literal("%"),
				this.width, GeneralConfig.dust_cloud_brightness));
		
		this.addWidget(this.configList);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, 
				(button) ->
				{
					if(this.parentScreen != null)
						this.minecraft.setScreen(this.parentScreen);
					else
						this.onClose();
				})
				.bounds((this.width - BACK_BUTTON_WIDTH) / 2, this.height - BACK_BUTTON_TOP_OFFSET, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT).build());
    }
	
	@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(graphics);
        this.configList.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(this.font, this.title, this.width / 2, 8, 16777215);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
	
}
