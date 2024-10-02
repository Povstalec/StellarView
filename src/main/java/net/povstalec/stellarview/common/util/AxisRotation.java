package net.povstalec.stellarview.common.util;

import com.mojang.math.Quaternion;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AxisRotation
{
	private boolean inDegrees;
	
	private double xAxis;
	private double yAxis;
	private double zAxis;
	
	private Quaterniond quaterniond;
	private Quaternion quaternionf;
	
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
		
		Quaterniond quaterniondX = new Quaterniond().rotationX(this.xAxis);
		Quaterniond quaterniondY = new Quaterniond().rotationY(this.yAxis);
		Quaterniond quaterniondZ = new Quaterniond().rotationZ(this.zAxis);
		
		quaterniond = quaterniondX.mul(quaterniondZ.mul(quaterniondY));
		
		Quaternion quaternionfX = rotationX(new Quaternion(0, 0, 0, 1), (float) this.xAxis);
		Quaternion quaternionfY = rotationY(new Quaternion(0, 0, 0, 1), (float) this.yAxis);
		Quaternion quaternionfZ = rotationZ(new Quaternion(0, 0, 0, 1), (float) this.zAxis);
		
		Quaternion quatf = quaternionfZ;
		quatf.mul(quaternionfY);
		quaternionfX.mul(quatf);
		quaternionf = quaternionfX;
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
	
	public Quaternion quaternionf()
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
	
	private static Quaternion rotationX(Quaternion quaternion, double angle)
	{
		float sin = (float) Math.sin(angle * 0.5);
		float cos = (float) Quaterniond.cosFromSin(sin, angle * 0.5);
		
		quaternion.set(sin, quaternion.j(), quaternion.k(), cos);
		
		return quaternion;
	}
	
	private static Quaternion rotationY(Quaternion quaternion, double angle)
	{
		float sin = (float) Math.sin(angle * 0.5);
		float cos = (float) Quaterniond.cosFromSin(sin, angle * 0.5);
		
		quaternion.set(quaternion.i(), sin, quaternion.k(), cos);
		
		return quaternion;
	}
    
	private static Quaternion rotationZ(Quaternion quaternion, double angle)
	{
		float sin = (float) Math.sin(angle * 0.5);
		float cos = (float) Quaterniond.cosFromSin(sin, angle * 0.5);
		
		quaternion.set(quaternion.i(), quaternion.j(), sin, cos);
		
		return quaternion;
	}
	
	public static Quaternion invertQuaternion(Quaternion quaternion, Quaternion destination)
	{
		float normalized = 1F / lengthSquared(quaternion);
		float x = quaternion.i(), y = quaternion.j(), z = quaternion.k(), w = quaternion.r();
		
		destination.set(-x * normalized, -y * normalized, -z * normalized, w * normalized);
		
		return destination;
	}
	
	private static float lengthSquared(Quaternion quaternion)
	{
		return (float) Math.fma(quaternion.i(), quaternion.i(), Math.fma(quaternion.j(), quaternion.j(), Math.fma(quaternion.k(), quaternion.k(), quaternion.r() * quaternion.r())));
	}
}
