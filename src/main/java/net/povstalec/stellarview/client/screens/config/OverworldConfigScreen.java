package net.povstalec.stellarview.client.screens.config;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.povstalec.stellarview.client.screens.config.ConfigList.BooleanConfigEntry;
import net.povstalec.stellarview.client.screens.config.ConfigList.SliderConfigEntry;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class OverworldConfigScreen extends Screen
{
	private final Screen parentScreen;
	private ConfigList configList;

    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int BACK_BUTTON_HEIGHT = 20;
    private static final int BACK_BUTTON_TOP_OFFSET = 26;
    
    private static final int OPTIONS_LIST_TOP_HEIGHT = 24;
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

	
	public OverworldConfigScreen(@Nullable Screen parentScreen)
	{
		super(Component.translatable("gui.stellarview.config.overworld"));
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
				this.width, OverworldConfig.disable_sun));
		
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_moon"), 
				this.width, OverworldConfig.disable_moon));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.disable_moon_phases"), 
				this.width, OverworldConfig.disable_moon_phases));

		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.meteor_shower_chance"),
				Component.literal("\u0025"),
				this.width, OverworldConfig.meteor_shower_chance));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.shooting_star_chance"),
				Component.literal("\u0025"),
				this.width, OverworldConfig.shooting_star_chance));

		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_x"),
				Component.empty(),
				this.width, OverworldConfig.milky_way_x));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_y"),
				Component.empty(),
				this.width, OverworldConfig.milky_way_y));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_z"),
				Component.empty(),
				this.width, OverworldConfig.milky_way_z));

		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_alpha"),
				Component.literal("\u00b0"),
				this.width, OverworldConfig.milky_way_alpha));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_beta"),
				Component.literal("\u00b0"),
				this.width, OverworldConfig.milky_way_beta));
		this.configList.add(new SliderConfigEntry(Component.translatable("gui.stellarview.milky_way_gamma"),
				Component.literal("\u00b0"),
				this.width, OverworldConfig.milky_way_gamma));
		
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
