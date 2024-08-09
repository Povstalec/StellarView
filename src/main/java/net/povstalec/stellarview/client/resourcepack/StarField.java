package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
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
import net.povstalec.stellarview.api.celestials.Star;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StarBuffer;
import net.povstalec.stellarview.common.util.TextureLayer;

public abstract class StarField extends SpaceObject
{
	@Nullable
	protected StarBuffer starBuffer;
	protected StarInfo starInfo;
	
	protected final long seed;
	
	protected final int diameter;
	protected final int stars;
	
	public StarField(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
			List<TextureLayer> textureLayers, long seed, int diameter, int numberOfStars)
	{
		super(parent, coords, axisRotation, textureLayers);
		
		this.seed = seed;

		this.diameter = diameter;
		this.stars = numberOfStars;
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
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, Vector3f parentVector)
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
			
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, new Vector3f((float) difference.x().toLy(), (float) difference.y().toLy(), (float) difference.z().toLy()), StellarViewShaders.starShader());
			//this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexShader());
			VertexBuffer.unbind();
			
			setupFog.run();
			stack.popPose();
		}
		
		for(SpaceObject child : children)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector);
		}
	}
	
	
	
	public static class GlobularCluster extends StarField
	{
		public static final Codec<GlobularCluster> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(GlobularCluster::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(GlobularCluster::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(GlobularCluster::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(GlobularCluster::getTextureLayers),
				
				Codec.LONG.fieldOf("seed").forGetter(GlobularCluster::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(GlobularCluster::getDiameter),
				
				Codec.INT.fieldOf("stars").forGetter(GlobularCluster::getStars)
				).apply(instance, GlobularCluster::new));

		public GlobularCluster(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, long seed, int diameter, int numberOfStars)
		{
			super(parent, coords, axisRotation, textureLayers, seed, diameter, numberOfStars);
		}

		@Override
		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			starInfo = new StarInfo(stars);
			
			for(int i = 0; i < stars; i++)
			{
				// This generates random coordinates for the Star close to the camera
				Vector3d cartesian = new SphericalCoords(randomsource.nextDouble() * diameter, randomsource.nextDouble() * 2F * Math.PI, randomsource.nextDouble() * Math.PI).toCartesianD();
				
				starInfo.newStar(bufferBuilder, randomsource, relativeCoords, cartesian.x, cartesian.y, cartesian.z, i);
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
				starInfo.createStar(bufferBuilder, randomsource, relativeCoords, i);
			}
			return bufferBuilder.end();
		}
		
		
	}
}
