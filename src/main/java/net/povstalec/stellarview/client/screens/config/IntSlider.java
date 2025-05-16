package net.povstalec.stellarview.client.screens.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

public class IntSlider extends AbstractSliderButton
{
	private static final DecimalFormat FORMAT = new DecimalFormat("0");
	
	protected Component prefix, suffix;
	
	protected int minValue;
	protected int maxValue;
	
	protected int multiplier;
	
	public IntSlider(int x, int y, int width, int height, Component prefix, Component suffix, int minValue, int maxValue, int value, int multiplier)
	{
		super(x, y, width, height, Component.empty(), snapToNearest(value, minValue, maxValue, multiplier));
		
		System.out.println("PREPARING: " + prefix.toString() + " " + value);
		this.prefix = prefix;
		this.suffix = suffix;
		
		this.minValue = minValue;
		this.maxValue = maxValue;
		
		this.multiplier = multiplier;
		
		this.updateMessage();
	}
	
	public int getValue()
	{
		return (int) Math.round(this.value * (maxValue - minValue) + minValue);
	}
	
	public String stringValue()
	{
		return FORMAT.format(getValue());
	}
	
	public void setValue(double value)
	{
		this.value = this.snapToNearest(value, this.minValue, this.maxValue, multiplier);
		this.updateMessage();
	}
	
	/**
	 * @param value Percentage of slider range
	 */
	private void setSliderValue(double value)
	{
		double oldValue = this.value;
		this.value = this.snapToNearest(value, this.minValue, this.maxValue, this.multiplier);
		if (!Mth.equal(oldValue, this.value))
			this.applyValue();
		
		this.updateMessage();
	}
	
	@Override
	public void onClick(double mouseX, double mouseY)
	{
		this.setSliderValue((mouseX - (this.getX() + 4)) / (this.width - 8));
	}
	
	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY)
	{
		super.onDrag(mouseX, mouseY, dragX, dragY);
		this.setSliderValue((mouseX - (this.getX() + 4)) / (this.width - 8));
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		boolean isPressed = keyCode == GLFW.GLFW_KEY_LEFT;
		if(isPressed || keyCode == GLFW.GLFW_KEY_RIGHT)
			this.setValue(this.getValue() + (isPressed ? -1 : 1) * this.multiplier);
		
		return false;
	}
	
	@Override
	protected void updateMessage()
	{
		setMessage(Component.literal("").append(prefix).append(stringValue()).append(suffix));
	}
	
	@Override
	protected void applyValue() {}
	
	
	
	public static double snapToNearest(double value, int min, int max, int multiplier)
	{
		value = (value - min) / (max - min);
		value = Mth.lerp(Mth.clamp(value, 0D, 1D), min, max);
		value = (multiplier * Math.round(value / multiplier));
		value = Mth.clamp(value, min, max);
		
		return Mth.map(value, min, max, 0D, 1D);
	}
}
