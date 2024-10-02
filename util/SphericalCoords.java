package net.povstalec.stellarview.common.util;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class SphericalCoords
{
	public double r;
	public double theta;
	public double phi;
	
	public SphericalCoords(double r, double theta, double phi)
	{
		this.r = r;
		this.theta = theta;
		this.phi = phi;
	}
	
	public SphericalCoords(Vector3f cartesianCoords)
	{
		this.r = sphericalR(cartesianCoords);
		this.theta = sphericalTheta(cartesianCoords);
		this.phi = sphericalPhi(cartesianCoords);
	}
	
	public SphericalCoords(Vector3f cartesianCoords, float r)
	{
		this.r = r;
		this.theta = sphericalTheta(cartesianCoords);
		this.phi = sphericalPhi(cartesianCoords);
	}
	
	public SphericalCoords(Vector3d cartesianCoords)
	{
		this.r = sphericalR(cartesianCoords);
		this.theta = sphericalTheta(cartesianCoords);
		this.phi = sphericalPhi(cartesianCoords);
	}
	
	public SphericalCoords(Vector3d cartesianCoords, double r)
	{
		this.r = r;
		this.theta = sphericalTheta(cartesianCoords);
		this.phi = sphericalPhi(cartesianCoords);
	}
	
	public Vector3f toCartesianF()
	{
		return new Vector3f((float) cartesianX(this), (float) cartesianY(this), (float) cartesianZ(this));
	}
	
	public Vector3d toCartesianD()
	{
		return new Vector3d(cartesianX(this), cartesianY(this), cartesianZ(this));
	}
	
	public static SphericalCoords cartesianToSpherical(Vector3f cartesianCoordinates)
	{
		return new SphericalCoords(cartesianCoordinates);
	}
	
	public static SphericalCoords cartesianToSpherical(Vector3d cartesianCoordinates)
	{
		return new SphericalCoords(cartesianCoordinates);
	}
	
	
	
	public static float sphericalR(Vector3f cartesianCoords)
	{
		return (float) Math.sqrt(cartesianCoords.x * cartesianCoords.x + cartesianCoords.y * cartesianCoords.y + cartesianCoords.z * cartesianCoords.z);
	}
	
	public static float sphericalTheta(Vector3f cartesianCoords)
	{
		return (float) Math.atan2(cartesianCoords.x, cartesianCoords.z);
	}
	
	public static float sphericalPhi(Vector3f cartesianCoords)
	{
		double xzLength = Math.sqrt(cartesianCoords.x * cartesianCoords.x + cartesianCoords.z * cartesianCoords.z);
		return (float) Math.atan2(xzLength, cartesianCoords.y);
	}
	
	public static double sphericalR(Vector3d cartesianCoords)
	{
		return Math.sqrt(cartesianCoords.x * cartesianCoords.x + cartesianCoords.y * cartesianCoords.y + cartesianCoords.z * cartesianCoords.z);
	}
	
	public static double sphericalTheta(Vector3d cartesianCoords)
	{
		return Math.atan2(cartesianCoords.x, cartesianCoords.z);
	}
	
	public static double sphericalPhi(Vector3d cartesianCoords)
	{
		double xzLength = Math.sqrt(cartesianCoords.x * cartesianCoords.x + cartesianCoords.z * cartesianCoords.z);
		return Math.atan2(xzLength, cartesianCoords.y);
	}
	
	
	
	public static double cartesianX(SphericalCoords sphericalCoords)
	{
		return sphericalCoords.r * Math.sin(sphericalCoords.phi) * Math.sin(sphericalCoords.theta);
	}
	
	public static double cartesianY(SphericalCoords sphericalCoords)
	{
		return sphericalCoords.r * Math.cos(sphericalCoords.phi);
	}
	
	public static double cartesianZ(SphericalCoords sphericalCoords)
	{
		return sphericalCoords.r * Math.sin(sphericalCoords.phi) * Math.cos(sphericalCoords.theta);
	}
	
	
	
	public static Vector3f sphericalToCartesianF(SphericalCoords sphericalCoords)
	{
		return new Vector3f((float) cartesianX(sphericalCoords), (float) cartesianY(sphericalCoords), (float) cartesianZ(sphericalCoords));
	}
	
	public static Vector3d sphericalToCartesianD(SphericalCoords sphericalCoords)
	{
		return new Vector3d(cartesianX(sphericalCoords), cartesianY(sphericalCoords), cartesianZ(sphericalCoords));
	}
	
	@Override
	public String toString()
	{
		return "(r: " + r + ", theta: " + theta + ", phi: " + phi + ")";
	}
}
