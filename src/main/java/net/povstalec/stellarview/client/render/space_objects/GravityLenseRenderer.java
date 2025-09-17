package net.povstalec.stellarview.client.render.space_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

import net.povstalec.stellarview.api.common.space_objects.GravityLense;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.*;

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
	
	public static double cosFromSin(float angle)
	{
		return Math.sin(angle + Math.PI / 2);
	}
	
	private Quaternion rotateY(float angle, Quaternion dest)
	{
		double sin = Math.sin(angle * 0.5f);
		double cos = cosFromSin(angle * 0.5f);
		dest.set((float) (dest.i() * cos - dest.k() * sin), (float) (dest.r() * sin + dest.j() * cos), (float) (dest.i() * sin + dest.k() * cos), (float) (dest.r() * cos - dest.j() * sin));
		
		return dest;
	}
	
	private Quaternion rotateX(float angle, Quaternion dest)
	{
		double sin = Math.sin(angle * 0.5f);
		double cos = cosFromSin(angle * 0.5f);
		
		dest.set((float) (dest.r() * sin + dest.i() * cos), (float) (dest.j() * cos + dest.k() * sin), (float) (dest.k() * cos - dest.j() * sin), (float) (dest.r() * cos - dest.i() * sin));
		
		return dest;
	}
	
	private Quaternion invert(Quaternion dest)
	{
		float invNorm = 1.0f / Math.fma(dest.i(), dest.i(), Math.fma(dest.j(), dest.j(), Math.fma(dest.k(), dest.k(), dest.r() * dest.r())));
		dest.set((float) (-dest.i() * invNorm), (float) (-dest.j() * invNorm), (float) (-dest.k() * invNorm), (float) (dest.r() * invNorm));
		
		return dest;
	}
	
	public void setupLensing()
	{
		float intensity = (float) renderedObject.getLensingIntensity(lastDistance);
		
		if(intensity < SpaceRenderer.lensingIntensity)
			return;
		
		Quaternion lensingQuat = new Quaternion(0, 0, 0, 1);
		rotateY((float) sphericalCoords.theta, lensingQuat);
		Quaternion temp = new Quaternion(0, 0, 0, 1);
		
		lensingQuat.mul(rotateX((float) sphericalCoords.phi, temp));
		
		Mat3f lensingMatrixInv = new Mat3f().rotate(lensingQuat);
		Mat3f lensingMatrix = new Mat3f().rotate(invert(lensingQuat));
		
		SpaceRenderer.lensingIntensity = intensity;
		SpaceRenderer.lensingMatrixInv = lensingMatrixInv;
		SpaceRenderer.lensingMatrix = lensingMatrix;
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  double fade, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;
		
		Color.FloatRGBA starRGBA = renderedObject.starRGBA(lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(renderedObject.distanceSize(distance));

		if(size < textureLayer.minSize()) {
			if (textureLayer.clampAtMinSize()) {
				size = (float) textureLayer.minSize();

				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = renderedObject.starSize(size, lyDistance);
			}
			else
				return;
		}
		else if(size > textureLayer.maxSize()) {
			if (textureLayer.clampAtMaxSize())
				size = (float) textureLayer.maxSize();
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, LightEffects.dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
					   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder,
					   Vector3f parentVector, AxisRotation parentRotation)
	{
		Vector3f positionVector = getPosition(viewCenter, parentRotation, viewCenter.ticks(), partialTicks); // Handles orbits 'n stuff
		positionVector.add(parentVector);
		
		// Add parent vector to current coords
		SpaceCoords coords = renderedObject.getCoords().add(positionVector);
		
		// Subtract coords of this from View Center coords to get relative coords
		sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, false);
		SphericalCoords sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, true);
		
		lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		if(renderedObject.getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			for(SpaceObjectRenderer child : children)
			{
				// Render child behind the parent
				if(child.lastDistance >= this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, axisRotation());
			}
		}
		
		// If the object isn't the same we're viewing everything from and it isn't too far away, render it
		if(!viewCenter.objectEquals(this) && renderedObject.getFadeOutHandler().getFadeOutEndDistance().toKm() > lastDistance)
			renderTextureLayers(viewCenter, level, camera, bufferbuilder, stack.last().pose(), sphericalCoords, viewCenter.ticks(), lastDistance, partialTicks);
		
		if(renderedObject.getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			for(SpaceObjectRenderer child : children)
			{
				// Render child in front of the parent
				if(child.lastDistance < this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, axisRotation());
			}
		}
	}
}
