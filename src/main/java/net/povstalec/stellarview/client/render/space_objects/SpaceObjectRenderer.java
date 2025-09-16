package net.povstalec.stellarview.client.render.space_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public abstract class SpaceObjectRenderer<RenderedObject extends SpaceObject>
{
	protected SpaceObjectRenderer parent;
	
	protected RenderedObject renderedObject;
	protected double lastDistance = 0; // Last known distance of this object from the View Center, used for sorting
	
	protected ArrayList<SpaceObjectRenderer<?>> children;
	
	public SpaceObjectRenderer(RenderedObject renderedObject)
	{
		this.renderedObject = renderedObject;
		this.children = new ArrayList<SpaceObjectRenderer<?>>();
	}
	
	public SpaceCoords spaceCoords()
	{
		return renderedObject.getCoords();
	}
	
	public AxisRotation axisRotation()
	{
		return renderedObject.getAxisRotation();
	}
	
	public Vector3f getPosition(ViewCenter viewCenter, AxisRotation axisRotation, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public Vector3f getPosition(ViewCenter viewCenter, long ticks, float partialTicks)
	{
		return new Vector3f();
	}
	
	public void addParent(SpaceObjectRenderer<?> parent)
	{
		this.parent = parent;
	}
	
	public void addChildRaw(SpaceObjectRenderer<?> child)
	{
		if(child.parent != null)
		{
			StellarView.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		children.add(child);
		child.addParent(this);
		
		renderedObject.addChildRaw(child.renderedObject);
	}
	
	public void addChild(SpaceObjectRenderer<?> child)
	{
		if(child.parent != null)
		{
			StellarView.LOGGER.error(this.toString() + " already has a parent");
			return;
		}
		
		children.add(child);
		child.addParent(this);
		
		renderedObject.addChild(child.renderedObject);
	}
	
	public ArrayList<SpaceObjectRenderer<?>> children()
	{
		return children;
	}
	
	public RenderedObject renderedObject()
	{
		return renderedObject;
	}
	
	public void setupSpaceObject(ResourceLocation id)
	{
		if(id != null)
			renderedObject().setResourceLocation(id);
	}
	
	@Override
	public String toString()
	{
		return renderedObject.toString();
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	public abstract void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
								Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator,
								Vector3f parentVector, AxisRotation parentRotation);
	
	// Sets View Center coords and then renders everything
	public void renderFrom(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
						   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator)
	{
		if(parent != null)
			viewCenter.addCoords(getPosition(viewCenter, parent.axisRotation(), viewCenter.ticks(), partialTicks));
		else
			viewCenter.addCoords(getPosition(viewCenter, viewCenter.ticks(), partialTicks));
		
		if(parent != null)
			parent.renderFrom(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator);
		else
			viewCenter.renderSkyObjects(this, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator);
	}
}
