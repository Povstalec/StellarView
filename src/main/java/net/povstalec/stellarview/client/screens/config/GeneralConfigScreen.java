package net.povstalec.stellarview.client.screens.config;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.povstalec.stellarview.client.screens.config.ConfigList.BooleanConfigEntry;
import net.povstalec.stellarview.common.config.GeneralConfig;

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
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.use_game_ticks"),
				this.width, GeneralConfig.use_game_ticks));
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.tick_multiplier").append(Component.literal(": ")),
				Component.empty(),
				this.width, GeneralConfig.tick_multiplier));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_view_center_rotation"),
				this.width, GeneralConfig.disable_view_center_rotation));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.gravitational_lensing"),
				this.width, GeneralConfig.gravitational_lensing));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_stars"), 
				this.width, GeneralConfig.disable_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.bright_stars"), 
				this.width, GeneralConfig.bright_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.textured_stars"),
				this.width, GeneralConfig.textured_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.dust_clouds"),
				this.width, GeneralConfig.dust_clouds));
		
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.space_region_render_distance").append(Component.literal(": ")),
				Component.empty(),
				this.width, GeneralConfig.space_region_render_distance));
		
		this.addWidget(this.configList);

		this.addRenderableWidget(new Button((this.width - BACK_BUTTON_WIDTH) / 2, this.height - BACK_BUTTON_TOP_OFFSET, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT, 
				CommonComponents.GUI_BACK, onPress ->
			{
				if(this.parentScreen != null)
					this.minecraft.setScreen(this.parentScreen);
				else
					this.onClose();
			}));
    }
	
	@Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(poseStack);
        this.configList.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
	
}
