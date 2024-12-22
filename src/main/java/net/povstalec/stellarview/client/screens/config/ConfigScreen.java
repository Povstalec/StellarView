package net.povstalec.stellarview.client.screens.config;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen
{
	private final Screen parentScreen;

    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int BACK_BUTTON_HEIGHT = 20;
    private static final int BACK_BUTTON_TOP_OFFSET = 26;

	
	public ConfigScreen(@Nullable Screen parentScreen)
	{
		super(Component.translatable("gui.stellarview.config"));
		this.parentScreen = parentScreen;
	}

	
	@Override
    public void init()
    {
		int l = this.height / 4 - 24;
		
		super.init();
		this.addRenderableWidget(Button.builder(Component.translatable("gui.stellarview.config.general"), 
				(button) -> this.minecraft.setScreen(new GeneralConfigScreen(this))).bounds(this.width / 2 - 100, l, 200, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.translatable("gui.stellarview.config.overworld"),
				(button) -> this.minecraft.setScreen(new OverworldConfigScreen(this))).bounds(this.width / 2 - 100, l + 24 * 2, 200, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.translatable("gui.stellarview.config.nether"),
				(button) -> this.minecraft.setScreen(new NetherConfigScreen(this))).bounds(this.width / 2 - 100, l + 24 * 3, 200, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.translatable("gui.stellarview.config.end"),
				(button) -> this.minecraft.setScreen(new EndConfigScreen(this))).bounds(this.width / 2 - 100, l + 24 * 4, 200, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.translatable("gui.stellarview.config.twilight_forest"),
				(button) -> this.minecraft.setScreen(new TwilightForestConfigScreen(this))).bounds(this.width / 2 - 100, l + 24 * 5, 200, 20).build());
		
		//this.addRenderableWidget(Button.builder(Component.translatable("gui.stellarview.config.aether"),
		//		(button) -> this.minecraft.setScreen(new AetherConfigScreen(this))).bounds(this.width / 2 - 100, l + 24 * 6, 200, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, 
				(button) -> this.minecraft.setScreen(this.parentScreen))
				.bounds((this.width - BACK_BUTTON_WIDTH) / 2, this.height - BACK_BUTTON_TOP_OFFSET, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT).build());
    }
	
	@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(graphics);
        graphics.drawString(this.font, this.title, this.width / 2, 8, 16777215);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
	
}
