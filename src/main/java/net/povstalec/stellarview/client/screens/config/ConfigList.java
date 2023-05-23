package net.povstalec.stellarview.client.screens.config;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.povstalec.stellarview.common.config.StellarViewConfigValue;

public class ConfigList extends ObjectSelectionList<ConfigList.ConfigEntry>
{
	public ConfigList(Minecraft minecraft, int screenWidth, int screenHeight, int yStart, int yEnd, int itemHeight)
	{
		super(minecraft, screenWidth, screenHeight, yStart, yEnd, itemHeight);
	}
	
	public void add(ConfigEntry entry)
	{
		this.addEntry(entry);
	}

	public int getRowWidth()
	{
		return super.getRowWidth() + 45;
	}
	
	@Override
	protected int getScrollbarPosition()
	{
		return super.getScrollbarPosition() + 35;
	}

	public static abstract class ConfigEntry extends ObjectSelectionList.Entry<ConfigList.ConfigEntry>
	{
	    protected final AbstractWidget resetToDefault;
	    protected final Component reset = Component.translatable("gui.stellarview.reset");
	    
	    public ConfigEntry()
	    {
	    	this.resetToDefault = Button.builder(reset, (button) -> reset()).bounds(0, 0, 50, 20).build();
	    }
	    
	    @Override
	    public boolean mouseClicked(double mouseX, double mouseY, int key)
	    {
	    	if(this.resetToDefault.isMouseOver(mouseX, mouseY))
	    		((AbstractButton) this.resetToDefault).onPress();
	    	
			return super.mouseClicked(mouseX, mouseY, key);
	    }
	    
	    protected void reset()
	    {
			this.resetToDefault.playDownSound(Minecraft.getInstance().getSoundManager());
	    }

		@Override
		public Component getNarration()
		{
			return reset;
		}

		@Override
		public void render(PoseStack stack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float partialTick)
		{
			this.resetToDefault.setX(k + 210);
	        this.resetToDefault.setY(j);
	        this.resetToDefault.render(stack, n, o, partialTick);
		}
	}
	
	public static class BooleanConfigEntry extends ConfigEntry
	{
		protected AbstractWidget cycleButton;
		StellarViewConfigValue.BooleanValue value;
		
		public BooleanConfigEntry(Component component, int screenWidth, StellarViewConfigValue.BooleanValue value)
		{
			this.value = value;
			this.cycleButton = CycleButton.booleanBuilder(
					Component.translatable("gui.stellarview.true").withStyle(ChatFormatting.GREEN),
					Component.translatable("gui.stellarview.false").withStyle(ChatFormatting.RED))
					.withInitialValue(value.get())
					.create(0, 0, 200, 20, component, (cycleButton, isTrue) -> 
					{
						value.set(isTrue);
						this.cycleButton.playDownSound(Minecraft.getInstance().getSoundManager());
					});
		}
		
		@SuppressWarnings("unchecked")
		protected void reset()
		{
			this.value.set(this.value.getDefault());
			((CycleButton<Boolean>) this.cycleButton).setValue(this.value.get());
			super.reset();
		}
	    
	    @Override
	    public boolean mouseClicked(double mouseX, double mouseY, int key)
	    {
	    	if(this.cycleButton.isMouseOver((double) mouseX, (double) mouseY))
	    		((AbstractButton) this.cycleButton).onPress();
	    	
			return super.mouseClicked(mouseX, mouseY, key);
	    }
		
		@Override
		public void render(PoseStack stack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float partialTick)
		{
			this.cycleButton.setX(k);
	        this.cycleButton.setY(j);
	        this.cycleButton.render(stack, n, o, partialTick);
			super.render(stack, i, j, k, l, m, n, o, bl, partialTick);
		}
		
	}
}
