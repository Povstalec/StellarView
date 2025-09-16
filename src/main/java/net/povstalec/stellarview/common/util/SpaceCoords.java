package net.povstalec.stellarview.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.minecraft.nbt.CompoundTag;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class SpaceCoords implements INBTSerializable<CompoundTag>
{
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";
	
	public static final double KM_PER_LY = 9_460_730_472_581.2;
	public static final double MAX_KM_VALUE = KM_PER_LY / 2;
	
	public static final double LIGHT_SPEED = 299_792.458;
	
	public static final SpaceCoords NULL_COORDS = new SpaceCoords();
    
    public static final Codec<SpaceCoords> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		SpaceDistance.CODEC.fieldOf(X).forGetter(SpaceCoords::x),
    		SpaceDistance.CODEC.fieldOf(Y).forGetter(SpaceCoords::y),
    		SpaceDistance.CODEC.fieldOf(Z).forGetter(SpaceCoords::z)
			).apply(instance, SpaceCoords::new));
	
	private SpaceDistance x;
	private SpaceDistance y;
	private SpaceDistance z;
	
	public SpaceCoords(SpaceDistance x, SpaceDistance y, SpaceDistance z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public SpaceCoords(long lyX, long lyY, long lyZ, double kmX, double kmY, double kmZ)
	{
		this(new SpaceDistance(lyX, kmX), new SpaceDistance(lyY, kmY), new SpaceDistance(lyZ, kmZ));
	}
	
	public SpaceCoords(long x, long y, long z)
	{
		this(x, y, z, 0, 0, 0);
	}
	
	public SpaceCoords(double x, double y, double z)
	{
		this(0, 0, 0, x, y, z);
	}
	
	public SpaceCoords(Vector3f vector)
	{
		this(0, 0, 0, vector.x, vector.y, vector.z);
	}
	
	public SpaceCoords()
	{
		this(0, 0, 0, 0, 0, 0);
	}
	
	//============================================================================================
	//************************************Relative coordinates************************************
	//============================================================================================
	
	public long lyDistanceSquared()
	{
		return this.x.ly * this.x.ly + this.y.ly * this.y.ly + this.z.ly * this.z.ly;
	}
	
	/**
	 * @param other The other coordinates that are compared to these coordinates
	 * @return Returns squared distance between two coordinate values (mainly for use in checks, since square root operation is costly)
	 */
	public double distanceSquared(SpaceCoords other)
	{
		double xDistance = this.x.sub(other.x).toKm();
		double yDistance = this.y.sub(other.y).toKm();
		double zDistance = this.z.sub(other.z).toKm();
		
		return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
	}
	
	/**
	 * @param other The other coordinates that are compared to these coordinates
	 * @return Returns distance between two coordinate values
	 */
	public double distance(SpaceCoords other)
	{
		return Math.sqrt(distanceSquared(other));
	}
	
	public double distanceToCenterSquared()
	{
		return distanceSquared(NULL_COORDS);
	}
	
	public double distanceToCenter()
	{
		return distance(NULL_COORDS);
	}
	
	public static Quaterniond getQuaterniond(ClientLevel level, ViewCenter viewCenter, float partialTicks)
	{
		Quaterniond q = new Quaterniond();
		// Inverting so that we can view the world through the relative rotation of our view center
		if(!GeneralConfig.disable_view_center_rotation.get())
			viewCenter.getObjectAxisRotation().quaterniond().invert(q);
		
		return q;
	}
	
	public static Quaternionf getQuaternionf(ClientLevel level, ViewCenter viewCenter, float partialTicks)
	{
		Quaternionf q = new Quaternionf();
		// Inverting so that we can view the world through the relative rotation of our view center
		if(!GeneralConfig.disable_view_center_rotation.get())
			viewCenter.getObjectAxisRotation().quaternionf().invert(q);
		
		return q;
	}
	
	/**
	 * @param viewCenter The coordinates this object is viewed from
	 * @param r The radius of the sphere onto which the sky position is projected
	 * @return Returns the sky position at which the coordinates of this would appear on the sky when viewed from the viewCenter
	 */
	public SphericalCoords skyPosition(ClientLevel level, ViewCenter viewCenter, float radius, float partialTicks, boolean adjustForRotation)
	{
		SpaceCoords viewCenterCoords = viewCenter.getCoords();
		Vector3d positionVector = new Vector3d(this.x.sub(viewCenterCoords.x).toKm(), this.y.sub(viewCenterCoords.y).toKm(), this.z.sub(viewCenterCoords.z).toKm());
		
		if(adjustForRotation)
		{
			Quaterniond q = getQuaterniond(level, viewCenter, partialTicks);
			q.transform(positionVector);
		}
		
		return new SphericalCoords(positionVector, radius);
	}
	
	/**
	 * @param viewCenter The coordinates this object is viewed from
	 * @return Returns the sky position at which the coordinates of this would appear on the sky when viewed from the viewCenter
	 */
	public SphericalCoords skyPosition(ClientLevel level, ViewCenter viewCenter, float partialTicks, boolean adjustForRotation)
	{
		SpaceCoords viewCenterCoords = viewCenter.getCoords();
		Vector3d positionVector = new Vector3d(this.x.sub(viewCenterCoords.x).toKm(), this.y.sub(viewCenterCoords.y).toKm(), this.z.sub(viewCenterCoords.z).toKm());
		
		if(adjustForRotation)
		{
			Quaterniond q = getQuaterniond(level, viewCenter, partialTicks);
			q.transform(positionVector);
		}
		
		return new SphericalCoords(positionVector);
	}
	
	public SpaceCoords add(SpaceCoords other)
	{
		return new SpaceCoords(this.x.add(other.x), this.y.add(other.y), this.z.add(other.z));
	}
	
	public SpaceCoords add(long x, long y, long z)
	{
		return new SpaceCoords(this.x.add(x), this.y.add(y), this.z.add(z));
	}
	
	public SpaceCoords add(Vector3f vector)
	{
		return new SpaceCoords(this.x.add(vector.x), this.y.add(vector.y), this.z.add(vector.z));
	}
	
	public SpaceCoords sub(SpaceCoords other)
	{
		return new SpaceCoords(this.x.sub(other.x), this.y.sub(other.y), this.z.sub(other.z));
	}
	
	public SpaceCoords sub(Vector3f vector)
	{
		return new SpaceCoords(this.x.sub(vector.x), this.y.sub(vector.y), this.z.sub(vector.z));
	}
	
	//============================================================================================
	//************************************Getters and Setters*************************************
	//============================================================================================
	
	public SpaceDistance x()
	{
		return x;
	}
	
	public SpaceDistance y()
	{
		return y;
	}
	
	public SpaceDistance z()
	{
		return z;
	}
	
	public SpaceCoords copy()
	{
		return new SpaceCoords(x.copy(), y.copy(), z.copy());
	}
	
	@Override
	public String toString()
	{
		return "( x: " + x.toString() + ", y: " + y.toString() + ", z: " + z.toString() + " )";
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider)
	{
		CompoundTag tag = new CompoundTag();
		tag.put(X, x.serializeNBT(provider));
		tag.put(Y, y.serializeNBT(provider));
		tag.put(Z, z.serializeNBT(provider));
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
	{
		x.deserializeNBT(provider, tag.getCompound(X));;
		y.deserializeNBT(provider, tag.getCompound(Y));;
		z.deserializeNBT(provider, tag.getCompound(Z));;
	}
	
	
	
	public static class SpaceDistance implements INBTSerializable<CompoundTag>
	{
		public static final String LY = "ly";
		public static final String KM = "km";
		
		private long ly; // Light Years
		private double km; // Kilometers
		
		public static final Codec<SpaceDistance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		// Coordinates in Light Years
				Codec.LONG.optionalFieldOf(LY, 0L).forGetter(SpaceDistance::ly),
				// Coordinates in Kilometers
				Codec.DOUBLE.optionalFieldOf(KM, 0D).forGetter(SpaceDistance::km)
				).apply(instance, SpaceDistance::new));
		
		public SpaceDistance(long lightYears, double kilometers)
		{
			this.ly = lightYears;
			this.km = kilometers;
			
			handleKmOverflow();
		}
		
		public SpaceDistance(long lightYears)
		{
			this(lightYears, 0);
		}
		
		public SpaceDistance()
		{
			this(0, 0);
		}
		
		public SpaceDistance(double kilometers)
		{
			this(0, kilometers);
		}
		
		private void handleKmOverflow()
		{
			if(this.km >= KM_PER_LY || this.km <= -KM_PER_LY)
			{
				long additionalLightYears = kmToLy(this.km );
				double subKm = this.km - lyToKm(additionalLightYears);
				
				this.ly += additionalLightYears;
				this.km -= subKm;
			}
		}
		
		public long ly()
		{
			return ly;
		}
		
		public double km()
		{
			return km;
		}
		
		
		
		public static long kmToLy(double km)
		{
			return (long) (km / KM_PER_LY);
		}
		
		public static double lyToKm(long ly)
		{
			return KM_PER_LY * ly;
		}
		
		
		
		public double toKm()
		{
			return km + lyToKm(ly);
		}
		
		public double toLy()
		{
			return ly + kmToLy(km);
		}
		
		public SpaceDistance add(SpaceDistance other)
		{
			return new SpaceDistance(this.ly + other.ly, this.km + other.km);
		}
		
		public SpaceDistance add(double value)
		{
			return new SpaceDistance(this.ly, this.km + value);
		}
		
		public SpaceDistance add(long value)
		{
			return new SpaceDistance(this.ly + value, this.km);
		}
		
		public SpaceDistance sub(SpaceDistance other)
		{
			return new SpaceDistance(this.ly - other.ly, this.km - other.km);
		}
		
		public SpaceDistance sub(double value)
		{
			return new SpaceDistance(this.ly, this.km - value);
		}
		
		public SpaceDistance sub(long value)
		{
			return new SpaceDistance(this.ly - value, this.km);
		}
		
		public SpaceDistance mul(double value, boolean roundDown)
		{
			double result = this.ly * value;
			long ly = (long) result;
			
			return new SpaceDistance((long) ly, roundDown ? 0 : result - ly);
		}
		
		public SpaceDistance copy()
		{
			return new SpaceDistance(ly, km);
		}
		
		@Override
		public String toString()
		{
			return "[ly: " + ly + ", km: " + km + "]";
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider provider)
		{
			CompoundTag tag = new CompoundTag();
			tag.putLong(LY, ly);
			tag.putDouble(KM, km);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
		{
			ly = tag.getLong(LY);
			km = tag.getDouble(KM);
		}
	}
}
