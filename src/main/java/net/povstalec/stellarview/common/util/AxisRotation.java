package net.povstalec.stellarview.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AxisRotation
{
	public double xAxis;
	public double yAxis;
	public double zAxis;
	
	public static final Codec<AxisRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("x_axis").forGetter(AxisRotation::xAxis),
			Codec.DOUBLE.fieldOf("y_axis").forGetter(AxisRotation::yAxis),
			Codec.DOUBLE.fieldOf("z_axis").forGetter(AxisRotation::zAxis)
			).apply(instance, AxisRotation::new));
	
	/**
	 * 
	 * @param xAxis Rotation around the X-axis (in degrees)
	 * @param yAxis Rotation around the Y-axis (in degrees)
	 * @param zAxis Rotation around the Z-axis (in degrees)
	 */
	public AxisRotation(double xAxis, double yAxis, double zAxis)
	{
		this.xAxis = Math.toRadians(xAxis);
		this.yAxis = Math.toRadians(yAxis);
		this.zAxis = Math.toRadians(zAxis);
	}
	
	/**
	 * @return Rotation around the X-Axis (in radians)
	 */
	public double xAxis()
	{
		return xAxis;
	}
	
	/**
	 * @return Rotation around the Y-Axis (in radians)
	 */
	public double yAxis()
	{
		return yAxis;
	}
	
	/**
	 * @return Rotation around the Z-Axis (in radians)
	 */
	public double zAxis()
	{
		return zAxis;
	}
}
