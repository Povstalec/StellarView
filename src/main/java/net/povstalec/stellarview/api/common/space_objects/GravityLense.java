package net.povstalec.stellarview.api.common.space_objects;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.*;

import java.util.List;
import java.util.Optional;

public abstract class GravityLense extends StarLike
{
	protected float lensingIntensity;
	protected double maxLensingDistance;
	
	public GravityLense() {}
	
	public GravityLense(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
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
}
