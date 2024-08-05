package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class OrbitingObject extends SpaceObject
{
	public static final Vector3f INITIAL_ORBIT_VECTOR = new Vector3f(0, 0, -1);
	
	@Nullable
	private OrbitInfo orbitInfo;
	
	public static final Codec<OrbitingObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(OrbitingObject::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(OrbitingObject::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(OrbitingObject::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(OrbitingObject::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(OrbitingObject::getTextureLayers)
			).apply(instance, OrbitingObject::new));
	
	public OrbitingObject(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
	{
		super(parent, coords, axisRotation, textureLayers);
		
		if(orbitInfo.isPresent())
			this.orbitInfo = orbitInfo.get();
	}
	
	public Optional<OrbitInfo> getOrbitInfo()
	{
		if(orbitInfo != null)
			return Optional.of(orbitInfo);
		
		return Optional.empty();
	}
	
	@Override
	public Vector3f getPosition(long ticks)
	{
		if(orbitInfo != null)
			return orbitInfo.getOrbitVector(ticks);
		else
			return super.getPosition(ticks);
	}
	
	
	
	public static class OrbitInfo
	{
		public static final Codec<OrbitInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.floatRange(1, Float.MAX_VALUE).fieldOf("apoapsis").forGetter(OrbitInfo::apoapsis),
				Codec.floatRange(1, Float.MAX_VALUE).fieldOf("periapsis").forGetter(OrbitInfo::periapsis),
				
				Codec.LONG.fieldOf("orbital_period").forGetter(OrbitInfo::orbitalPeriod),
				
				Codec.FLOAT.optionalFieldOf("argument_of_periapsis", 0F).forGetter(OrbitInfo::argumentOfPeriapsis),
				
				Codec.FLOAT.optionalFieldOf("inclination", 0F).forGetter(OrbitInfo::inclination),
				Codec.FLOAT.optionalFieldOf("longtitude_of_ascending_node", 0F).forGetter(OrbitInfo::longtitudeOfAscendingNode),
				
				Codec.FLOAT.optionalFieldOf("epoch_mean_anomaly", 0f).forGetter(OrbitInfo::epochMeanAnomaly)
				).apply(instance, OrbitInfo::new));
		
		private final float apoapsis;
		private final float periapsis;
		
		private final long orbitalPeriod;
		
		private final float argumentOfPeriapsis;
		
		private final float inclination;
		private final float longtitudeOfAscendingNode;
		
		private final float epochMeanAnomaly;
		
		private final float sweep;
		
		private final float eccentricity;
		
		private final Matrix4f orbitMatrix;
		
		public OrbitInfo(float apoapsis, float periapsis,
				long orbitalPeriod,
				float argumentOfPeriapsis,
				float inclination, float longtitudeOfAscendingNode,
				float meanAnomaly)
		{
			this.apoapsis = apoapsis;
			this.periapsis = periapsis;
			
			this.orbitalPeriod = orbitalPeriod;

			this.argumentOfPeriapsis = (float) Math.toRadians(argumentOfPeriapsis);
			
			this.inclination = (float) Math.toRadians(inclination);
			this.longtitudeOfAscendingNode = (float) Math.toRadians(longtitudeOfAscendingNode);
			
			this.epochMeanAnomaly = (float) Math.toRadians(meanAnomaly);
			this.sweep = (float) ((2 * Math.PI) / orbitalPeriod);
			
			this.eccentricity = (apoapsis - periapsis) / (apoapsis + periapsis);
			
			this.orbitMatrix = orbitMatrix();
		}
		
		public float apoapsis()
		{
			return apoapsis;
		}
		
		public float periapsis()
		{
			return periapsis;
		}
		
		public long orbitalPeriod()
		{
			return orbitalPeriod;
		}
		
		public float argumentOfPeriapsis()
		{
			return argumentOfPeriapsis;
		}
		
		public float inclination()
		{
			return inclination;
		}
		
		public float longtitudeOfAscendingNode()
		{
			return longtitudeOfAscendingNode;
		}
		
		public float epochMeanAnomaly()
		{
			return epochMeanAnomaly;
		}
		
		public float eccentricity()
		{
			return eccentricity;
		}
		
		public Vector3f getOrbitVector(long ticks)
		{
			Vector3f orbitVector = new Vector3f(INITIAL_ORBIT_VECTOR);
			
			float trueAnomaly = (float) eccentricAnomaly(ticks);
			
			orbitVector = orbitVector.mulProject(movementMatrix(trueAnomaly)).mulProject(orbitMatrix);
			
			return orbitVector;
		}
		
		public double meanAnomaly(long ticks)
		{
			return epochMeanAnomaly + sweep * ticks;
		}
		
		public double eccentricAnomaly(long ticks)
		{
			return approximateEccentricAnomaly(eccentricity, meanAnomaly(ticks), 4); // 4 chosen as an arbitrary number
		}
		
		// Moves a point along a unit circle, starting from the mean anomaly
		public Matrix4f movementMatrix(float orbitProgress)
		{
			return new Matrix4f().rotate(Axis.YP.rotation(epochMeanAnomaly + orbitProgress));
		}
		
		// Reference direction is positive X axis and reference plane is the XZ plane
		public Matrix4f orbitMatrix()
		{
			// Radius of a circle with the diameter of apoapsis + periapsis
			float semiMajorAxis = (apoapsis + periapsis) / 2;
			
			Matrix4f scaleMatrix = new Matrix4f().scale(semiMajorAxis, semiMajorAxis, semiMajorAxis);
			
			Matrix4f eccentricityMatrix = new Matrix4f().scale(1, 1, 1 - eccentricity);
			
			Matrix4f offsetMatrix = new Matrix4f().translate(new Vector3f(0, 0, semiMajorAxis - periapsis));
			
			Matrix4f inclinationMatrix = new Matrix4f().rotate(Axis.ZP.rotation(inclination));
			
			Matrix4f ascensionMatrix = new Matrix4f().rotate(Axis.YP.rotation(longtitudeOfAscendingNode));
			
			return ascensionMatrix.mul(inclinationMatrix).mul(offsetMatrix).mul(eccentricityMatrix).mul(scaleMatrix);
		}
		
		public Matrix4f getOrbitMatrix()
		{
			return orbitMatrix;
		}

		/**
		 * Approximate E (Eccentric Anomaly) for a given
		 * e (eccentricity) and M (Mean Anomaly)
		 * where e < 1 and E and M are given in radians
		 * 
		 * This is performed by finding the root of the
		 * function f(E) = E - e*sin(E) - M(t)
		 * via Newton's method, where the derivative of
		 * f(E) with respect to E is 
		 * f'(E) = 1 - e*cos(E)
		 * 
		 * @param eccentricity
		 * @param meanAnomaly
		 * @param iterations
		 * @return
		 */
		public static double approximateEccentricAnomaly(double eccentricity, double meanAnomaly, int iterations)
		{
			double sinMeanAnomaly = Math.sin(meanAnomaly);
			
			double E = meanAnomaly + eccentricity * ( sinMeanAnomaly / (1 - Math.sin(meanAnomaly + eccentricity) + sinMeanAnomaly) );
			
			for (int i = 0; i < iterations; i++)
			{
				E = E - (E - eccentricity * Math.sin(E) - meanAnomaly) / (1 - eccentricity * Math.cos(E));
			}
			return E;
		}
	}
}
