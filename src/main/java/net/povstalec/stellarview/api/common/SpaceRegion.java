package net.povstalec.stellarview.api.common;

import net.minecraft.nbt.CompoundTag;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.util.ISerializable;
import net.povstalec.stellarview.common.util.SpaceCoords;

import java.util.ArrayList;

public class SpaceRegion implements ISerializable
{
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";
	
	public static final long LY_PER_REGION = 2000000;
	public static final long LY_PER_REGION_HALF = LY_PER_REGION / 2;
	
	private RegionPos pos;
	
	protected ArrayList<SpaceObject> children;
	
	public SpaceRegion(RegionPos pos)
	{
		this.pos = pos;
		this.children = new ArrayList<SpaceObject>();
	}
	
	public SpaceRegion(long x, long y, long z)
	{
		this(new RegionPos(x, y, z));
	}
	
	public SpaceRegion()
	{
		this(0, 0, 0);
	}
	
	public RegionPos getRegionPos()
	{
		return pos;
	}
	
	public ArrayList<SpaceObject> getChildren()
	{
		return children;
	}
	
	public boolean addChild(SpaceObject child)
	{
		if(this.children.contains(child))
			return false;
		
		this.children.add(child);
		
		return true;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putLong(X, pos.x());
		tag.putLong(Y, pos.y());
		tag.putLong(Z, pos.z());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		pos = new RegionPos(tag.getLong(X), tag.getLong(Y), tag.getLong(Z));
	}
	
	
	
	public static class RegionPos
	{
		private long x, y, z;
		
		public RegionPos(long x, long y, long z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public RegionPos(SpaceCoords coords)
		{
			this(	(coords.x().ly() - LY_PER_REGION_HALF) / LY_PER_REGION,
					(coords.y().ly() - LY_PER_REGION_HALF) / LY_PER_REGION,
					(coords.z().ly() - LY_PER_REGION_HALF) / LY_PER_REGION);
		}
		
		public long x()
		{
			return x;
		}
		
		public long y()
		{
			return y;
		}
		
		public long z()
		{
			return z;
		}
		
		public long lyX()
		{
			return x * LY_PER_REGION;
		}
		
		public long lyY()
		{
			return y * LY_PER_REGION;
		}
		
		public long lyZ()
		{
			return z * LY_PER_REGION;
		}
		
		public boolean isInRange(RegionPos other, int range)
		{
			return Math.abs(this.x - other.x) < range && Math.abs(this.y - other.y) < range && Math.abs(this.z - other.z) < range;
		}
		
		@Override
		public final boolean equals(Object object)
		{
			if(object instanceof RegionPos pos)
				return this.x == pos.x && this.y == pos.y && this.z == pos.z;
			
			return false;
		}
		
		@Override
		public final int hashCode()
		{
			int result = (int) x;
			result = 31 * result + (int) y;
			result = 31 * result + (int) z;
			return result;
		}
		
		@Override
		public String toString()
		{
			return "{x: " + x + ", y: " + y + ", z: " + z + "}";
		}
	}
}
