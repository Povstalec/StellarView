package net.povstalec.stellarview.client.render.level.misc;

public class StellarCoordinates
{
	//============================================================================================
	//***********************************Cartesian to Spherical***********************************
	//============================================================================================
	
	public static double sphericalR(double x, double y, double z)
	{
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public static double sphericalTheta(double x, double y, double z)
	{
		return Math.atan2(x, z);
	}
	
	public static double sphericalPhi(double x, double y, double z)
	{
		double xzLength = Math.sqrt(x * x + z * z);
		return Math.atan2(xzLength, y);
	}
	
	//============================================================================================
	//***********************************Spherical to Cartesian***********************************
	//============================================================================================
	
	public static double cartesianX(double r, double theta, double phi)
	{
		return r * Math.sin(phi) * Math.sin(theta);
	}
	
	public static double cartesianY(double r, double theta, double phi)
	{
		return r * Math.cos(phi);
	}
	
	public static double cartesianZ(double r, double theta, double phi)
	{
		return r * Math.sin(phi) * Math.cos(theta);
	}
	
	//============================================================================================
	//*******************************************Useful*******************************************
	//============================================================================================
	
	public static double[] moveSpherical(double initialTheta, double initialPhi, double offsetX, double offsetY, double r, double theta, double phi)
	{
		double x = cartesianX(r, theta, phi);
		double y = cartesianY(r, theta, phi);
		double z = cartesianZ(r, theta, phi);
		
		x += - offsetY * Math.cos(phi) * Math.sin(theta) - offsetX * Math.cos(theta);
		y += offsetY * Math.sin(phi);
		z += - offsetY * Math.cos(phi) * Math.cos(theta) + offsetX * Math.sin(theta);
		
		return new double[] {x, y, z};
	}
	
	public static float[] placeOnSphere(float offsetX, float offsetY, float r, double theta, double phi)
	{
		double x = cartesianX(r, theta, phi);
		double y = cartesianY(r, theta, phi);
		double z = cartesianZ(r, theta, phi);
		
		x += - offsetY * Math.cos(phi) * Math.sin(theta) - offsetX * Math.cos(theta);
		y += offsetY * Math.sin(phi);
		z += - offsetY * Math.cos(phi) * Math.cos(theta) + offsetX * Math.sin(theta);
		
		return new float[] {(float) x, (float) y, (float) z};
	}
	
	public static double spiralR(double r, double phi, double beta)
	{
		return r * (phi + beta);
	}
}
