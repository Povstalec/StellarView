package net.povstalec.stellarview.client.screens.config;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.povstalec.stellarview.client.screens.config.ConfigList.BooleanConfigEntry;
import net.povstalec.stellarview.client.screens.config.ConfigList.SliderConfigEntry;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class ConfigScreen extends Screen
{
	private final Screen parentScreen;
	private ConfigList configList;

    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int BACK_BUTTON_HEIGHT = 20;
    private static final int BACK_BUTTON_TOP_OFFSET = 26;
    
    private static final int OPTIONS_LIST_TOP_HEIGHT = 24;
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

	
	public ConfigScreen(@Nullable Screen parentScreen)
	{
		super(Component.translatable("gui.sgjourney.config_stellarview"));
		this.parentScreen = parentScreen;
	}

	
	@Override
    public void init()
    {
		super.init();
		
		this.configList = new ConfigList(minecraft, this.width, this.height, 
				OPTIONS_LIST_TOP_HEIGHT, this.height - OPTIONS_LIST_BOTTOM_OFFSET, OPTIONS_LIST_ITEM_HEIGHT);
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.replace_vanilla"), 
				this.width, StellarViewConfig.replace_vanilla));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_sun"), 
				this.width, StellarViewConfig.disable_sun));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_moon"), 
				this.width, StellarViewConfig.disable_moon));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_moon_phases"), 
				this.width, StellarViewConfig.disable_moon_phases));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_stars"), 
				this.width, StellarViewConfig.disable_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.day_stars"), 
				this.width, StellarViewConfig.day_stars));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.bright_stars"), 
				this.width, StellarViewConfig.bright_stars));

		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_x"), 
				this.width, StellarViewConfig.milky_way_x));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_y"), 
				this.width, StellarViewConfig.milky_way_y));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_z"), 
				this.width, StellarViewConfig.milky_way_z));

		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_alpha"), 
				this.width, StellarViewConfig.milky_way_alpha));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_beta"), 
				this.width, StellarViewConfig.milky_way_beta));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_gamma"), 
				this.width, StellarViewConfig.milky_way_gamma));
		
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(poseStack);
        this.configList.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
	
}
