package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class SpaceObject
{
	public static final float DEFAULT_DISTANCE = 100.0F;
	
	public static final ResourceLocation SPACE_OBJECT_LOCATION = new ResourceLocation(StellarView.MODID, "space_object");
	public static final ResourceKey<Registry<SpaceObject>> REGISTRY_KEY = ResourceKey.createRegistryKey(SPACE_OBJECT_LOCATION);
	public static final Codec<ResourceKey<SpaceObject>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);
	
	@Nullable
	protected ResourceKey<SpaceObject> parentKey;

	@Nullable
	protected SpaceObject parent;
	
	protected ArrayList<SpaceObject> children = new ArrayList<SpaceObject>();
	
	protected SpaceCoords coords; // Absolute coordinates of the center (not necessarily the object itself, since it can be orbiting some other object for example)
	protected AxisRotation axisRotation;
	
	private ArrayList<TextureLayer> textureLayers;
	
	public ResourceLocation location;
	
	public SpaceObject(Optional<ResourceKey<SpaceObject>> parentKey, SpaceCoords coords, AxisRotation axisRotation, List<TextureLayer> textureLayers)
	{
		if(parentKey.isPresent())
				this.parentKey = parentKey.get();
		
		this.coords = coords;
		this.axisRotation = axisRotation;
		
		this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
	}
	
	public SpaceCoords getCoords()
	{
		return this.coords;
	}
	
	public Vector3f getPosition(long ticks)
	{
		return new Vector3f();
	}
	
	public AxisRotation getAxisRotation()
	{
		return axisRotation;
	}
	
	public ArrayList<TextureLayer> getTextureLayers()
	{
		return textureLayers;
	}
	
	public Optional<ResourceKey<SpaceObject>> getParentKey()
	{
		if(parentKey != null)
			return Optional.of(parentKey);
		
		return Optional.empty();
	}
	
	public float sizeMultiplier(float distance)
	{
		return 1 / distance;
	}
	
	public void addChild(SpaceObject child)
	{
		if(child.parent != null)
		{
			StellarView.LOGGER.error(this.location + " already has a parent");
			return;
		}
		
		this.children.add(child);
		child.parent = this;
		child.coords = child.coords.add(this.coords);
		
		child.addCoordsToChildren(this.coords);
	}
	
	private void addCoordsToChildren(SpaceCoords coords)
	{
		for(SpaceObject childOfChild : this.children)
		{
			childOfChild.coords = childOfChild.coords.add(coords);
			childOfChild.addCoordsToChildren(coords);
		}
	}
	
	
	
	protected void renderTextureLayers(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, float sizeMultiplier, float brightness)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : textureLayers)
		{
			textureLayer.render(bufferbuilder, lastMatrix, sphericalCoords, brightness, sizeMultiplier, 0);
		}
	}
	
	
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, 
			Vector3f parentVector)
	{
		Vector3f potitionVector = getPosition(level.getDayTime()).add(parentVector); // Handles orbits 'n stuff
		
		if(!viewCenter.objectEquals(this))
		{
			// Add parent vector to current coords
			SpaceCoords coords = getCoords().add(potitionVector);
			
			// Subtract coords of this from View Center coords to get relative coords
			SphericalCoords sphericalCoords = coords.skyPosition(viewCenter.getCoords());
			
			double distance = sphericalCoords.r;
			sphericalCoords.r = DEFAULT_DISTANCE;
			renderTextureLayers(bufferbuilder, stack.last().pose(), sphericalCoords, sizeMultiplier((float) distance), 1); //TODO Brightness
		}
		
		for(SpaceObject child : children)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector);
		}
	}
	
	// Sets View Center coords and then renders everything
	public void renderFrom(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		viewCenter.addCoords(getPosition(level.getDayTime()));
		
		if(parent != null)
			parent.renderFrom(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
		else
			viewCenter.renderSkyObjects(level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	@Override
	public String toString()
	{
		if(location != null)
			return location.toString();
		
		return this.getClass().toString();
	}
}
