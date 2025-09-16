package net.povstalec.stellarview.api.common.space_objects;

import java.util.ArrayList;
import java.util.Optional;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.ISerializable;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import org.jetbrains.annotations.Nullable;

public abstract class SpaceObject implements ISerializable
{
	public static final String PARENT = "parent";
	public static final String COORDS = "coords";
	public static final String AXIS_ROTATION = "axis_rotation";
	public static final String ID = "id";
	
	public static final ResourceLocation SPACE_OBJECT_LOCATION = new ResourceLocation(StellarView.MODID, "space_object");
	public static final ResourceKey<Registry<SpaceObject>> REGISTRY_KEY = ResourceKey.createRegistryKey(SPACE_OBJECT_LOCATION);
	public static final Codec<ResourceKey<SpaceObject>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	@Nullable
	protected ParentInfo parentInfo;

	@Nullable
	protected SpaceObject parent;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	protected SpaceCoords coords; // Absolute coordinates of the center (not necessarily the object itself, since it can be orbiting some other object for example)
	protected AxisRotation axisRotation;
	
	protected ResourceLocation location;
	
	public SpaceObject() {}
	
	public SpaceObject(Optional<ParentInfo> parentInfo, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation)
	{
		this.parentInfo = parentInfo.orElse(null);
		
		if(coords.left().isPresent())
			this.coords = coords.left().get();
		else
			this.coords = coords.right().get().toGalactic().toSpaceCoords();
		
		this.axisRotation = axisRotation;
	}
	
	public SpaceCoords getCoords()
	{
		return this.coords;
	}
	
	public AxisRotation getAxisRotation()
	{
		return axisRotation;
	}
	
	public ArrayList<SpaceObject> getChildren()
	{
		return children;
	}
	
	public Optional<ParentInfo> getParentInfo()
	{
		return Optional.ofNullable(parentInfo);
	}
	
	@Nullable
	public ResourceLocation getParentLocation()
	{
		if(parentInfo != null)
			return parentInfo.parentLocation();
		
		return null;
	}
	
	public boolean isRelative()
	{
		if(parentInfo != null)
			return parentInfo.relative();
		
		return true;
	}
	
	public Optional<SpaceObject> getParent()
	{
		return Optional.ofNullable(parent);
	}
	
	public void setResourceLocation(ResourceLocation resourceLocation)
	{
		this.location = resourceLocation;
	}
	
	public static double distanceSize(double distance)
	{
		return 1 / distance;
	}
	
	public void setPosAndRotation(SpaceCoords coords, AxisRotation axisRotation)
	{
		removeCoordsAndRotationFromChildren(getCoords(), getAxisRotation());
		
		if(this.parent != null && isRelative())
		{
			this.coords = coords.add(this.parent.getCoords());
			this.axisRotation = axisRotation.add(this.parent.getAxisRotation());
		}
		else
		{
			this.coords = coords;
			this.axisRotation = axisRotation;
		}
		
		addCoordsAndRotationToChildren(this.coords, this.axisRotation);
	}
	
	public void addParent(SpaceObject parent)
	{
		this.parent = parent;
	}
	
	public boolean addChildRaw(SpaceObject child)
	{
		if(child.parent != null)
		{
			StellarView.LOGGER.error(this.toString() + " already has a parent");
			return false;
		}
		
		this.children.add(child);
		child.addParent(this);
		
		return true;
	}
	
	public void addChild(SpaceObject child)
	{
		if(!addChildRaw(child))
			return;
		
		if(child.isRelative())
		{
			child.coords = child.coords.add(this.coords);
			child.axisRotation = child.axisRotation.add(this.axisRotation);
			
			child.addCoordsAndRotationToChildren(this.coords, this.axisRotation);
		}
	}
	
	protected void addCoordsAndRotationToChildren(SpaceCoords coords, AxisRotation axisRotation)
	{
		for(SpaceObject childOfChild : this.children)
		{
			if(childOfChild.isRelative())
			{
				childOfChild.coords = childOfChild.coords.add(coords);
				childOfChild.axisRotation = childOfChild.axisRotation.add(axisRotation);
				
				childOfChild.addCoordsAndRotationToChildren(coords, axisRotation);
			}
		}
	}
	
	protected void removeCoordsAndRotationFromChildren(SpaceCoords coords, AxisRotation axisRotation)
	{
		for(SpaceObject childOfChild : this.children)
		{
			if(childOfChild.isRelative())
			{
				childOfChild.coords = childOfChild.coords.sub(coords);
				childOfChild.axisRotation = childOfChild.axisRotation.sub(axisRotation);
				
				childOfChild.removeCoordsAndRotationFromChildren(coords, axisRotation);
			}
		}
	}
	
	@Override
	public String toString()
	{
		if(location != null)
			return location.toString();
		
		return this.getClass().toString();
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		if(location != null)
			tag.putString(ID, location.toString());
		
		if(parentInfo != null)
			tag.put(PARENT, parentInfo.serializeNBT());
		
		tag.put(COORDS, coords.serializeNBT());
		
		tag.put(AXIS_ROTATION, axisRotation.serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		if(tag.contains(ID))
			this.location = new ResourceLocation(tag.getString(ID));
		
		if(tag.contains(PARENT))
		{
			this.parentInfo = new ParentInfo();
			parentInfo.deserializeNBT(tag.getCompound(PARENT));
		}
		
		this.coords = new SpaceCoords();
		coords.deserializeNBT(tag.getCompound(COORDS));
		
		this.axisRotation = new AxisRotation();
		axisRotation.deserializeNBT(tag.getCompound(AXIS_ROTATION));
	}
	
	
	
	public static class ParentInfo implements ISerializable
	{
		public static final String PARENT_LOCATION = "location";
		public static final String RELATIVE = "relative";
		
		public static final Codec<ParentInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf(PARENT_LOCATION).forGetter(ParentInfo::parentLocation),
				Codec.BOOL.optionalFieldOf(RELATIVE, true).forGetter(ParentInfo::relative)
		).apply(instance, ParentInfo::new));
		
		protected ResourceLocation parentLocation;
		protected boolean relative;
		
		public ParentInfo() {}
		
		public ParentInfo(ResourceLocation parentLocation, boolean relative)
		{
			this.parentLocation = parentLocation;
			this.relative = relative;
		}
		
		public ResourceLocation parentLocation()
		{
			return parentLocation;
		}
		
		public boolean relative()
		{
			return relative;
		}
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.putString(PARENT_LOCATION, this.parentLocation.toString());
			tag.putBoolean(RELATIVE, this.relative);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			this.parentLocation = new ResourceLocation(tag.getString(PARENT_LOCATION));
			this.relative = tag.getBoolean(RELATIVE);
		}
	}
}
