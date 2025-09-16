package net.povstalec.stellarview.api.common.space_objects;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.*;

import java.util.List;
import java.util.Optional;

public abstract class GravityLense extends StarLike
{
	public static final String LENSING_INTENSITY = "lensing_intensity";
	public static final String MAX_LENSING_DISTANCE = "max_lensing_distance";
	
	protected float lensingIntensity;
	protected double maxLensingDistance;
	
	public GravityLense() {}
	
	public GravityLense(Optional<ParentInfo> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
						Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
						float minStarSize, float maxStarAlpha, float minStarAlpha,
						float lensingIntensity, double maxLensingDistance)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha);
		
		this.lensingIntensity = lensingIntensity;
		this.maxLensingDistance = maxLensingDistance;
	}
	
	public float getLensingIntensity()
	{
		return lensingIntensity;
	}
	
	public double getMaxLensingDistance()
	{
		return maxLensingDistance;
	}
	
	public double getLensingIntensity(double distance)
	{
		double lensingIntensity = getLensingIntensity();
		
		lensingIntensity -= lensingIntensity * (distance / maxLensingDistance / 10000000);
		
		return lensingIntensity;
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.putFloat(LENSING_INTENSITY, lensingIntensity);
		tag.putDouble(MAX_LENSING_DISTANCE, maxLensingDistance);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		lensingIntensity = tag.getFloat(LENSING_INTENSITY);
		maxLensingDistance = tag.getFloat(MAX_LENSING_DISTANCE);
	}
}
