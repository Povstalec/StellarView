package net.povstalec.stellarview.common.util;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Super basic implementation of float 4x4 matrix
 */
public class Mat3f
{
	private static final NumberFormat FORMAT = new DecimalFormat("0.###E0");
	
	float m00, m01, m02;
    float m10, m11, m12;
    float m20, m21, m22;
    
    public Mat3f()
    {
    	this.m00 = 1;
    	this.m11 = 1;
    	this.m22 = 1;
    }
    
    public Mat3f scale(float x, float y, float z)
    {
    	this.m00 *= x;
    	this.m01 *= x;
    	this.m02 *= x;

    	this.m10 *= y;
    	this.m11 *= y;
    	this.m12 *= y;

    	this.m20 *= z;
    	this.m21 *= z;
    	this.m22 *= z;
    	
    	return this;
    }
    
    public Mat3f rotate(Quaternion quat)
    {
    	float m00 = this.m00, m01 = this.m01, m02 = this.m02;
    	float m10 = this.m10, m11 = this.m11, m12 = this.m12;
    	float m20 = this.m20, m21 = this.m21, m22 = this.m22;
    	
    	float w2 = quat.r() * quat.r(), x2 = quat.i() * quat.i();
        float y2 = quat.j() * quat.j(), z2 = quat.k() * quat.k();
        float zw = quat.k() * quat.r(), dzw = zw + zw, xy = quat.i() * quat.j(), dxy = xy + xy;
        float xz = quat.i() * quat.k(), dxz = xz + xz, yw = quat.j() * quat.r(), dyw = yw + yw;
        float yz = quat.j() * quat.k(), dyz = yz + yz, xw = quat.i() * quat.r(), dxw = xw + xw;
        
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        
        float rm10 = -dzw + dxy;
        float rm11 = y2 - z2 + w2 - x2;
        float rm12 = dyz + dxw;
        
        float rm20 = dyw + dxz;
        float rm21 = dyz - dxw;
        float rm22 = z2 - y2 - x2 + w2;
        
        float nm00 = this.m00 * rm00 + this.m10 * rm01 + this.m20 * rm02;
        float nm01 = this.m01 * rm00 + this.m11 * rm01 + this.m21 * rm02;
        float nm02 = this.m02 * rm00 + this.m12 * rm01 + this.m22 * rm02;
        
        float nm10 = this.m00 * rm10 + this.m10 * rm11 + this.m20 * rm12;
        float nm11 = this.m01 * rm10 + this.m11 * rm11 + this.m21 * rm12;
        float nm12 = this.m02 * rm10 + this.m12 * rm11 + this.m22 * rm12;
        
        this.m00 = nm00;
        this.m01 = nm01;
        this.m02 = nm02;

        this.m10 = nm10;
        this.m11 = nm11;
        this.m12 = nm12;
        
        this.m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
        this.m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
        this.m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
    	
    	return this;
    }
	
	public Matrix3f toMatrix3f()
	{
		float[] values = new float[] {m00, m01, m02, 0, m10, m11, m12, 0, m20, m21, m22, 0, 0, 0, 0, 1};
		// Words cannot describe how much I dislike having to do this instead of being able to set the freaking values directly, what a pain
		return new Matrix3f(new Matrix4f(values));
	}
    
    @Override
    public String toString()
    {
    	 return FORMAT.format(m00) + " " + FORMAT.format(m10) + " " + FORMAT.format(m20) + "\n"
                 + FORMAT.format(m01) + " " + FORMAT.format(m11) + " " + FORMAT.format(m21) + "\n"
                 + FORMAT.format(m02) + " " + FORMAT.format(m12) + " " + FORMAT.format(m22) + "\n";
    }
}
