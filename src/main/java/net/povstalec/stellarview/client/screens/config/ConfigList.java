package net.povstalec.stellarview.client.screens.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.common.config.StellarViewConfigValue;

public class ConfigList extends ObjectSelectionList<ConfigList.ConfigEntry>
{
	public ConfigList(Minecraft minecraft, int screenWidth, int listHeight, int headerHeight, int itemHeight)
	{
		super(minecraft, screenWidth, listHeight, headerHeight, itemHeight);
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
			update();
	    }
	    
	    protected void update()
	    {
	    	SpaceRenderer.updateSpaceObjects();
	    }

		@Override
		public Component getNarration()
		{
			return reset;
		}

		@Override
		public void render(GuiGraphics graphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float partialTick)
		{
			this.resetToDefault.setX(k + 210);
	        this.resetToDefault.setY(j);
	        this.resetToDefault.render(graphics, n, o, partialTick);
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
	    	if(this.cycleButton.isMouseOver(mouseX, mouseY))
	    		((AbstractButton) this.cycleButton).onPress();
	    	update();
	    	
			return super.mouseClicked(mouseX, mouseY, key);
	    }
		
		@Override
		public void render(GuiGraphics graphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float partialTick)
		{
			this.cycleButton.setX(k);
	        this.cycleButton.setY(j);
	        this.cycleButton.render(graphics, n, o, partialTick);
			super.render(graphics, i, j, k, l, m, n, o, bl, partialTick);
		}
		
	}
	
	public static class SliderConfigEntry extends ConfigEntry
	{
		protected AbstractWidget sliderButton;
		protected StellarViewConfigValue.IntValue value;
		protected int multiplier;
		
		public SliderConfigEntry(Component component1, Component component2, int screenWidth, StellarViewConfigValue.IntValue value, int multiplier)
		{
			this.value = value;
			this.multiplier = multiplier;
			this.sliderButton = new ExtendedSlider(0, 0, 200, 20, 
					component1, component2,
					value.getMin() * multiplier, value.getMax() * multiplier, value.get() * multiplier, multiplier, 1, true);
		}
		
		public SliderConfigEntry(Component component1, Component component2, int screenWidth, StellarViewConfigValue.IntValue value)
		{
			this(component1, component2, screenWidth, value, 1);
		}
		
		protected void reset()
		{
			this.value.set(this.value.getDefault());
			((ExtendedSlider) this.sliderButton).setValue((double) this.value.get() * multiplier);
			super.reset();
		}
		
		protected void onChanged()
		{
	    	value.set((int) ((ExtendedSlider) this.sliderButton).getValue() / multiplier);
	    	update();
		}
	    
	    @Override
	    public boolean mouseClicked(double mouseX, double mouseY, int key)
	    {
	    	if(this.sliderButton.isMouseOver(mouseX, mouseY))
	    		((ExtendedSlider) this.sliderButton).mouseClicked(mouseX, mouseY, key);
	    	onChanged();
	    	
			return super.mouseClicked(mouseX, mouseY, key);
	    }
	    
	    @Override
	    public boolean mouseDragged(double mouseX, double mouseY, int key, double dragX, double dragY)
	    {
	    	if(this.sliderButton.isMouseOver(mouseX, mouseY))
	    		((ExtendedSlider) this.sliderButton).mouseDragged(mouseX, mouseY, key, dragX, dragY);
	    	
			return super.mouseDragged(mouseX, mouseY, key, dragX, dragY);
	    }
	    
	    @Override
	    public boolean mouseReleased(double mouseX, double mouseY, int key)
	    {
	    	if(this.sliderButton.isMouseOver(mouseX, mouseY))
	    		((ExtendedSlider) this.sliderButton).mouseReleased(mouseX, mouseY, key);
	    	onChanged();
	    	
			return super.mouseReleased(mouseX, mouseY, key);
	    }
	    
	    @Override
	    public void mouseMoved(double mouseX, double mouseY)
	    {
	    	if(this.sliderButton.isMouseOver(mouseX, mouseY))
	    		((ExtendedSlider) this.sliderButton).mouseMoved(mouseX, mouseY);
	    	
			super.mouseMoved(mouseX, mouseY);
	    }
		
		@Override
		public void render(GuiGraphics graphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float partialTick)
		{
			this.sliderButton.setX(k);
	        this.sliderButton.setY(j);
	        this.sliderButton.render(graphics, n, o, partialTick);
			super.render(graphics, i, j, k, l, m, n, o, bl, partialTick);
		}
		
	}
}
