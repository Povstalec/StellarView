package net.povstalec.stellarview.client.screens.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.povstalec.stellarview.client.screens.config.ConfigList.BooleanConfigEntry;
import net.povstalec.stellarview.common.config.TwilightForestConfig;
import org.jetbrains.annotations.Nullable;

public class TwilightForestConfigScreen extends Screen
{
	private final Screen parentScreen;
	private ConfigList configList;

    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int BACK_BUTTON_HEIGHT = 20;
    private static final int BACK_BUTTON_TOP_OFFSET = 26;
    
    private static final int OPTIONS_LIST_TOP_HEIGHT = 24;
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

	
	public TwilightForestConfigScreen(@Nullable Screen parentScreen)
	{
		super(Component.translatable("gui.stellarview.config.twilight_forest"));
		this.parentScreen = parentScreen;
	}

	
	@Override
    public void init()
    {
		super.init();
		
		this.configList = new ConfigList(minecraft, this.width, this.height, 
				OPTIONS_LIST_TOP_HEIGHT, this.height - OPTIONS_LIST_BOTTOM_OFFSET, OPTIONS_LIST_ITEM_HEIGHT);
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.replace_default"),
				this.width, TwilightForestConfig.replace_default));
		this.configList.add(new BooleanConfigEntry(Component.translatable("gui.stellarview.config_priority"), 
				this.width, TwilightForestConfig.config_priority));


		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.meteor_shower_chance").append(Component.literal(": ")),
				Component.literal("\u0025"),
				this.width, TwilightForestConfig.meteor_shower_chance));
		this.configList.add(new ConfigList.SliderConfigEntry(Component.translatable("gui.stellarview.shooting_star_chance").append(Component.literal(": ")),
				Component.literal("\u0025"),
				this.width, TwilightForestConfig.shooting_star_chance));
		
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
