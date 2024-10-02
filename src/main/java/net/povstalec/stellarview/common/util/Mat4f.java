package net.povstalec.stellarview.common.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

/**
 * Super basic implementation of float 4x4 matrix
 */
public class Mat4f
{
	private static final NumberFormat FORMAT = new DecimalFormat("0.###E0");
	
	float m00, m01, m02, m03;
    float m10, m11, m12, m13;
    float m20, m21, m22, m23;
    float m30, m31, m32, m33;
    
    public Mat4f()
    {
    	this.m00 = 1;
    	this.m11 = 1;
    	this.m22 = 1;
    	this.m33 = 1;
    }
    
    public Mat4f scale(float x, float y, float z)
    {
    	this.m00 *= x;
    	this.m01 *= x;
    	this.m02 *= x;
    	this.m03 *= x;

    	this.m10 *= y;
    	this.m11 *= y;
    	this.m12 *= y;
    	this.m13 *= y;

    	this.m20 *= z;
    	this.m21 *= z;
    	this.m22 *= z;
    	this.m23 *= z;
    	
    	return this;
    }
    
    public Mat4f translate(Vector3f vec)
    {
    	float m30 = this.m30, m31 = this.m31, m32 = this.m32, m33 = this.m33;
    	
    	this.m30 = Math.fma(m00, vec.x(), Math.fma(m10, vec.y(), Math.fma(m20, vec.z(), m30)));
    	this.m31 = Math.fma(m01, vec.x(), Math.fma(m11, vec.y(), Math.fma(m21, vec.z(), m31)));
    	this.m32 = Math.fma(m02, vec.x(), Math.fma(m12, vec.y(), Math.fma(m22, vec.z(), m32)));
    	this.m33 = Math.fma(m03, vec.x(), Math.fma(m13, vec.y(), Math.fma(m23, vec.z(), m33)));
        
        return this;
    }
    
    public Mat4f rotate(Quaternion quat)
    {
    	float m00 = this.m00, m01 = this.m01, m02 = this.m02, m03 = this.m03;
    	float m10 = this.m10, m11 = this.m11, m12 = this.m12, m13 = this.m13;
    	float m20 = this.m20, m21 = this.m21, m22 = this.m22, m23 = this.m23;
    	
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
        float nm03 = this.m03 * rm00 + this.m13 * rm01 + this.m23 * rm02;
        
        float nm10 = this.m00 * rm10 + this.m10 * rm11 + this.m20 * rm12;
        float nm11 = this.m01 * rm10 + this.m11 * rm11 + this.m21 * rm12;
        float nm12 = this.m02 * rm10 + this.m12 * rm11 + this.m22 * rm12;
        float nm13 = this.m03 * rm10 + this.m13 * rm11 + this.m23 * rm12;
        
        this.m00 = nm00;
        this.m01 = nm01;
        this.m02 = nm02;
        this.m03 = nm03;

        this.m10 = nm10;
        this.m11 = nm11;
        this.m12 = nm12;
        this.m13 = nm13;
        
        this.m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
        this.m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
        this.m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
        this.m23 = m03 * rm20 + m13 * rm21 + m23 * rm22;
    	
    	return this;
    }
    
    public Mat4f mul(Mat4f right)
    {
    	float nm00 = Math.fma(m00, right.m00, Math.fma(m10, right.m01, Math.fma(m20, right.m02, m30 * right.m03)));
        float nm01 = Math.fma(m01, right.m00, Math.fma(m11, right.m01, Math.fma(m21, right.m02, m31 * right.m03)));
        float nm02 = Math.fma(m02, right.m00, Math.fma(m12, right.m01, Math.fma(m22, right.m02, m32 * right.m03)));
        float nm03 = Math.fma(m03, right.m00, Math.fma(m13, right.m01, Math.fma(m23, right.m02, m33 * right.m03)));
        
        float nm10 = Math.fma(m00, right.m10, Math.fma(m10, right.m11, Math.fma(m20, right.m12, m30 * right.m13)));
        float nm11 = Math.fma(m01, right.m10, Math.fma(m11, right.m11, Math.fma(m21, right.m12, m31 * right.m13)));
        float nm12 = Math.fma(m02, right.m10, Math.fma(m12, right.m11, Math.fma(m22, right.m12, m32 * right.m13)));
        float nm13 = Math.fma(m03, right.m10, Math.fma(m13, right.m11, Math.fma(m23, right.m12, m33 * right.m13)));
        
        float nm20 = Math.fma(m00, right.m20, Math.fma(m10, right.m21, Math.fma(m20, right.m22, m30 * right.m23)));
        float nm21 = Math.fma(m01, right.m20, Math.fma(m11, right.m21, Math.fma(m21, right.m22, m31 * right.m23)));
        float nm22 = Math.fma(m02, right.m20, Math.fma(m12, right.m21, Math.fma(m22, right.m22, m32 * right.m23)));
        float nm23 = Math.fma(m03, right.m20, Math.fma(m13, right.m21, Math.fma(m23, right.m22, m33 * right.m23)));
        
        float nm30 = Math.fma(m00, right.m30, Math.fma(m10, right.m31, Math.fma(m20, right.m32, m30 * right.m33)));
        float nm31 = Math.fma(m01, right.m30, Math.fma(m11, right.m31, Math.fma(m21, right.m32, m31 * right.m33)));
        float nm32 = Math.fma(m02, right.m30, Math.fma(m12, right.m31, Math.fma(m22, right.m32, m32 * right.m33)));
        float nm33 = Math.fma(m03, right.m30, Math.fma(m13, right.m31, Math.fma(m23, right.m32, m33 * right.m33)));
        
        this.m00 = nm00;
        this.m01 = nm01;
        this.m02 = nm02;
        this.m03 = nm03;

        this.m10 = nm10;
        this.m11 = nm11;
        this.m12 = nm12;
        this.m13 = nm13;

        this.m20 = nm20;
        this.m21 = nm21;
        this.m22 = nm22;
        this.m23 = nm23;

        this.m30 = nm30;
        this.m31 = nm31;
        this.m32 = nm32;
        this.m33 = nm33;
        
        return this;
    }
    
    public Vector3f mulProject(Vector3f vec)
    {
    	float x = vec.x(), y = vec.y(), z = vec.z();
    	
        float invW = 1.0f / Math.fma(m03, x, Math.fma(m13, y, Math.fma(m23, z, m33)));
        
        vec.set(Math.fma(m00, x, Math.fma(m10, y, Math.fma(m20, z, m30))) * invW,
        		Math.fma(m01, x, Math.fma(m11, y, Math.fma(m21, z, m31))) * invW,
        		Math.fma(m02, x, Math.fma(m12, y, Math.fma(m22, z, m32))) * invW);
        
        return vec;
    }
    
    @Override
    public String toString()
    {
    	 return FORMAT.format(m00) + " " + FORMAT.format(m10) + " " + FORMAT.format(m20) + " " + FORMAT.format(m30) + "\n"
                 + FORMAT.format(m01) + " " + FORMAT.format(m11) + " " + FORMAT.format(m21) + " " + FORMAT.format(m31) + "\n"
                 + FORMAT.format(m02) + " " + FORMAT.format(m12) + " " + FORMAT.format(m22) + " " + FORMAT.format(m32) + "\n"
                 + FORMAT.format(m03) + " " + FORMAT.format(m13) + " " + FORMAT.format(m23) + " " + FORMAT.format(m33) + "\n";
    }
}
