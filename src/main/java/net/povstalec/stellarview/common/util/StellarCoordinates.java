package net.povstalec.stellarview.common.util;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.povstalec.stellarview.common.util.SpaceCoords.SpaceDistance;

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
	
	public static Vector3f placeOnSphere(float offsetX, float offsetY, float r, double theta, double phi, double rotation)
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
		
		return new Vector3f((float) x, (float) y, (float) z);
	}
	
	public static Vector3f placeOnSphere(float offsetX, float offsetY, SphericalCoords sphericalCoords, double rotation)
	{
		Vector3f cartesian = sphericalCoords.toCartesianF();
		
		double polarR = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
		double polarPhi = Math.atan2(offsetY, offsetX);
		polarPhi += rotation;
		
		double polarX = polarR * Math.cos(polarPhi);
		double polarY = polarR * Math.sin(polarPhi);
		
		cartesian.x += - polarY * Math.cos(sphericalCoords.phi) * Math.sin(sphericalCoords.theta) - polarX * Math.cos(sphericalCoords.theta);
		cartesian.y += polarY * Math.sin(sphericalCoords.phi);
		cartesian.z += - polarY * Math.cos(sphericalCoords.phi) * Math.cos(sphericalCoords.theta) + polarX * Math.sin(sphericalCoords.theta);
		
		return cartesian;
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
	
	//============================================================================================
	//****************************************Astronomical****************************************
	//============================================================================================
	
	public static class RightAscension
	{
		public static final String HOURS = "hours";
		public static final String MINUTES = "minutes";
		public static final String SECONDS = "seconds";
		
		public final double hours;
		public final double minutes;
		public final double seconds;
		
		public final double radians;
		
		public static final Codec<RightAscension> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.doubleRange(0, 24).optionalFieldOf(HOURS, 0D).forGetter(rightAscension -> rightAscension.hours),
				Codec.doubleRange(0, 60).optionalFieldOf(MINUTES, 0D).forGetter(rightAscension -> rightAscension.minutes),
				Codec.doubleRange(0, 60).optionalFieldOf(SECONDS, 0D).forGetter(rightAscension -> rightAscension.seconds)
				).apply(instance, RightAscension::new));
		
		public RightAscension(double hours, double minutes, double seconds)
		{
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;
			
			this.radians = toRightAscension(hours, minutes, seconds);
		}
		
		public static double toRightAscension(double hours, double minutes, double seconds)
		{
			return Math.toRadians(360F * (hours / 24F) + 360F * (minutes / 1440F) + 360F * (seconds / 86400F));
		}
		
		@Override
		public String toString()
		{
			return "[" + hours + "h " + minutes + "m " + seconds + "s]";
		}
	}
	
	public static class Declination
	{
		public static final String DEGREES = "degrees";
		public static final String MINUTES = "minutes";
		public static final String SECONDS = "seconds";
		
		public final double degrees;
		public final double minutes;
		public final double seconds;
		
		public final double radians;
		
		public static final Codec<Declination> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.doubleRange(-180, 180).optionalFieldOf(DEGREES, 0D).forGetter(declination -> declination.degrees),
				Codec.doubleRange(-60, 60).optionalFieldOf(MINUTES, 0D).forGetter(declination -> declination.minutes),
				Codec.doubleRange(-60, 60).optionalFieldOf(SECONDS, 0D).forGetter(declination -> declination.seconds)
				).apply(instance, Declination::new));
		
		public Declination(double degrees, double minutes, double seconds)
		{
			this.degrees = degrees;
			
			if(isNegative(degrees))
			{
				this.minutes = -minutes;
				this.seconds = -seconds;
			}
			else
			{
				this.minutes = minutes;
				this.seconds = seconds;
			}
			
			this.radians = toDeclination(this.degrees, this.minutes, this.seconds);
		}
		
		public static boolean isNegative(double degrees)
		{
			return Double.doubleToRawLongBits(degrees) < 0;
		}
		
		public static double toDeclination(double degrees, double minutes, double seconds)
		{
				return Math.toRadians(degrees + minutes / 60F + seconds / 3600F);
		}
		
		@Override
		public String toString()
		{
			return "[" + degrees + "° " + minutes + "' " + seconds + "\"]";
		}
	}
	
	public static class Equatorial
	{
		public static final String RIGHT_ASCENSION = "right_ascension";
		public static final String DECLINATION = "declination";
		public static final String DISTNACE = "distance";
		
		public static final double RIGHT_ASCENSION_NGP = Math.toRadians(192.85948); // Right ascension of the north galactic pole
		public static final double DECLINATION_NGP = Math.toRadians(27.12825); // Declination of the north galactic pole
		public static final double L_NCP = Math.toRadians(122.93192); // Longtitude of the north celestial pole
		
		public final RightAscension rightAscension;
		public final Declination declination;
		public final SpaceDistance distance;
		
		public static final Codec<Equatorial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RightAscension.CODEC.fieldOf(RIGHT_ASCENSION).forGetter(equatorial -> equatorial.rightAscension),
				Declination.CODEC.fieldOf(DECLINATION).forGetter(equatorial -> equatorial.declination),
				SpaceDistance.CODEC.fieldOf(DISTNACE).forGetter(equatorial -> equatorial.distance)
				).apply(instance, Equatorial::new));
		
		public Equatorial(RightAscension rightAscension, Declination declination, SpaceDistance distance)
		{
			this.rightAscension = rightAscension;
			this.declination = declination;
			this.distance = distance;
		}
		
		public Galactic toGalactic()
		{
			return toGalactic(rightAscension.radians, declination.radians, distance);
		}
		
		public static Galactic toGalactic(double rightAscension, double declination, SpaceDistance distance)
		{
			double sinB = Math.sin(DECLINATION_NGP) * Math.sin(declination) + Math.cos(DECLINATION_NGP) * Math.cos(declination) * Math.cos(rightAscension - RIGHT_ASCENSION_NGP);
			
			double galacticLatitude = Math.asin(sinB);
			
			double x1 = Math.cos(declination) * Math.sin(rightAscension - RIGHT_ASCENSION_NGP);
			double x2 = (Math.sin(declination) - Math.sin(DECLINATION_NGP) * galacticLatitude) / Math.cos(DECLINATION_NGP);
			
			double galacticLongtitude = L_NCP - Math.atan2(x1, x2);
			
			return new Galactic(galacticLongtitude, galacticLatitude, distance);
		}
		
		@Override
		public String toString()
		{
			return "{RA: " + rightAscension.toString() + " Dec: " + declination.toString() + " Dist: " + distance.toString() + '}';
		}
	}
	
	public static class Galactic
	{
		public final double galacticLongtitude;
		public final double galacticLatitude;
		public final SpaceDistance distance;
		
		public Galactic(double galacticLongtitude, double galacticLatitude, SpaceDistance distance)
		{
			this.galacticLongtitude = galacticLongtitude;
			this.galacticLatitude = galacticLatitude;
			this.distance = distance;
		}
		
		public SpaceCoords toSpaceCoords()
		{
			double xProj = Math.sin(galacticLongtitude) * Math.cos(galacticLatitude);
			double yProj = Math.sin(galacticLatitude);
			double zProj = Math.cos(galacticLongtitude) * Math.cos(galacticLatitude);
			
			return new SpaceCoords(distance.mul(xProj, false), distance.mul(yProj, false), distance.mul(zProj, false));
		}
		
		@Override
		public String toString()
		{
			return "{Long: " + Math.toDegrees(galacticLongtitude) + "° Lat: " + Math.toDegrees(galacticLatitude) + "° Dist: " + distance.toString() + '}';
		}
	}
}
