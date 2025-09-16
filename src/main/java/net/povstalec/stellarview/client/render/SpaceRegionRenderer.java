package net.povstalec.stellarview.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.common.SpaceRegion;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.client.render.space_objects.GravityLenseRenderer;
import net.povstalec.stellarview.client.render.space_objects.OrbitingObjectRenderer;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.ConstellationRenderer;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.StarFieldRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class SpaceRegionRenderer
{
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	protected final SpaceRegion region;
	
	protected final ArrayList<SpaceObjectRenderer<?>> children = new ArrayList<SpaceObjectRenderer<?>>();
	
	protected final ArrayList<GravityLenseRenderer> lensingRenderers = new ArrayList<GravityLenseRenderer>();
	protected final ArrayList<StarFieldRenderer> starFieldRenderers = new ArrayList<StarFieldRenderer>();
	protected final ArrayList<ConstellationRenderer> constellationRenderers = new ArrayList<ConstellationRenderer>();
	
	private boolean isSetUp = false;
	
	public SpaceRegionRenderer(SpaceRegion region)
	{
		this.region = region;
	}
	
	public SpaceRegion.RegionPos getRegionPos()
	{
		return region.getRegionPos();
	}
	
	public ArrayList<SpaceObjectRenderer<?>> getChildren()
	{
		return children;
	}
	
	public boolean addChild(SpaceObjectRenderer child)
	{
		if(!this.region.addChild(child.renderedObject()))
			return false;
		
		this.children.add(child);
		
		return true;
	}
	
	public void setupRegion()
	{
		for(SpaceObjectRenderer<?> child : children)
		{
			setupLensingAndStarFields(child);
		}
		
		isSetUp = true;
	}
	
	public void setupLensingAndStarFields(SpaceObjectRenderer<?> renderer)
	{
		if(renderer instanceof StarFieldRenderer<?> starField)
			starFieldRenderers.add(starField);
		else if(renderer instanceof ConstellationRenderer<?> constellation && constellation.shouldRender())
			constellationRenderers.add(constellation);
		else if(renderer instanceof GravityLenseRenderer<?> lense)
			lensingRenderers.add(lense);
		
		for(SpaceObjectRenderer<?> child : renderer.children())
		{
			setupLensingAndStarFields(child);
		}
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	public void renderDustClouds(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, float brightness)
	{
		for(StarFieldRenderer<?> starField : starFieldRenderers)
		{
			starField.renderDustClouds(viewCenter, level, partialTicks, stack, camera, projectionMatrix, setupFog, brightness);
		}
	}
	
	public void render(ViewCenter viewCenter, SpaceObjectRenderer masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		if(!isSetUp)
			setupRegion();
		
		for(SpaceObjectRenderer<?> spaceObject : children)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation());
		}
	}
	
	public void setBestLensing()
	{
		if(!GeneralConfig.gravitational_lensing.get())
		{
			SpaceRenderer.lensingIntensity = 0;
			SpaceRenderer.lensingMatrixInv = SpaceRenderer.IDENTITY_MATRIX;
			SpaceRenderer.lensingMatrix = SpaceRenderer.IDENTITY_MATRIX;
		}
		else
		{
			for(GravityLenseRenderer<?> gravityLense : lensingRenderers)
			{
				if(gravityLense.lensingIntensity() > SpaceRenderer.lensingIntensity)
					gravityLense.setupLensing();
			}
		}
	}
	
	public void setupSynodicOrbits()
	{
		for(SpaceObjectRenderer<?> spaceObject : children)
		{
			if(spaceObject instanceof OrbitingObjectRenderer<?> orbitingObject)
				orbitingObject.setupSynodicOrbit(null);
		}
	}
	
	public void resetStarFields()
	{
		for(StarFieldRenderer<?> starField : starFieldRenderers)
		{
			starField.reset();
		}
	}
	
	public void resetConstellations()
	{
		for(ConstellationRenderer<?> constellation : constellationRenderers)
		{
			constellation.reset();
		}
	}
}
