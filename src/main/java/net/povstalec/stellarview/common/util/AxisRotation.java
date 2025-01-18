package net.povstalec.stellarview.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.joml.Math;
import org.joml.Quaterniond;
import org.joml.Quaternionf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AxisRotation implements INBTSerializable<CompoundTag>
{
	public static final String X_AXIS = "x_axis";
	public static final String Y_AXIS = "y_axis";
	public static final String Z_AXIS = "z_axis";
	
	private boolean inDegrees;
	
	private double xAxis;
	private double yAxis;
	private double zAxis;
	
	private Quaterniond quaterniond;
	private Quaternionf quaternionf;
	
	public static final Codec<AxisRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("in_degrees", true).forGetter(axisRotation -> axisRotation.inDegrees),
			Codec.DOUBLE.fieldOf("x_axis").forGetter(AxisRotation::xAxis),
			Codec.DOUBLE.fieldOf("y_axis").forGetter(AxisRotation::yAxis),
			Codec.DOUBLE.fieldOf("z_axis").forGetter(AxisRotation::zAxis)
			).apply(instance, AxisRotation::new));
	
	public AxisRotation(boolean inDegrees, double xAxis, double yAxis, double zAxis)
	{
		this.inDegrees = inDegrees;
		
		if(inDegrees)
		{
			this.xAxis = Math.toRadians(xAxis);
			this.yAxis = Math.toRadians(yAxis);
			this.zAxis = Math.toRadians(zAxis);
		}
		else
		{
			this.xAxis = xAxis;
			this.yAxis = yAxis;
			this.zAxis = zAxis;
		}
		
		setupQuaternions();
	}
	
	private void setupQuaternions()
	{
		Quaterniond quaterniondX = rotationdX(this.xAxis);
		Quaterniond quaterniondY = new Quaterniond().rotationY(this.yAxis);
		Quaterniond quaterniondZ = new Quaterniond().rotationZ(this.zAxis);
		
		quaterniond = quaterniondX.mul(quaterniondZ.mul(quaterniondY));
		
		Quaternionf quaternionfX = new Quaternionf().rotationX((float) this.xAxis);
		Quaternionf quaternionfY = new Quaternionf().rotationY((float) this.yAxis);
		Quaternionf quaternionfZ = new Quaternionf().rotationZ((float) this.zAxis);
		
		Quaternionf quatf = quaternionfZ.mul(quaternionfY);
		quaternionf = quaternionfX.mul(quatf);
	}
	
	// Sooooooo... Quaterniond is kinda wrong, so here's a custom function for rotating around X-axis that works properly
	private Quaterniond rotationdX(double angle)
	{
		Quaterniond quaternion = new Quaterniond();
		
		double sin = Math.sin(angle * 0.5);
        double cos = Math.cosFromSin(sin, angle * 0.5);
        return quaternion.set(sin, 0, 0, cos);
	}
	
	/**
	 * 
	 * @param xAxis Rotation around the X-axis (in degrees)
	 * @param yAxis Rotation around the Y-axis (in degrees)
	 * @param zAxis Rotation around the Z-axis (in degrees)
	 */
	public AxisRotation(double xAxis, double yAxis, double zAxis)
	{
		this(true, xAxis, yAxis, zAxis);
	}
	
	public AxisRotation()
	{
		this(false, 0, 0, 0);
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
	
	public Quaternionf quaternionf()
	{
		return quaternionf;
	}
	
	public Quaterniond quaterniond()
	{
		return quaterniond;
	}
	
	public AxisRotation add(AxisRotation other)
	{
		return new AxisRotation(false, this.xAxis + other.xAxis, this.yAxis + other.yAxis, this.zAxis + other.zAxis);
	}
	
	public AxisRotation add(double xRot, double yRot, double zRot)
	{
		return new AxisRotation(false, this.xAxis + xRot, this.yAxis + yRot, this.zAxis + zRot);
	}
	
	public AxisRotation sub(AxisRotation other)
	{
		return new AxisRotation(false, this.xAxis - other.xAxis, this.yAxis - other.yAxis, this.zAxis - other.zAxis);
	}
	
	public AxisRotation copy()
	{
		return new AxisRotation(false, xAxis, yAxis, zAxis);
	}
	
	@Override
	public String toString()
	{
		return "( xAxis: " + Math.toDegrees(xAxis) + "°, yAxis: " + Math.toDegrees(yAxis) + "°, zAxis: " + Math.toDegrees(zAxis) + "° )";
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putDouble(X_AXIS, xAxis);
		tag.putDouble(Y_AXIS, yAxis);
		tag.putDouble(Z_AXIS, zAxis);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		inDegrees = false;
		xAxis = tag.getDouble(X_AXIS);
		yAxis = tag.getDouble(Y_AXIS);
		zAxis = tag.getDouble(Z_AXIS);
		
		setupQuaternions();
	}
}
