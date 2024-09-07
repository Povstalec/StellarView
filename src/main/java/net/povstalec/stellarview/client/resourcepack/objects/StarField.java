package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.client.resourcepack.StarInfo;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StarBuffer;
import net.povstalec.stellarview.common.util.StarData;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class StarField extends SpaceObject
{
	@Nullable
	protected StarBuffer starBuffer;
	protected StarData starData;

	protected StarInfo starInfo;
	
	protected final long seed;
	
	protected final int diameter;
	protected final int stars;
	
	public StarField(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation, List<TextureLayer> textureLayers,
			FadeOutHandler fadeOutHandler, StarInfo starInfo, long seed, int diameter, int numberOfStars)
	{
		super(parent, coords, axisRotation, textureLayers, fadeOutHandler);
		
		this.starInfo = starInfo;
		
		this.seed = seed;

		this.diameter = diameter;
		this.stars = numberOfStars;
	}
	
	public StarInfo getStarInfo()
	{
		return starInfo;
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	public int getDiameter()
	{
		return diameter;
	}
	
	public int getStars()
	{
		return stars;
	}
	
	public boolean requiresSetup()
	{
		return starBuffer == null;
	}

	protected abstract BufferBuilder.RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords);
		
	protected abstract BufferBuilder.RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords);
	
	public StarField setStarBuffer(SpaceCoords relativeCoords)
	{
		if(starBuffer != null)
			starBuffer.close();
		
		starBuffer = new StarBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		bufferbuilder$renderedbuffer = getStarBuffer(bufferBuilder, relativeCoords);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return this;
	}
	
	public StarField setupBuffer(SpaceCoords relativeCoords)
	{
		if(starBuffer != null)
			starBuffer.close();
		
		starBuffer = new StarBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer;
		
		bufferbuilder$renderedbuffer = generateStarBuffer(bufferBuilder, relativeCoords);
		
		starBuffer.bind();
		starBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return this;
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder,
			Vector3f parentVector, AxisRotation parentRotation)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(getCoords());
		
		if(requiresSetup())
			setupBuffer(difference);
		//else
		//	setStarBuffer(difference); // This could be viable with fewer stars
		
		float starBrightness = Star.getStarBrightness(level, camera, partialTicks);
		
		if(starBrightness > 0.0F)
		{
			stack.pushPose();
			
			//stack.translate(0, 0, 0);
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			//RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/sun.png"));
			FogRenderer.setupNoFog();
			
			Quaternionf q = new Quaternionf();
			// Inverting so that we can view the world through the relative rotation of our view center
			viewCenter.getViewCenterAxisRotation().quaternionf().invert(q);
			
			stack.mulPose(q);
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, new Vector3f((float) difference.x().toLy(), (float) difference.y().toLy(), (float) difference.z().toLy()), StellarViewShaders.starShader());
			//this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexShader());
			VertexBuffer.unbind();
			
			setupFog.run();
			stack.popPose();
		}
		
		for(SpaceObject child : children)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector, new AxisRotation(0, 0, 0));
		}
	}
	
	
	
	public static class GlobularCluster extends StarField
	{
		public static final Codec<GlobularCluster> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(GlobularCluster::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(GlobularCluster::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(GlobularCluster::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(GlobularCluster::getTextureLayers),
				
				SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(GlobularCluster::getFadeOutHandler),

				StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(GlobularCluster::getStarInfo),
				Codec.LONG.fieldOf("seed").forGetter(GlobularCluster::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(GlobularCluster::getDiameter),
				
				Codec.INT.fieldOf("stars").forGetter(GlobularCluster::getStars)
				).apply(instance, GlobularCluster::new));

		public GlobularCluster(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler, StarInfo starInfo, long seed, int diameter, int numberOfStars)
		{
			super(parent, coords, axisRotation, textureLayers, fadeOutHandler, starInfo, seed, diameter, numberOfStars);
		}

		@Override
		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			starData = new StarData(stars);
			
			for(int i = 0; i < stars; i++)
			{
				// This generates random coordinates for the Star close to the camera
				double distance = randomsource.nextDouble(); // Stars will be spread evenly aroudn the sphere
				double theta = randomsource.nextDouble() * 2F * Math.PI;
				double phi = Math.acos(2F * randomsource.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
				
				Vector3d cartesian = new SphericalCoords(distance * diameter, theta, phi).toCartesianD();
				
				starData.newStar(starInfo, bufferBuilder, randomsource, relativeCoords, cartesian.x, cartesian.y, cartesian.z, i);
			}
			return bufferBuilder.end();
		}

		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			for(int i = 0; i < stars; i++)
			{
				starData.createStar(bufferBuilder, randomsource, relativeCoords, i);
			}
			return bufferBuilder.end();
		}
		
		
	}
}
