package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.povstalec.stellarview.common.config.GeneralConfig;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.common.util.UV;

public abstract class TexturedObject extends SpaceObject
{
	public static final String TEXTURE_LAYERS = "texture_layers";
	
	protected ArrayList<TextureLayer> textureLayers;
	protected FadeOutHandler fadeOutHandler;
	
	public TexturedObject()
	{
		textureLayers = new ArrayList<TextureLayer>();
	}
	
	public TexturedObject(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords,
			AxisRotation axisRotation, List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler)
	{
		super(parent, coords, axisRotation);
		
		this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
		this.fadeOutHandler = fadeOutHandler;
	}
	
	public ArrayList<TextureLayer> getTextureLayers()
	{
		return textureLayers;
	}
	
	public FadeOutHandler getFadeOutHandler()
	{
		return fadeOutHandler;
	}
	
	public double fadeOut(double distance)
	{
		double fadeOutEnd = getFadeOutHandler().getFadeOutEndDistance().toKm();
		if(distance > fadeOutEnd)
			return 0;
		
		double fadeOutStart = getFadeOutHandler().getFadeOutStartDistance().toKm();
		if(distance < fadeOutStart)
			return 1;
		
		return (distance - fadeOutStart) / (fadeOutEnd - fadeOutStart);
	}
	
	
	public static void renderOnSphere(Color.FloatRGBA rgba, Color.FloatRGBA secondaryRGBA, ResourceLocation texture, UV.Quad uv,
			ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords,
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
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(uv.topRight().u(ticks), uv.topRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(uv.bottomRight().u(ticks), uv.bottomRight().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(uv.bottomLeft().u(ticks), uv.bottomLeft().v(ticks)).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(uv.topLeft().u(ticks), uv.topLeft().v(ticks)).endVertex();
        
        BufferUploader.drawWithShader(bufferbuilder.end());
        
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
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords, double fade, long ticks, double distance, float partialTicks)
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
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade,
				size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	protected void renderTextureLayers(ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double fade = fadeOut(distance);
		
		if(fade <= 0)
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		for(TextureLayer textureLayer : textureLayers)
		{
			renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, fade, ticks, distance, partialTicks);
		}
	}
	
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, 
			Vector3f parentVector, AxisRotation parentRotation)
	{
		Vector3f positionVector = getPosition(viewCenter, parentRotation, viewCenter.ticks(), partialTicks).add(parentVector); // Handles orbits 'n stuff

		// Add parent vector to current coords
		SpaceCoords coords = getCoords().add(positionVector);

		// Subtract coords of this from View Center coords to get relative coords
		SphericalCoords sphericalCoords = coords.skyPosition(level, viewCenter, partialTicks, true);
		
		lastDistance = sphericalCoords.r;
		sphericalCoords.r = DEFAULT_DISTANCE;
		
		double childRenderDistance = getFadeOutHandler().getMaxChildRenderDistance().toKm();
		if(childRenderDistance > lastDistance)
		{
			for(SpaceObject child : children)
			{
				// Render child behind the parent
				if(child.lastDistance >= this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, this.axisRotation);
			}
		}
		
		// If the object isn't the same we're viewing everything from and it isn't too far away, render it
		if(!viewCenter.objectEquals(this))
			renderTextureLayers(viewCenter, level, camera, bufferbuilder, stack.last().pose(), sphericalCoords, viewCenter.ticks(), lastDistance, partialTicks);
		
		if(childRenderDistance > lastDistance)
		{
			for(SpaceObject child : children)
			{
				// Render child in front of the parent
				if(child.lastDistance < this.lastDistance)
					child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, positionVector, this.axisRotation);
			}
		}
	}
	
	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		
		// Deserialize Texture Layers
		CompoundTag textureLayerTag = tag.getCompound(TEXTURE_LAYERS);
		for(int i = 0; i < textureLayerTag.size(); i++)
		{
			textureLayers.add(TextureLayer.fromTag(textureLayerTag.getCompound(String.valueOf(i))));
		}
		
		this.fadeOutHandler = FadeOutHandler.fromTag(tag.getCompound(FADE_OUT_HANDLER));
	}
	
	
	
	public static class FadeOutHandler
	{
		public static final String FADE_OUT_START_DISTANCE = "fade_out_start_distance";
		public static final String FADE_OUT_END_DISTANCE = "fade_out_end_distance";
		public static final String MAX_CHILD_RENDER_DISTANCE = "max_child_render_distance";
		
		public static final FadeOutHandler DEFAULT_PLANET_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(70000000000D), new SpaceCoords.SpaceDistance(100000000000D), new SpaceCoords.SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_STAR_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(5000000L), new SpaceCoords.SpaceDistance(10000000L), new SpaceCoords.SpaceDistance(100000000000D));
		public static final FadeOutHandler DEFAULT_NEBULA_HANDLER = new FadeOutHandler(new SpaceCoords.SpaceDistance(1000000L), new SpaceCoords.SpaceDistance(2000000L), new SpaceCoords.SpaceDistance(5000000L));
		
		private SpaceCoords.SpaceDistance fadeOutStartDistance;
		private SpaceCoords.SpaceDistance fadeOutEndDistance;
		private SpaceCoords.SpaceDistance maxChildRenderDistance;
		
		public static final Codec<FadeOutHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SpaceCoords.SpaceDistance.CODEC.fieldOf(FADE_OUT_START_DISTANCE).forGetter(FadeOutHandler::getFadeOutStartDistance),
				SpaceCoords.SpaceDistance.CODEC.fieldOf(FADE_OUT_END_DISTANCE).forGetter(FadeOutHandler::getFadeOutEndDistance),
				SpaceCoords.SpaceDistance.CODEC.fieldOf(MAX_CHILD_RENDER_DISTANCE).forGetter(FadeOutHandler::getMaxChildRenderDistance)
		).apply(instance, FadeOutHandler::new));
		
		public FadeOutHandler(SpaceCoords.SpaceDistance fadeOutStartDistance, SpaceCoords.SpaceDistance fadeOutEndDistance, SpaceCoords.SpaceDistance maxChildRenderDistance)
		{
			this.fadeOutStartDistance = fadeOutStartDistance;
			this.fadeOutEndDistance = fadeOutEndDistance;
			this.maxChildRenderDistance = maxChildRenderDistance;
		}
		
		public SpaceCoords.SpaceDistance getFadeOutStartDistance()
		{
			return fadeOutStartDistance;
		}
		
		public SpaceCoords.SpaceDistance getFadeOutEndDistance()
		{
			return fadeOutEndDistance;
		}
		
		public SpaceCoords.SpaceDistance getMaxChildRenderDistance()
		{
			return maxChildRenderDistance;
		}
		
		public static FadeOutHandler fromTag(CompoundTag tag)
		{
			return new FadeOutHandler(SpaceCoords.SpaceDistance.fromTag(tag.getCompound(FADE_OUT_START_DISTANCE)), SpaceCoords.SpaceDistance.fromTag(tag.getCompound(FADE_OUT_END_DISTANCE)), SpaceCoords.SpaceDistance.fromTag(tag.getCompound(MAX_CHILD_RENDER_DISTANCE)));
		}
	}
}
