package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.ArrayList;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.povstalec.stellarview.client.render.LightEffects;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SpaceCoords.SpaceDistance;
import net.povstalec.stellarview.common.util.StellarCoordinates;

public abstract class SpaceObject
{
	public static final float DEFAULT_DISTANCE = 100.0F;
	
	public static final String PARENT_LOCATION = "parent";
	
	public static final String COORDS = "coords";
	public static final String AXIS_ROTATION = "axis_rotation";
	
	public static final String FADE_OUT_HANDLER = "fade_out_handler";
	
	public static final String ID = "id";
	
	public static final ResourceLocation SPACE_OBJECT_LOCATION = new ResourceLocation(StellarView.MODID, "space_object");
	public static final ResourceKey<Registry<SpaceObject>> REGISTRY_KEY = ResourceKey.createRegistryKey(SPACE_OBJECT_LOCATION);
	public static final Codec<ResourceKey<SpaceObject>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	@Nullable
	protected ResourceLocation parentLocation;

	@Nullable
	protected SpaceObject parent;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	protected SpaceCoords coords; // Absolute coordinates of the center (not necessarily the object itself, since it can be orbiting some other object for example)
	protected AxisRotation axisRotation;
	
	protected ResourceLocation location;
	protected double lastDistance = 0; // Last known distance of this object from the View Center, used for sorting
	
	public SpaceObject() {}
	
	public SpaceObject(Optional<ResourceLocation> parentLocation, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation)
	{
		if(parentLocation.isPresent())
				this.parentLocation = parentLocation.get();
		
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
	
	public Vector3f getPosition(ViewCenter viewCenter, AxisRotation axisRotation, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public Vector3f getPosition(ViewCenter viewCenter, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public AxisRotation getAxisRotation()
	{
		return axisRotation;
	}
	
	public Optional<ResourceLocation> getParentLocation()
	{
		return Optional.ofNullable(parentLocation);
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
	
	public static float dayBrightness(ViewCenter viewCenter, float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		if(viewCenter.starsAlwaysVisible())
			return GeneralConfig.bright_stars.get() ? 0.5F * LightEffects.lightSourceStarDimming(level, camera) : 0.5F;
		
		float brightness = level.getStarBrightness(partialTicks) * 2;
		
		if(GeneralConfig.bright_stars.get())
			brightness = brightness * LightEffects.lightSourceStarDimming(level, camera);
		
		if(brightness < viewCenter.dayMaxBrightness && size > viewCenter.dayMinVisibleSize)
		{
			float aboveSize = size >= viewCenter.dayMaxVisibleSize ? viewCenter.dayVisibleSizeRange : size - viewCenter.dayMinVisibleSize;
			float brightnessPercentage = aboveSize / viewCenter.dayVisibleSizeRange;
			
			brightness = brightnessPercentage * viewCenter.dayMaxBrightness;
		}
		
		return brightness * LightEffects.rainDimming(level, partialTicks);
	}
	
	public void setPosAndRotation(SpaceCoords coords, AxisRotation axisRotation)
	{
		removeCoordsAndRotationFromChildren(getCoords(), getAxisRotation());
		
		if(this.parent != null)
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
	
	public void addChild(SpaceObject child)
	{
		if(child.parent != null)
		{
			StellarView.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		this.children.add(child);
		child.parent = this;
		child.coords = child.coords.add(this.coords);
		
		child.axisRotation = child.axisRotation.add(this.axisRotation);
		
		child.addCoordsAndRotationToChildren(this.coords, this.axisRotation);
	}
	
	protected void addCoordsAndRotationToChildren(SpaceCoords coords, AxisRotation axisRotation)
	{
		for(SpaceObject childOfChild : this.children)
		{
			childOfChild.coords = childOfChild.coords.add(coords);
			childOfChild.axisRotation = childOfChild.axisRotation.add(axisRotation);
			
			childOfChild.addCoordsAndRotationToChildren(coords, axisRotation);
		}
	}
	
	protected void removeCoordsAndRotationFromChildren(SpaceCoords coords, AxisRotation axisRotation)
	{
		for(SpaceObject childOfChild : this.children)
		{
			childOfChild.coords = childOfChild.coords.sub(coords);
			childOfChild.axisRotation = childOfChild.axisRotation.sub(axisRotation);
			
			childOfChild.removeCoordsAndRotationFromChildren(coords, axisRotation);
		}
	}
	
	
	public abstract void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, 
			Vector3f parentVector, AxisRotation parentRotation);
	
	// Sets View Center coords and then renders everything
	public void renderFrom(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		if(parent != null)
			viewCenter.addCoords(getPosition(viewCenter, parent.getAxisRotation(), viewCenter.ticks(), partialTicks));
		else
			viewCenter.addCoords(getPosition(viewCenter, viewCenter.ticks(), partialTicks));
		
		if(parent != null)
			parent.renderFrom(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
		else
			viewCenter.renderSkyObjects(this, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	@Override
	public String toString()
	{
		if(location != null)
			return location.toString();
		
		return this.getClass().toString();
	}
	
	public void fromTag(CompoundTag tag)
	{
		if(tag.contains(ID))
			this.location = new ResourceLocation(tag.getString(ID));
		
		if(tag.contains(PARENT_LOCATION))
			this.parentLocation = new ResourceLocation(tag.getString(PARENT_LOCATION));
		
		this.coords = SpaceCoords.fromTag(tag.getCompound(COORDS));
		
		this.axisRotation = AxisRotation.fromTag(tag.getCompound(AXIS_ROTATION));
	}
}
