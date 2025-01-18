package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.util.DustCloudBuffer;
import net.povstalec.stellarview.client.util.StarData;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.DustCloudData;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Random;

public class StarFieldRenderer<T extends StarField> extends SpaceObjectRenderer<T>
{
	protected boolean hasTexture = GeneralConfig.textured_stars.get();
	
	protected StarData starData;
	protected int lod1stars = 0;
	protected int lod2stars = 0;
	protected int totalLOD1stars;
	protected int totalLOD2stars;
	protected int totalStars;
	
	protected int[] armLod1stars;
	protected int[] armLod2stars;
	
	
	protected DustCloudData dustCloudData;
	@Nullable
	protected DustCloudBuffer dustCloudBuffer;
	protected int totalDustClouds;
	
	public StarFieldRenderer(T starField)
	{
		super(starField);
		
		this.totalStars = starField.getStars();
		setupLOD(starField.getStarInfo());
		
		int totalDustClouds = starField.getDustClouds();
		
		armLod1stars = new int[renderedObject.getSpiralArms().size()];
		armLod2stars = new int[renderedObject.getSpiralArms().size()];
		int i = 0;
		for(StarField.SpiralArm arm : renderedObject.getSpiralArms())
		{
			armLod1stars[i] = (int) (arm.armStars() * ((float) starField.getStarInfo().lod1Weight() / starField.getStarInfo().totalWeight()));
			armLod2stars[i] = (int) (arm.armStars() * ((float) starField.getStarInfo().lod2Weight() / starField.getStarInfo().totalWeight()));
			
			this.totalStars += arm.armStars();
			this.totalLOD1stars += armLod1stars[i];
			this.totalLOD2stars += armLod2stars[i];
			
			totalDustClouds += arm.armDustClouds();
			
			i++;
		}
		
		this.totalDustClouds = totalDustClouds;
	}
	
	protected void setupLOD(StarInfo starInfo)
	{
		this.lod1stars = (int) (renderedObject.getStars() * ((float) starInfo.lod1Weight() / starInfo.totalWeight()));
		this.lod2stars = (int) (renderedObject.getStars() * ((float) starInfo.lod2Weight() / starInfo.totalWeight()));
		
		this.totalLOD1stars = this.lod1stars;
		this.totalLOD2stars = this.lod2stars;
	}
	
	public boolean requiresReset()
	{
		return hasTexture != GeneralConfig.textured_stars.get();
	}
	
	public void reset()
	{
		if(starData != null)
			starData.reset();
	}
	
	//============================================================================================
	//*******************************************Stars********************************************
	//============================================================================================
	
	protected void generateStars(StarData.LOD lod, StarField.LevelOfDetail levelOfDetail, Random random)
	{
		int stars;
		
		switch(levelOfDetail)
		{
			case LOD1:
				stars = lod1stars;
				break;
			case LOD2:
				stars = lod2stars;
				break;
			default:
				stars = renderedObject.getStars() - lod1stars - lod2stars;
		}
		
		for(int i = 0; i < stars; i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = renderedObject.clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(Math.abs(random.nextDouble()));
			double theta = random.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * renderedObject.getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= renderedObject.xStretch();
			cartesian.y *= renderedObject.yStretch();
			cartesian.z *= renderedObject.zStretch();
			
			renderedObject.getAxisRotation().quaterniond().transform(cartesian);
			
			switch(levelOfDetail)
			{
				case LOD1:
					lod.newStar(renderedObject.getStarInfo().randomLOD1StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
					break;
				case LOD2:
					lod.newStar(renderedObject.getStarInfo().randomLOD2StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
					break;
				default:
					lod.newStar(renderedObject.getStarInfo().randomLOD3StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
			}
		}
	}
	
	protected void generateArmStars(StarData.LOD lod, StarField.LevelOfDetail levelOfDetail, AxisRotation axisRotation, StarInfo starInfo, Random random, double sizeMultiplier, boolean hasTexture, StarField.SpiralArm arm, int armIndex)
	{
		int stars;
		
		switch(levelOfDetail)
		{
			case LOD1:
				stars = armLod1stars[armIndex];
				break;
			case LOD2:
				stars = armLod2stars[armIndex];
				break;
			default:
				stars = arm.armStars() - armLod1stars[armIndex] - armLod2stars[armIndex];
		}
		
		for(int i = 0; i < stars; i++)
		{
			// Milky Way is 90 000 ly across
			
			double progress = (double) i / stars;
			
			double phi = arm.armLength() * Math.PI * progress - arm.armRotation();
			double r = StellarCoordinates.spiralR(5, phi, arm.armRotation());
			
			// This generates random coordinates for the Star close to the camera
			double distance = arm.clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
			double theta = random.nextDouble() * 2F * Math.PI;
			double sphericalphi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * arm.armThickness(), theta, sphericalphi).toCartesianD();
			
			double x =  r * Math.cos(phi) + cartesian.x * arm.armThickness() / (progress * 1.5);
			double z =  r * Math.sin(phi) + cartesian.z * arm.armThickness() / (progress * 1.5);
			double y =  cartesian.y * arm.armThickness() / (progress * 1.5);
			
			cartesian.x = x * sizeMultiplier;
			cartesian.y = y * sizeMultiplier;
			cartesian.z = z * sizeMultiplier;
			
			axisRotation.quaterniond().transform(cartesian);
			
			switch(levelOfDetail)
			{
				case LOD1:
					lod.newStar(starInfo.randomLOD1StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
					break;
				case LOD2:
					lod.newStar(starInfo.randomLOD2StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
					break;
				default:
					lod.newStar(starInfo.randomLOD3StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
			}
		}
	}
	
	protected void setStars()
	{
		double sizeMultiplier = renderedObject.getDiameter() / 30D;
		
		starData = new StarData()
		{
			@Override
			protected LOD newStars(StarField.LevelOfDetail levelOfDetail)
			{
				Random random;
				int stars;
				
				switch(levelOfDetail)
				{
					case LOD1:
						random = new Random(renderedObject.getSeed());
						stars = totalLOD1stars;
						break;
					case LOD2:
						random = new Random(renderedObject.getSeed() + 1);
						stars = totalLOD2stars;
						break;
					default:
						random = new Random(renderedObject.getSeed() + 2);
						stars = totalStars - totalLOD1stars - totalLOD2stars;
				}
				LOD lod = new LOD(stars);
				
				generateStars(lod, levelOfDetail, random);
				
				int i = 0;
				for(StarField.SpiralArm arm : renderedObject.getSpiralArms()) //Draw each arm
				{
					generateArmStars(lod, levelOfDetail, renderedObject.getAxisRotation(), renderedObject.getStarInfo(), random, sizeMultiplier, hasTexture, arm, i);
					i++;
				}
				
				return lod;
			}
		};
	}
	
	//============================================================================================
	//****************************************Dust Clouds*****************************************
	//============================================================================================
	
	protected void generateDustClouds(BufferBuilder bufferBuilder, Random random)
	{
		for(int i = 0; i < renderedObject.getDustClouds(); i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = renderedObject.clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
			double theta = random.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * renderedObject.getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= renderedObject.xStretch();
			cartesian.y *= renderedObject.yStretch();
			cartesian.z *= renderedObject.zStretch();
			
			renderedObject.getAxisRotation().quaterniond().transform(cartesian);
			
			dustCloudData.newDustCloud(renderedObject.getDustCloudInfo(), bufferBuilder, random, cartesian.x, cartesian.y, cartesian.z, 1, i);
		}
	}
	
	protected void generateArmDustClouds(BufferBuilder bufferBuilder, AxisRotation axisRotation, DustCloudData dustCloudData, DustCloudInfo dustCloudInfo, Random random, int numberOfDustClouds, double sizeMultiplier, StarField.SpiralArm arm)
	{
		for(int i = 0; i < arm.armDustClouds(); i++)
		{
			// Milky Way is 90 000 ly across
			
			double progress = (double) i / arm.armDustClouds();
			
			double phi = arm.armLength() * Math.PI * progress - arm.armRotation();
			double r = StellarCoordinates.spiralR(5, phi, arm.armRotation());
			progress++;
			
			// This generates random coordinates for the Star close to the camera
			double distance = arm.clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
			double theta = random.nextDouble() * 2F * Math.PI;
			double sphericalphi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * arm.armThickness(), theta, sphericalphi).toCartesianD();
			
			double x =  r * Math.cos(phi) + cartesian.x * arm.armThickness() / (progress * 1.5);
			double z =  r * Math.sin(phi) + cartesian.z * arm.armThickness() / (progress * 1.5);
			double y =  cartesian.y * arm.armThickness() / (progress * 1.5);
			
			cartesian.x = x * sizeMultiplier;
			cartesian.y = y * sizeMultiplier;
			cartesian.z = z * sizeMultiplier;
			
			axisRotation.quaterniond().transform(cartesian);
			
			dustCloudData.newDustCloud(arm.dustCloudInfo() == null ? dustCloudInfo : arm.dustCloudInfo(), bufferBuilder, random, cartesian.x, cartesian.y, cartesian.z, (1 / progress) + 0.2, numberOfDustClouds + i);
		}
	}
	
	protected MeshData generateDustCloudBuffer(Tesselator tesselator, Random random)
	{
		final var bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get());
		double sizeMultiplier = renderedObject.getDiameter() / 30D;
		
		dustCloudData = new DustCloudData(totalDustClouds);
		
		generateDustClouds(bufferBuilder, random);
		
		int numberOfDustClouds = renderedObject.getDustClouds();
		for(StarField.SpiralArm arm :renderedObject.getSpiralArms()) //Draw each arm
		{
			generateArmDustClouds(bufferBuilder, renderedObject.getAxisRotation(), dustCloudData, renderedObject.getDustCloudInfo(), random, numberOfDustClouds, sizeMultiplier, arm);
			numberOfDustClouds += arm.armDustClouds();
		}
		
		return bufferBuilder.build();
	}
	
	public void setupDustCloudBuffer()
	{
		if(dustCloudBuffer != null)
			dustCloudBuffer.close();
		
		dustCloudBuffer = new DustCloudBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		
		MeshData mesh = generateDustCloudBuffer(tesselator, new Random(renderedObject.getSeed()));
		
		if(mesh == null)
			return;
		
		dustCloudBuffer.bind();
		dustCloudBuffer.upload(mesh);
		VertexBuffer.unbind();
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
					   Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator,
					   Vector3f parentVector, AxisRotation parentRotation)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(spaceCoords());
		
		if(starData == null)
			setStars();
		else if(requiresReset())
		{
			hasTexture = GeneralConfig.textured_stars.get();
			starData.reset();
		}
		
		float starBrightness = LightEffects.starBrightness(viewCenter, level, camera, partialTicks);
		
		if(!GeneralConfig.disable_stars.get() && starBrightness > 0.0F && totalStars > 0)
		{
			final var transformedModelView = new Matrix4f(modelViewMatrix);
			
			//stack.translate(0, 0, 0);
			if(hasTexture)
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			if(hasTexture)
				RenderSystem.setShaderTexture(0, renderedObject.getStarInfo().getStarTexture());
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			transformedModelView.rotate(q);
			this.starData.renderStars(StarField.LevelOfDetail.fromDistance(difference), transformedModelView, projectionMatrix, difference, hasTexture);
			
			setupFog.run();
		}
		
		for(SpaceObjectRenderer child : children)
		{
			child.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, parentVector, new AxisRotation(0, 0, 0));
		}
	}
	
	public void renderDustClouds(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
								 Matrix4f projectionMatrix, Runnable setupFog, float brightness)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(spaceCoords());
		
		if(dustCloudBuffer == null)
			setupDustCloudBuffer();
		
		if(brightness > 0.0F && totalDustClouds > 0)
		{
			final var transformedModelView = new Matrix4f(modelViewMatrix);
			
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, brightness);
			RenderSystem.setShaderTexture(0, renderedObject.getDustCloudTexture());
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			transformedModelView.rotate(q);
			this.dustCloudBuffer.bind();
			this.dustCloudBuffer.drawWithShader(transformedModelView, projectionMatrix, difference, StellarViewShaders.starDustCloudShader());
			VertexBuffer.unbind();
			
			setupFog.run();
		}
	}
}
