package net.povstalec.stellarview.common.util;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

/**
 * Super basic implementation of double quaternion
 */
public class Quaterniond
{
	private static final double PI_HALF = Math.PI / 2;
	private static final double PI_2 = Math.PI * 2;
	
	public double x, y, z, w;
	
	public Quaterniond()
	{
		this.w = 1F;
	}
	
	public Quaterniond rotationX(double angle)
	{
		double sin = Math.sin(angle * 0.5);
		double cos = cosFromSin(sin, angle * 0.5);
		
		this.x = sin;
		this.w = cos;
		
		return this;
	}
	
	public Quaterniond rotationY(double angle)
	{
		double sin = Math.sin(angle * 0.5);
		double cos = cosFromSin(sin, angle * 0.5);
		
		this.y = sin;
		this.w = cos;
		
		return this;
	}
    
	public Quaterniond rotationZ(double angle)
	{
		double sin = Math.sin(angle * 0.5);
		double cos = cosFromSin(sin, angle * 0.5);
		
		this.z = sin;
		this.w = cos;
		
		return this;
	}
	
	public Vector3d transform(Vector3d vec)
	{
		double xx = this.x * this.x, yy = this.y * this.y, zz = this.z * this.z, ww = this.w * this.w;
		double xy = this.x * this.y, xz = this.x * this.z, yz = this.y * this.z, xw = this.x * this.w;
		double zw = this.z * this.w, yw = this.y * this.w, k = 1 / (xx + yy + zz + ww);
		
		double x = vec.x, y = vec.y, z = vec.z;
		
		vec.x = Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z));
		vec.y = Math.fma(2 * (xy + zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z));
		vec.z = Math.fma(2 * (xz - yw) * k, x, Math.fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z));
		
		return vec;
	}
	
	public Vector3f transform(Vector3f vec)
	{
		double xx = this.x * this.x, yy = this.y * this.y, zz = this.z * this.z, ww = this.w * this.w;
		double xy = this.x * this.y, xz = this.x * this.z, yz = this.y * this.z, xw = this.x * this.w;
		double zw = this.z * this.w, yw = this.y * this.w, k = 1 / (xx + yy + zz + ww);
		
		double x = vec.x(), y = vec.y(), z = vec.z();
		
		vec.setX((float) (Math.fma((xx - yy - zz + ww) * k, x, Math.fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z))));
		vec.setY((float) (Math.fma(2 * (xy + zw) * k, x, Math.fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z))));
		vec.setZ((float) (Math.fma(2 * (xz - yw) * k, x, Math.fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z))));
		
		return vec;
	}
	
	public Quaterniond rotateX(double angle)
	{
		double sin = Math.sin(angle * 0.5);
		double cos = cosFromSin(sin, angle * 0.5);
		double x = this.x, y = this.y, z = this.z, w = this.w;
		
		this.x = w * sin + x * cos;
		this.y = y * cos + z * sin;
		this.z = z * cos - y * sin;
		this.w = w * cos - x * sin;
		
		return this;
	}
	
	public Quaterniond rotateY(double angle)
	{
		double sin = Math.sin(angle * 0.5);
		double cos = cosFromSin(sin, angle * 0.5);
		double x = this.x, y = this.y, z = this.z, w = this.w;
		
		this.x = x * cos - z * sin;
		this.y = w * sin + y * cos;
		this.z = x * sin + z * cos;
		this.w = w * cos - y * sin;
		
		return this;
	}
	
	public Quaterniond rotateZ(double angle)
	{
		double sin = Math.sin(angle * 0.5);
		double cos = cosFromSin(sin, angle * 0.5);
		double x = this.x, y = this.y, z = this.z, w = this.w;
		
		this.x = x * cos + y * sin;
		this.y = y * cos - x * sin;
		this.z = w * sin + z * cos;
		this.w = w * cos - z * sin;
		
		return this;
	}
	
	public Quaterniond mul(Quaterniond other)
	{
		double x = this.x, y = this.y, z = this.z, w = this.w;
		
		this.x = Math.fma(w, other.x, Math.fma(x, other.w, Math.fma(y, other.z, -z * other.y)));
		this.y = Math.fma(w, other.y, Math.fma(-x, other.z, Math.fma(y, other.w, z * other.x)));
		this.z = Math.fma(w, other.z, Math.fma(x, other.y, Math.fma(-y, other.x, z * other.w)));
		this.w = Math.fma(w, other.w, Math.fma(-x, other.x, Math.fma(-y, other.y, -z * other.z)));
		
		return this;
	}
	
	public Quaterniond invert(Quaterniond destination)
	{
		double normalized = 1.0 / lengthSquared();
		
		destination.x = -x * normalized;
		destination.y = -y * normalized;
		destination.z = -z * normalized;
		destination.w = w * normalized;
		
		return destination;
	}
	
	public double lengthSquared()
	{
		return Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w)));
	}
	
	
	
	public static double cosFromSin(double sin, double angle)
	{
        double cos = Math.sqrt(1.0 - sin * sin);
        double a = angle + PI_HALF;
        double b = a - (int)(a / PI_2) * PI_2;
        
        if (b < 0.0)
            b = PI_2 + b;
        
        if (b >= Math.PI)
            return -cos;
        
        return cos;
    }
}
