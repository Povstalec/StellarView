package net.povstalec.stellarview.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.client.resourcepack.objects.GravityLense;
import net.povstalec.stellarview.client.resourcepack.objects.OrbitingObject;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.objects.StarField;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

import java.util.ArrayList;

public class ClientSpaceRegion
{
	public static final long LY_PER_REGION = 2000000;
	public static final long LY_PER_REGION_HALF = LY_PER_REGION / 2;
	
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private RegionPos pos;
	
	protected final ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	protected final ArrayList<GravityLense> lensingRenderers = new ArrayList<GravityLense>();
	protected final ArrayList<StarField> starFieldRenderers = new ArrayList<StarField>();
	
	public ClientSpaceRegion(RegionPos pos)
	{
		this.pos = pos;
	}
	
	public ClientSpaceRegion(long x, long y, long z)
	{
		this(new RegionPos(x, y, z));
	}
	
	public ClientSpaceRegion()
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
		
		if(child instanceof StarField starField)
			starFieldRenderers.add(starField);
		else if(child instanceof GravityLense lense)
			lensingRenderers.add(lense);
		
		return true;
	}
	
	public void renderDustClouds(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, float brightness)
	{
		for(StarField starField : starFieldRenderers)
		{
			starField.renderDustClouds(viewCenter, level, partialTicks, stack, camera, projectionMatrix, setupFog, brightness);
		}
	}
	
	public void render(ViewCenter viewCenter, SpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		for(SpaceObject spaceObject : children)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation());
		}
	}
	
	public void setBestLensing()
	{
		for(GravityLense gravityLense : lensingRenderers)
		{
			if(gravityLense.getLensingIntensity() > SpaceRenderer.lensingIntensity)
				gravityLense.setupLensing();
		}
	}
	
	public void setupSynodicOrbits()
	{
		for(SpaceObject spaceObject : children)
		{
			if(spaceObject instanceof OrbitingObject orbitingObject)
				orbitingObject.setupSynodicOrbit(null);
		}
	}
	
	public void resetStarFields()
	{
		for(StarField starField : starFieldRenderers)
		{
			starField.reset();
		}
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
