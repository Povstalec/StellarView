package net.povstalec.stellarview.client.render.level.misc;

import org.joml.Vector3f;

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
	
	public static Vector3f cartesianToSpherical(Vector3f cartesianCoordinates)
	{
		float x = cartesianCoordinates.x;
		float y = cartesianCoordinates.y;
		float z = cartesianCoordinates.z;
		
		return new Vector3f((float) sphericalR(x, y, z), (float) sphericalTheta(x, y, z), (float) sphericalPhi(x, y, z));
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
	
	public static Vector3f sphericalToCartesian(Vector3f sphericalCoordinates)
	{
		float r = sphericalCoordinates.x;
		float theta = sphericalCoordinates.y;
		float phi = sphericalCoordinates.z;
		
		return new Vector3f((float) cartesianX(r, theta, phi), (float) cartesianY(r, theta, phi), (float) cartesianZ(r, theta, phi));
	}
	
	//============================================================================================
	//*******************************************Useful*******************************************
	//============================================================================================
	
	public static double[] moveSpherical(double offsetX, double offsetY, double r, double theta, double phi)
	{
		double x = cartesianX(r, theta, phi);
		double y = cartesianY(r, theta, phi);
		double z = cartesianZ(r, theta, phi);
		
		x += - offsetY * Math.cos(phi) * Math.sin(theta) - offsetX * Math.cos(theta);
		y += offsetY * Math.sin(phi);
		z += - offsetY * Math.cos(phi) * Math.cos(theta) + offsetX * Math.sin(theta);
		
		return new double[] {x, y, z};
	}
	
	public static float[] placeOnSphere(float offsetX, float offsetY, float r, double theta, double phi, double rotation)
	{
		double x = cartesianX(r, theta, phi);
		double y = cartesianY(r, theta, phi);
		double z = cartesianZ(r, theta, phi);
		
		double polarR = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
		double polarPhi = Math.atan2(offsetY, offsetX);
		polarPhi += rotation;
		
		double polarX = polarR * Math.cos(polarPhi);
		double polarY = polarR * Math.sin(polarPhi);
		
		x += - polarY * Math.cos(phi) * Math.sin(theta) - polarX * Math.cos(theta);
		y += polarY * Math.sin(phi);
		z += - polarY * Math.cos(phi) * Math.cos(theta) + polarX * Math.sin(theta);
		
		return new float[] {(float) x, (float) y, (float) z};
	}
	
	public static double spiralR(double r, double phi, double beta)
	{
		return r * (phi + beta);
	}
	
	public static double elipticalR(double a, double b, double phi)
	{
		return (a * b) / (Math.sqrt(b * Math.pow(Math.cos(phi), 2) + a * Math.pow(Math.sin(phi), 2)));
	}
	
	public static Vector3f addVectors(Vector3f vector1, Vector3f vector2)
	{
		return new Vector3f(vector1.x + vector2.x, vector1.y + vector2.y, vector1.z + vector2.z);
	}
	
	public static Vector3f subtractVectors(Vector3f vector1, Vector3f vector2)
	{
		return new Vector3f(vector1.x - vector2.x, vector1.y - vector2.y, vector1.z - vector2.z);
	}
	
	public static Vector3f relativeVector(Vector3f vector1, Vector3f vector2)
	{
		return subtractVectors(vector1, vector2);
	}
	
	public static Vector3f absoluteVector(Vector3f vector1, Vector3f vector2)
	{
		return addVectors(vector1, vector2);
	}
}
