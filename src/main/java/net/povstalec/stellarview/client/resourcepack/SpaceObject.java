package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SpaceCoords.SpaceDistance;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class SpaceObject
{
	public static final float DEFAULT_DISTANCE = 100.0F;

	public static final float MIN_VISIBLE_BRIGHTNESS = 0.25F;
	
	public static final float MIN_VISIBLE_SIZE = 2.5F; // TODO Make these values definable in resourcepacks
	public static final float MAX_VISIBLE_SIZE = 10F;
	public static final float VISIBLE_SIZE_RANGE = MAX_VISIBLE_SIZE - MIN_VISIBLE_SIZE;
	
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
	
	protected ArrayList<TextureLayer> textureLayers;
	
	protected FadeOutHandler fadeOutHandler;
	
	protected ResourceLocation location;
	protected double lastDistance = 0; // Last known distance of this object from the View Center, used for sorting
	
	public SpaceObject(Optional<ResourceKey<SpaceObject>> parentKey, SpaceCoords coords, AxisRotation axisRotation, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		if(parentKey.isPresent())
				this.parentKey = parentKey.get();
		
		this.coords = coords;
		this.axisRotation = axisRotation;
		
		this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
		
		this.fadeOutHandler = fadeOutHandler;
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
	
	public FadeOutHandler getFadeOutHandler()
	{
		return fadeOutHandler;
	}
	
	public void setResourceLocation(ResourceLocation resourceLocation)
	{
		this.location = resourceLocation;
	}
	
	public static double distanceSize(double distance)
	{
		return 1 / distance;
	}
	
	public static float dayBrightness(float size, long ticks, ClientLevel level, Camera camera, float partialTicks)
	{
		if(StellarViewConfig.day_stars.get())
			return StellarViewConfig.bright_stars.get() ? 0.5F * StellarView.lightSourceDimming(level, camera) : 0.5F;
		
		float brightness = level.getStarBrightness(partialTicks) * 2;
		
		if(brightness < MIN_VISIBLE_BRIGHTNESS && size > MIN_VISIBLE_SIZE)
		{
			float aboveSize = size >= MAX_VISIBLE_SIZE ? VISIBLE_SIZE_RANGE : size - MIN_VISIBLE_SIZE;
			float brightnessPercentage = aboveSize / VISIBLE_SIZE_RANGE;
			
			brightness = brightnessPercentage * 0.25F;
		}
		
		if(StellarViewConfig.bright_stars.get())
			brightness = brightness * StellarView.lightSourceDimming(level, camera);
		
		return brightness * StellarView.rainDimming(level, partialTicks);
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
	
	
	/**
	 * Method for rendering an individual texture layer, override to change details of how this object's texture layers are rendered
	 * @param textureLayer
	 * @param level
	 * @param bufferbuilder
	 * @param lastMatrix
	 * @param sphericalCoords
	 * @param ticks
	 * @param distance
	 * @param partialTicks
	 */
	protected void renderTextureLayer(TextureLayer textureLayer, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		if(textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
				size = (float) textureLayer.minSize();
			else
				return;
		}
		
		float rotation = (float) textureLayer.rotation();
		
		Vector3f corner00 = StellarCoordinates.placeOnSphere(-size, -size, sphericalCoords, rotation);
		Vector3f corner10 = StellarCoordinates.placeOnSphere(size, -size, sphericalCoords, rotation);
		Vector3f corner11 = StellarCoordinates.placeOnSphere(size, size, sphericalCoords, rotation);
		Vector3f corner01 = StellarCoordinates.placeOnSphere(-size, size, sphericalCoords, rotation);
	
	
		if(textureLayer.shoulBlend())
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		else
			RenderSystem.defaultBlendFunc();
		
		RenderSystem.setShaderColor(textureLayer.rgba().red() / 255F, textureLayer.rgba().green() / 255F, textureLayer.rgba().blue() / 255F, dayBrightness(size, ticks, level, camera, partialTicks) * textureLayer.rgba().alpha() / 255F);
		
		RenderSystem.setShaderTexture(0, textureLayer.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(textureLayer.uv().topRight().u(ticks), textureLayer.uv().topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(textureLayer.uv().bottomRight().u(ticks), textureLayer.uv().bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(textureLayer.uv().bottomLeft().u(ticks), textureLayer.uv().bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(textureLayer.uv().topLeft().u(ticks), textureLayer.uv().topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
        RenderSystem.defaultBlendFunc();
	}
	
	protected void renderTextureLayers(ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : textureLayers)
		{
			renderTextureLayer(textureLayer, level, camera, bufferbuilder, lastMatrix, sphericalCoords, ticks, distance, partialTicks);
		}
	}
	
	
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, 
			Vector3f parentVector)
	{
		long ticks = level.getDayTime();
		
		Vector3f positionVector = getPosition(ticks).add(parentVector); // Handles orbits 'n stuff
		
		// Add parent vector to current coords
		SpaceCoords coords = getCoords().add(positionVector);
		
		// Subtract coords of this from View Center coords to get relative coords
		SphericalCoords sphericalCoords = coords.skyPosition(viewCenter.getCoords());
		
		lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		// Render children behind the parent
		for(SpaceObject child : children) //TODO
		{
			if(child.lastDistance >= this.lastDistance)
				child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector);
		}
		
		// If the object isn't the same we're viewing everything from and it itsn't too far away, render it
		if(!viewCenter.objectEquals(this) && getFadeOutHandler().getFadeOutEndDistance().toKm() > lastDistance)
			renderTextureLayers(level, camera, bufferbuilder, stack.last().pose(), sphericalCoords, ticks, lastDistance, partialTicks);
		
		if(getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			// Render children in front of the parent
			for(SpaceObject child : children)
			{
				if(child.lastDistance < this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector);
			}
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
			viewCenter.renderSkyObjects(this, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	@Override
	public String toString()
	{
		if(location != null)
			return location.toString();
		
		return this.getClass().toString();
	}
	
	
	
	public static class FadeOutHandler
	{
		public static final FadeOutHandler DEFAULT_PLANET_HANDLER = new FadeOutHandler(new SpaceDistance(70000000000D), new SpaceDistance(100000000000D), new SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_HANDLER = new FadeOutHandler(new SpaceDistance(10000000L), new SpaceDistance(50000000L), new SpaceDistance(50000000L));
		public static final FadeOutHandler DEFAULT_STAR_FIELD_HANDLER = new FadeOutHandler(new SpaceDistance(10000000L), new SpaceDistance(50000000L), new SpaceDistance(50000000L));
		
		private SpaceDistance fadeOutStartDistance;
		private SpaceDistance fadeOutEndDistance;
		private SpaceDistance maxChildRenderDistance;
		
		public static final Codec<FadeOutHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpaceDistance.CODEC.fieldOf("fade_out_start_distance").forGetter(FadeOutHandler::getFadeOutStartDistance),
				SpaceDistance.CODEC.fieldOf("fade_out_end_distance").forGetter(FadeOutHandler::getFadeOutEndDistance),
				SpaceDistance.CODEC.fieldOf("max_child_render_distance").forGetter(FadeOutHandler::getMaxChildRenderDistance)
				).apply(instance, FadeOutHandler::new));
		
		public FadeOutHandler(SpaceDistance fadeOutStartDistance, SpaceDistance fadeOutEndDistance, SpaceDistance maxChildRenderDistance)
		{
			this.fadeOutStartDistance = fadeOutStartDistance;
			this.fadeOutEndDistance = fadeOutEndDistance;
			this.maxChildRenderDistance = maxChildRenderDistance;
		}
		
		public SpaceDistance getFadeOutStartDistance()
		{
			return fadeOutStartDistance;
		}
		
		public SpaceDistance getFadeOutEndDistance()
		{
			return fadeOutEndDistance;
		}
		
		public SpaceDistance getMaxChildRenderDistance()
		{
			return maxChildRenderDistance;
		}
	}
}
