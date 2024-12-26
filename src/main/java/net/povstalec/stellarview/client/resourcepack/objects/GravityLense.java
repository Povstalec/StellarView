package net.povstalec.stellarview.client.resourcepack.objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public abstract class GravityLense extends StarLike
{
	protected final float lensingIntensity;
	protected final double maxLensingDistance;
	
	protected SphericalCoords sphericalCoords = new SphericalCoords(0, 0, 0);
	
	public GravityLense(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
						Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler,
						float minStarSize, float maxStarAlpha, float minStarAlpha,
						float lensingIntensity, double maxLensingDistance)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler, minStarSize, maxStarAlpha, minStarAlpha);
		
		this.lensingIntensity = lensingIntensity;
		this.maxLensingDistance = maxLensingDistance;
	}
	
	public void setupLensing()
	{
		float intensity = (float) getLensingIntensity(lastDistance);
		
		if(intensity < SpaceRenderer.lensingIntensity)
			return;
		
		Quaternionf lensingQuat = new Quaternionf().rotateY((float) sphericalCoords.theta);
		lensingQuat.mul(new Quaternionf().rotateX((float) sphericalCoords.phi));
		
		Matrix3f lensingMatrixInv = new Matrix3f().rotate(lensingQuat);
		Matrix3f lensingMatrix = new Matrix3f().rotate(lensingQuat.invert());
		
		SpaceRenderer.lensingIntensity = intensity;
		SpaceRenderer.lensingMatrixInv = lensingMatrixInv;
		SpaceRenderer.lensingMatrix = lensingMatrix;
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
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;
		
		Color.FloatRGBA starRGBA = starRGBA(lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
			{
				size = (float) textureLayer.minSize();
				
				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = starSize(size, lyDistance);
			}
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
					   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder,
					   Vector3f parentVector, AxisRotation parentRotation)
	{
		long ticks = level.getDayTime();
		
		Vector3f positionVector = getPosition(viewCenter, parentRotation, ticks, partialTicks).add(parentVector); // Handles orbits 'n stuff
		
		// Add parent vector to current coords
		SpaceCoords coords = getCoords().add(positionVector);
		
		// Subtract coords of this from View Center coords to get relative coords
		sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, false);
		SphericalCoords sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, true);
		
		lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		if(getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			for(SpaceObject child : children)
			{
				// Render child behind the parent
				if(child.lastDistance >= this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, this.axisRotation);
			}
		}
		
		// If the object isn't the same we're viewing everything from and it isn't too far away, render it
		if(!viewCenter.objectEquals(this) && getFadeOutHandler().getFadeOutEndDistance().toKm() > lastDistance)
			renderTextureLayers(viewCenter, level, camera, bufferbuilder, stack.last().pose(), sphericalCoords, ticks, lastDistance, partialTicks);
		
		if(getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			for(SpaceObject child : children)
			{
				// Render child in front of the parent
				if(child.lastDistance < this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, this.axisRotation);
			}
		}
	}
}
