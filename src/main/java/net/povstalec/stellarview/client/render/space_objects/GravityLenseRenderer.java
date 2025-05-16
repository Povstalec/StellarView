package net.povstalec.stellarview.client.render.space_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.common.space_objects.GravityLense;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class GravityLenseRenderer<T extends GravityLense> extends StarLikeRenderer<T>
{
	protected SphericalCoords sphericalCoords;
	
	public GravityLenseRenderer(T gravityLense)
	{
		super(gravityLense);
		
		this.sphericalCoords = new SphericalCoords(0, 0, 0);
	}
	
	public float lensingIntensity()
	{
		return renderedObject.getLensingIntensity();
	}
	
	public double maxLensingDistance()
	{
		return renderedObject.getMaxLensingDistance();
	}
	
	public void setupLensing()
	{
		float intensity = (float) renderedObject.getLensingIntensity(lastDistance);
		
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
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, Tesselator tesselator,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  double fade, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;
		
		Color.FloatRGBA starRGBA = renderedObject.starRGBA(lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(renderedObject.distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
			{
				size = (float) textureLayer.minSize();
				
				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = renderedObject.starSize(size, lyDistance);
			}
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), Color.FloatRGBA.DEFAULT, textureLayer.texture(), textureLayer.uv(),
				level, camera, tesselator, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, LightEffects.dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
					   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator,
					   Vector3f parentVector, AxisRotation parentRotation)
	{
		Vector3f positionVector = getPosition(viewCenter, parentRotation, viewCenter.ticks(), partialTicks).add(parentVector); // Handles orbits 'n stuff
		
		// Add parent vector to current coords
		SpaceCoords coords = renderedObject.getCoords().add(positionVector);
		
		// Subtract coords of this from View Center coords to get relative coords
		sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, false);
		SphericalCoords sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, true);
		
		lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		double childRenderDistance = renderedObject.getFadeOutHandler().getMaxChildRenderDistance().toKm();
		if(childRenderDistance > lastDistance)
		{
			for(SpaceObjectRenderer child : children)
			{
				// Render child behind the parent
				if(child.lastDistance >= this.lastDistance)
					child.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, positionVector, axisRotation());
			}
		}
		
		// If the object isn't the same we're viewing everything from and it isn't too far away, render it
		if(!viewCenter.objectEquals(this) && renderedObject.getFadeOutHandler().getFadeOutEndDistance().toKm() > lastDistance)
			renderTextureLayers(viewCenter, level, camera, tesselator, modelViewMatrix, sphericalCoords, viewCenter.ticks(), lastDistance, partialTicks);
		
		if(childRenderDistance > lastDistance)
		{
			for(SpaceObjectRenderer child : children)
			{
				// Render child in front of the parent
				if(child.lastDistance < this.lastDistance)
					child.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, positionVector, axisRotation());
			}
		}
	}
}
