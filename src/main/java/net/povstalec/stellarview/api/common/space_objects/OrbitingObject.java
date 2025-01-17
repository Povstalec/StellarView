package net.povstalec.stellarview.api.common.space_objects;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.config.GeneralConfig;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class OrbitingObject extends TexturedObject
{
	public static final Vector3f INITIAL_ORBIT_VECTOR = new Vector3f(-1, 0, 0);
	
	@Nullable
	private OrbitInfo orbitInfo;
	
	public static final Codec<OrbitingObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(OrbitingObject::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(OrbitingObject::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(object -> Optional.ofNullable(object.orbitInfo)),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(OrbitingObject::getTextureLayers),
			
			FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(OrbitingObject::getFadeOutHandler)
			).apply(instance, OrbitingObject::new));
	
	public OrbitingObject() {}
	
	public OrbitingObject(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo,
			List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, textureLayers, fadeOutHandler);
		
		if(orbitInfo.isPresent())
			this.orbitInfo = orbitInfo.get();
	}
	
	@Nullable
	public OrbitInfo orbitInfo()
	{
		return orbitInfo;
	}
	
	public void setupSynodicOrbit(@Nullable OrbitalPeriod parentOrbitalPeriod)
	{
		if(orbitInfo() != null)
		{
			orbitInfo().orbitalPeriod().updateFromParentPeriod(parentOrbitalPeriod);
			orbitInfo().setupSweep();
			
			for(SpaceObject child : children)
			{
				if(child instanceof OrbitingObject orbitingObject)
					orbitingObject.setupSynodicOrbit(orbitInfo().orbitalPeriod());
			}
		}
		else
		{
			for(SpaceObject child : children)
			{
				if(child instanceof OrbitingObject orbitingObject)
					orbitingObject.setupSynodicOrbit(null);
			}
		}
	}
	
	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		
		orbitInfo = null; //TODO
	}
	
	
	
	public static class OrbitalPeriod
	{
		private long ticks;
		private double orbits; // The number of full orbital revolutions the object will complete in a given number of ticks
		private boolean synodic;
		
		private double frequency;
		
		public static final Codec<OrbitalPeriod> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("ticks").forGetter(OrbitalPeriod::ticks),
				Codec.doubleRange(Double.MIN_NORMAL, Double.MAX_VALUE).optionalFieldOf("orbits", 1D).forGetter(OrbitalPeriod::orbits),
				Codec.BOOL.optionalFieldOf("synodic", false).forGetter(OrbitalPeriod::synodic)
				).apply(instance, OrbitalPeriod::new));
		
		public OrbitalPeriod(long ticks, double orbits, boolean synodic)
		{
			if(ticks <= 0)
				throw(new IllegalArgumentException("Value ticks outside of range [" + 1 + ':' + Integer.MAX_VALUE + ']'));
			
			this.ticks = ticks;
			this.orbits = orbits;
			
			this.synodic = synodic;
			
			this.frequency = orbits / ticks;
		}
		
		public void updateFromParentPeriod(OrbitalPeriod parentPeriod)
		{
			if(!synodic)
				return;
			
			this.ticks = parentPeriod.ticks;
			this.orbits = this.frequency * this.ticks + 1;
			
			this.frequency = this.orbits / this.ticks;
			
			this.synodic = false;
		}
		
		public long ticks()
		{
			return ticks;
		}
		
		public double orbits()
		{
			return orbits;
		}
		
		public boolean synodic()
		{
			return synodic;
		}
		
		public double frequency()
		{
			return frequency;
		}
	}
	
	public static class OrbitInfo
	{
		public static final Codec<OrbitInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.floatRange(1, Float.MAX_VALUE).fieldOf("apoapsis").forGetter(OrbitInfo::apoapsis),
				Codec.floatRange(1, Float.MAX_VALUE).fieldOf("periapsis").forGetter(OrbitInfo::periapsis),
				
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("orbit_clamp_distance", 0F).forGetter(OrbitInfo::orbitClampNumber),
				
				OrbitalPeriod.CODEC.fieldOf("orbital_period").forGetter(OrbitInfo::orbitalPeriod),
				
				Codec.FLOAT.optionalFieldOf("argument_of_periapsis", 0F).forGetter(OrbitInfo::argumentOfPeriapsis),
				
				Codec.FLOAT.optionalFieldOf("inclination", 0F).forGetter(OrbitInfo::inclination),
				Codec.FLOAT.optionalFieldOf("longtitude_of_ascending_node", 0F).forGetter(OrbitInfo::longtitudeOfAscendingNode),
				
				Codec.FLOAT.optionalFieldOf("epoch_mean_anomaly", 0F).forGetter(OrbitInfo::epochMeanAnomaly)
				).apply(instance, OrbitInfo::new));
		
		private final float apoapsis;
		private final float periapsis;
		private final float orbitClampDistance; // Visually clamps the orbit as if it was viewed from this distance
		
		private final OrbitalPeriod orbitalPeriod;
		
		private final float argumentOfPeriapsis;
		
		private final float inclination;
		private final float longtitudeOfAscendingNode;
		
		private final float epochMeanAnomaly;
		
		private float sweep;
		
		private final float eccentricity;
		
		private final Matrix4f orbitMatrix;
		
		public OrbitInfo(float apoapsis, float periapsis, float orbitClampDistance,
				OrbitalPeriod orbitalPeriod,
				float argumentOfPeriapsis,
				float inclination, float longtitudeOfAscendingNode,
				float meanAnomaly)
		{
			this.apoapsis = apoapsis;
			this.periapsis = periapsis;
			this.orbitClampDistance = orbitClampDistance;
			
			this.orbitalPeriod = orbitalPeriod;

			this.argumentOfPeriapsis = (float) Math.toRadians(argumentOfPeriapsis);
			
			this.inclination = (float) Math.toRadians(inclination);
			this.longtitudeOfAscendingNode = (float) Math.toRadians(longtitudeOfAscendingNode);
			
			this.epochMeanAnomaly = (float) Math.toRadians(meanAnomaly);
			setupSweep();
			
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
		
		public float orbitClampNumber()
		{
			return orbitClampDistance;
		}
		
		public void setupSweep()
		{
			this.sweep = (float) ((2 * Math.PI) * orbitalPeriod().frequency());;
		}
		
		public OrbitalPeriod orbitalPeriod()
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
		
		public Vector3f getOrbitVector(long ticks, float partialTicks)
		{
			Vector3f orbitVector = new Vector3f(INITIAL_ORBIT_VECTOR);
			
			float trueAnomaly = (float) eccentricAnomaly(ticks, partialTicks);
			
			orbitVector.mulProject(movementMatrix(trueAnomaly));
			orbitVector.mulProject(orbitMatrix);
			
			return orbitVector;
		}
		
		public Vector3f getOrbitVector(long ticks, float partialTicks, double distance)
		{
			if(orbitClampDistance > 0 && distance > orbitClampDistance)
			{
				float mul = (float) distance / orbitClampDistance;
				
				return getOrbitVector(ticks, partialTicks).mulProject(new Matrix4f().scale(mul, mul, mul));
			}
			
			return getOrbitVector(ticks, partialTicks);
		}
		
		public double meanAnomaly(long ticks, float partialTicks)
		{
			return epochMeanAnomaly + sweep * (ticks - GeneralConfig.tick_multiplier.get() + partialTicks);
		}
		
		public double eccentricAnomaly(long ticks, float partialTicks)
		{
			return approximateEccentricAnomaly(eccentricity, meanAnomaly(ticks % orbitalPeriod().ticks(), partialTicks), 4); // 4 chosen as an arbitrary number
		}
		
		// Moves a point along a unit circle, starting from the mean anomaly
		public Matrix4f movementMatrix(float orbitProgress)
		{
			return new Matrix4f().rotate(Axis.YP.rotation(orbitProgress));
		}
		
		// Reference direction is positive X axis and reference plane is the XZ plane
		public Matrix4f orbitMatrix()
		{
			// Radius of a circle with the diameter of apoapsis + periapsis
			float semiMajorAxis = (apoapsis + periapsis) / 2;
			
			// Scale to the correct size
			Matrix4f scaleMatrix = new Matrix4f().scale(semiMajorAxis, semiMajorAxis, semiMajorAxis);
			
			// Make the orbit eccentric
			Matrix4f eccentricityMatrix = new Matrix4f().scale(1, 1, 1 - eccentricity);
			
			// Offset the orbit to make periapsis closer to whatever it's orbiting around
			Matrix4f offsetMatrix = new Matrix4f().translate(new Vector3f(semiMajorAxis - periapsis, 0, 0));
			
			// Rotate to push the periapsis into the correct position
			Matrix4f periapsisMatrix = new Matrix4f().rotate(Axis.YP.rotation(argumentOfPeriapsis));
			
			Matrix4f inclinationMatrix = new Matrix4f().rotate(Axis.ZP.rotation(inclination));
			
			Matrix4f ascensionMatrix = new Matrix4f().rotate(Axis.YP.rotation(longtitudeOfAscendingNode));
			
			return ascensionMatrix.mul(inclinationMatrix).mul(periapsisMatrix).mul(offsetMatrix).mul(eccentricityMatrix).mul(scaleMatrix);
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
