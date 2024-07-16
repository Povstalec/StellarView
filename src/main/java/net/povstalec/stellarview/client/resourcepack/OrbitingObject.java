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
				Codec.FLOAT.fieldOf("apoapsis").forGetter(OrbitInfo::apoapsis),
				Codec.FLOAT.fieldOf("periapsis").forGetter(OrbitInfo::periapsis),
				
				Codec.FLOAT.fieldOf("apoapsis_velocity").forGetter(OrbitInfo::periapsisVelocity),
				Codec.FLOAT.fieldOf("periapsis_velocity").forGetter(OrbitInfo::apoapsisVelocity),
				
				Codec.FLOAT.optionalFieldOf("argument_of_periapsis", 0F).forGetter(OrbitInfo::argumentOfPeriapsis),
				
				Codec.FLOAT.optionalFieldOf("inclination", 0F).forGetter(OrbitInfo::inclination),
				Codec.FLOAT.optionalFieldOf("longtitude_of_ascending_node", 0F).forGetter(OrbitInfo::longtitudeOfAscendingNode),
				
				Codec.FLOAT.optionalFieldOf("mean_anomaly", 0f).forGetter(OrbitInfo::meanAnomaly),
				Codec.floatRange(0, 1).optionalFieldOf("eccentricity", 0f).forGetter(OrbitInfo::eccentricity)
				).apply(instance, OrbitInfo::new));
		
		private final float apoapsis;
		private final float periapsis;
		
		private final float apoapsisVelocity;
		private final float periapsisVelocity;
		
		private final float argumentOfPeriapsis;
		
		private final float inclination;
		private final float longtitudeOfAscendingNode;
		
		private final float meanAnomaly;
		private final float eccentricity;
		
		private final Matrix4f orbitMatrix;
		
		public OrbitInfo(float apoapsis, float periapsis,
				float apoapsisVelocity, float periapsisVelocity,
				float argumentOfPeriapsis,
				float inclination, float longtitudeOfAscendingNode,
				float meanAnomaly, float eccentricity)
		{
			this.apoapsis = apoapsis;
			this.periapsis = periapsis;
			
			this.apoapsisVelocity = apoapsisVelocity;
			this.periapsisVelocity = periapsisVelocity;

			this.argumentOfPeriapsis = argumentOfPeriapsis;
			
			this.inclination = inclination;
			this.longtitudeOfAscendingNode = longtitudeOfAscendingNode;
			
			this.meanAnomaly = meanAnomaly;
			this.eccentricity = eccentricity;
			
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
		
		public float apoapsisVelocity()
		{
			return apoapsisVelocity;
		}
		
		public float periapsisVelocity()
		{
			return periapsisVelocity;
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
		
		public float meanAnomaly()
		{
			return meanAnomaly;
		}
		
		public float eccentricity()
		{
			return eccentricity;
		}
		
		public Vector3f getOrbitVector(long ticks)
		{
			Vector3f orbitVector = new Vector3f(INITIAL_ORBIT_VECTOR);
			
			orbitVector = orbitVector.mulProject(movementMatrix(getOrbitProgress(ticks)).mul(orbitMatrix));
			
			//System.out.println(orbitVector);
			return orbitVector;
		}
		
		public float getOrbitProgress(long ticks)
		{
			//System.out.println(ticks);
			return (float) (2 * Math.PI * (ticks / 24000F)); //TODO
		}
		
		// Moves a point along a unit circle, starting from the mean anomaly
		public Matrix4f movementMatrix(float orbitProgress)
		{
			return new Matrix4f().rotate(Axis.YP.rotation(meanAnomaly + orbitProgress));
		}
		
		// Reference direction is positive X axis and reference plane is the XZ plane
		public Matrix4f orbitMatrix()
		{
			// Radius of a circle with the diameter of apoapsis + periapsis
			float radius = (apoapsis + periapsis) / 2;
			
			Matrix4f scaleMatrix = new Matrix4f().scale(radius, radius, radius);
			
			Matrix4f eccentricityMatrix = new Matrix4f().scale(1, 1, 1 - eccentricity);
			
			Matrix4f offsetMatrix = new Matrix4f().translate(new Vector3f(radius - periapsis, 0, 0));
			
			Matrix4f inclinationMatrix = new Matrix4f().rotate(Axis.ZP.rotation(inclination));
			
			Matrix4f ascensionMatrix = new Matrix4f().rotate(Axis.YP.rotation(longtitudeOfAscendingNode));
			
			return ascensionMatrix.mul(inclinationMatrix).mul(offsetMatrix).mul(eccentricityMatrix).mul(scaleMatrix);
		}
		
		public Matrix4f getOrbitMatrix()
		{
			return orbitMatrix;
		}
	}
}
