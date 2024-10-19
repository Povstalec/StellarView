package net.povstalec.stellarview.client.resourcepack.objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TexturedObject extends SpaceObject
{
	protected ArrayList<TextureLayer> textureLayers;
	
	public TexturedObject(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords,
			AxisRotation axisRotation, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation, fadeOutHandler);
		
		this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
	}
	
	public ArrayList<TextureLayer> getTextureLayers()
	{
		return textureLayers;
	}
	
	public static void renderOnSphere(Color.FloatRGBA rgba, Color.FloatRGBA secondaryRGBA, ResourceLocation texture, UV.Quad uv,
									  ClientLevel level, Camera camera, Tesselator tesselator, Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  long ticks, double distance, float partialTicks, float brightness, float size, float rotation, boolean shouldBlend)
	{
		Vector3f corner00 = new Vector3f(size, DEFAULT_DISTANCE, size);
		Vector3f corner10 = new Vector3f(-size, DEFAULT_DISTANCE, size);
		Vector3f corner11 = new Vector3f(-size, DEFAULT_DISTANCE, -size);
		Vector3f corner01 = new Vector3f(size, DEFAULT_DISTANCE, -size);
		
		Quaterniond quaternionX = new Quaterniond().rotateY(sphericalCoords.theta);
		quaternionX.mul(new Quaterniond().rotateX(sphericalCoords.phi));
		quaternionX.mul(new Quaterniond().rotateY(rotation));
		
		quaternionX.transform(corner00);
		quaternionX.transform(corner10);
		quaternionX.transform(corner11);
		quaternionX.transform(corner01);
		
		if(shouldBlend)
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		else
			RenderSystem.defaultBlendFunc();
		
		RenderSystem.setShaderColor(rgba.red() * secondaryRGBA.red(), rgba.green() * secondaryRGBA.green(), rgba.blue() * secondaryRGBA.blue(), brightness * rgba.alpha() * secondaryRGBA.alpha());
		
		RenderSystem.setShaderTexture(0, texture);
		final var bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		
		bufferbuilder.addVertex(lastMatrix, corner00.x, corner00.y, corner00.z).setUv(uv.topRight().u(ticks), uv.topRight().v(ticks));
		bufferbuilder.addVertex(lastMatrix, corner10.x, corner10.y, corner10.z).setUv(uv.bottomRight().u(ticks), uv.bottomRight().v(ticks));
		bufferbuilder.addVertex(lastMatrix, corner11.x, corner11.y, corner11.z).setUv(uv.bottomLeft().u(ticks), uv.bottomLeft().v(ticks));
		bufferbuilder.addVertex(lastMatrix, corner01.x, corner01.y, corner01.z).setUv(uv.topLeft().u(ticks), uv.topLeft().v(ticks));
		
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
		
		RenderSystem.defaultBlendFunc();
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
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, Tesselator tesselator, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
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
		
		renderOnSphere(textureLayer.rgba(), Color.FloatRGBA.DEFAULT, textureLayer.texture(), textureLayer.uv(),
				level, camera, tesselator, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	protected void renderTextureLayers(ViewCenter viewCenter, ClientLevel level, Camera camera, Tesselator tesselator, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		for(TextureLayer textureLayer : textureLayers)
		{
			renderTextureLayer(textureLayer, viewCenter, level, camera, tesselator, lastMatrix, sphericalCoords, ticks, distance, partialTicks);
		}
	}
	
	
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator,
			Vector3f parentVector, AxisRotation parentRotation)
	{
		long ticks = level.getDayTime();
		
		Vector3f positionVector = getPosition(viewCenter, parentRotation, ticks, partialTicks).add(parentVector); // Handles orbits 'n stuff

		// Add parent vector to current coords
		SpaceCoords coords = getCoords().add(positionVector);

		// Subtract coords of this from View Center coords to get relative coords
		SphericalCoords sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks);
		
		lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		if(getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			for(SpaceObject child : children)
			{
				// Render child behind the parent
				if(child.lastDistance >= this.lastDistance)
					child.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, positionVector, this.axisRotation);
			}
		}
		
		// If the object isn't the same we're viewing everything from and it isn't too far away, render it
		if(!viewCenter.objectEquals(this) && getFadeOutHandler().getFadeOutEndDistance().toKm() > lastDistance)
			renderTextureLayers(viewCenter, level, camera, tesselator, modelViewMatrix, sphericalCoords, ticks, lastDistance, partialTicks);
		
		if(getFadeOutHandler().getMaxChildRenderDistance().toKm() > lastDistance)
		{
			for(SpaceObject child : children)
			{
				// Render child in front of the parent
				if(child.lastDistance < this.lastDistance)
					child.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, positionVector, this.axisRotation);
			}
		}
	}
}
