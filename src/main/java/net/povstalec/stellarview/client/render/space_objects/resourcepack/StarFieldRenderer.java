package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Constellation;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.client.render.StellarViewEffects;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.util.StarData;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.client.util.DustCloudData;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class StarFieldRenderer<T extends StarField> extends SpaceObjectRenderer<T>
{
	protected boolean hasTexture = GeneralConfig.textured_stars.get();
	
	@Nullable
	protected StarInfo starInfo;
	protected StarData starData;
	protected int lod1stars = 0;
	protected int lod2stars = 0;
	protected int lod3stars = 0;
	protected int totalLOD1stars;
	protected int totalLOD2stars;
	protected int totalLOD3stars;
	
	protected int[] armLod1stars;
	protected int[] armLod2stars;
	
	@Nullable
	protected DustCloudInfo dustCloudInfo;
	@Nullable
	protected DustCloudInfo[] armDustCloudInfo;
	protected DustCloudData dustCloudData;
	protected int totalDustClouds;
	
	protected ArrayList<Constellation.StarDefinition> lod1definedStars;
	protected ArrayList<Constellation.StarDefinition> lod2definedStars;
	protected ArrayList<Constellation.StarDefinition> lod3definedStars;
	
	public StarFieldRenderer(T starField)
	{
		super(starField);
		
		this.totalDustClouds = starField.getDustClouds();
		
		this.armDustCloudInfo = new DustCloudInfo[this.renderedObject.getSpiralArms().size()];
		this.armLod1stars = new int[this.renderedObject.getSpiralArms().size()];
		this.armLod2stars = new int[this.renderedObject.getSpiralArms().size()];
		
		this.lod1definedStars = new ArrayList<>();
		this.lod2definedStars = new ArrayList<>();
		this.lod3definedStars = new ArrayList<>();
	}
	
	@Override
	public void addChild(SpaceObjectRenderer<?> child)
	{
		super.addChild(child);
		
		if(child instanceof ConstellationRenderer<?> constellation)
			addConstellation(constellation.renderedObject());
	}
	
	public void addConstellation(Constellation constellation)
	{
		constellation.relativeStars();
		this.lod1definedStars.addAll(constellation.lod1stars());
		this.lod2definedStars.addAll(constellation.lod2stars());
		this.lod3definedStars.addAll(constellation.lod3stars());
	}
	
	protected void setupLOD(StarInfo starInfo)
	{
		this.lod1stars = (int) (renderedObject.getStars() * ((float) starInfo.lod1Weight() / starInfo.totalWeight()));
		this.lod2stars = (int) (renderedObject.getStars() * ((float) starInfo.lod2Weight() / starInfo.totalWeight()));
		this.lod3stars = renderedObject.getStars() - this.lod1stars - this.lod2stars;
		
		this.lod1stars += this.lod1definedStars.size();
		this.lod2stars += this.lod2definedStars.size();
		this.lod3stars += this.lod3definedStars.size();
		
		this.totalLOD1stars = this.lod1stars;
		this.totalLOD2stars = this.lod2stars;
		this.totalLOD3stars = this.lod3stars;
		
		int i = 0;
		for(StarField.SpiralArm arm : renderedObject.getSpiralArms())
		{
			armLod1stars[i] = (int) (arm.armStars() * ((float) starInfo.lod1Weight() / starInfo.totalWeight()));
			armLod2stars[i] = (int) (arm.armStars() * ((float) starInfo.lod2Weight() / starInfo.totalWeight()));
			
			this.totalLOD1stars += armLod1stars[i];
			this.totalLOD2stars += armLod2stars[i];
			this.totalLOD3stars += arm.armStars() - armLod1stars[i] - armLod2stars[i];
			
			this.totalDustClouds += arm.armDustClouds();
			
			i++;
		}
	}
	
	public boolean requiresReset()
	{
		return hasTexture != GeneralConfig.textured_stars.get();
	}
	
	public void reset()
	{
		if(starData != null)
			starData.reset();
		
		if(dustCloudData != null)
			dustCloudData.reset();
	}
	
	public void setStarInfo(StarInfo starInfo)
	{
		this.starInfo = starInfo;
	}
	
	public void setupStarInfo()
	{
		if(renderedObject().getStarInfo() != null && StellarViewEffects.hasStarInfo(renderedObject().getStarInfo()))
			setStarInfo(StellarViewEffects.getStarInfo(renderedObject().getStarInfo()));
	}
	
	public StarInfo getStarInfo()
	{
		if(starInfo == null)
			return StarInfo.DEFAULT_STAR_INFO;
		
		return starInfo;
	}
	
	public void setDustCloudInfo(DustCloudInfo dustCloudInfo)
	{
		this.dustCloudInfo = dustCloudInfo;
	}
	
	public void setupDustCloudInfo()
	{
		if(renderedObject().getDustCloudInfo() != null && StellarViewEffects.hasDustCloudInfo(renderedObject().getDustCloudInfo()))
			setDustCloudInfo(StellarViewEffects.getDustCloudInfo(renderedObject().getDustCloudInfo()));
		
		ResourceLocation location;
		for(int i = 0; i < armDustCloudInfo.length; i++)
		{
			location = renderedObject.getSpiralArms().get(i).dustCloudInfo();
			if(location != null && StellarViewEffects.hasDustCloudInfo(location))
				setArmDustCloudInfo(StellarViewEffects.getDustCloudInfo(location), i);
				
		}
	}
	
	public DustCloudInfo getDustCloudInfo()
	{
		if(dustCloudInfo == null)
			return DustCloudInfo.DEFAULT_DUST_CLOUD_INFO;
		
		return dustCloudInfo;
	}
	
	public void setArmDustCloudInfo(DustCloudInfo dustCloudInfo, int armIndex)
	{
		if(armIndex < 0 || armIndex >= armDustCloudInfo.length)
			return;
		
		armDustCloudInfo[armIndex] = dustCloudInfo;
	}
	
	public DustCloudInfo getArmDustCloudInfo(int armIndex)
	{
		if(armIndex < 0 || armIndex >= armDustCloudInfo.length)
			return null;
		
		if(armDustCloudInfo[armIndex] == null)
			return getDustCloudInfo();
		
		return armDustCloudInfo[armIndex];
	}
	
	@Override
	public void setupSpaceObject(ResourceLocation id)
	{
		super.setupSpaceObject(id);
		
		setupStarInfo();
		setupLOD(getStarInfo());
		
		setupDustCloudInfo();
	}
	
	//============================================================================================
	//*******************************************Stars********************************************
	//============================================================================================
	
	protected void generateStars(StarData.LOD lod, StarField.LevelOfDetail levelOfDetail, Random random, ArrayList<Constellation.StarDefinition> definedStars)
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
				stars = lod3stars;
		}
		
		for(Constellation.StarDefinition star : definedStars)
		{
			lod.newStar(star, this.spaceCoords());
		}
		
		for(int i = definedStars.size(); i < stars; i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = renderedObject.clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(Math.abs(random.nextDouble()));
			double theta = random.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * renderedObject.getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= renderedObject.starStretch().xStretch();
			cartesian.y *= renderedObject.starStretch().yStretch();
			cartesian.z *= renderedObject.starStretch().zStretch();
			
			renderedObject.getAxisRotation().quaterniond().transform(cartesian);
			
			switch(levelOfDetail)
			{
				case LOD1:
					lod.newStar(getStarInfo().randomLOD1StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
					break;
				case LOD2:
					lod.newStar(getStarInfo().randomLOD2StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
					break;
				default:
					lod.newStar(getStarInfo().randomLOD3StarType(random), random, cartesian.x, cartesian.y, cartesian.z);
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
				ArrayList<Constellation.StarDefinition> definedStars;
				
				switch(levelOfDetail)
				{
					case LOD1:
						random = new Random(renderedObject.getSeed());
						stars = totalLOD1stars;
						definedStars = lod1definedStars;
						break;
					case LOD2:
						random = new Random(renderedObject.getSeed() + 1);
						stars = totalLOD2stars;
						definedStars = lod2definedStars;
						break;
					default:
						random = new Random(renderedObject.getSeed() + 2);
						stars = totalLOD3stars;
						definedStars = lod3definedStars;
				}
				LOD lod = new LOD(stars);
				
				generateStars(lod, levelOfDetail, random, definedStars);
				
				int i = 0;
				for(StarField.SpiralArm arm : renderedObject.getSpiralArms()) //Draw each arm
				{
					generateArmStars(lod, levelOfDetail, renderedObject.getAxisRotation(), getStarInfo(), random, sizeMultiplier, hasTexture, arm, i);
					i++;
				}
				
				return lod;
			}
		};
	}
	
	//============================================================================================
	//****************************************Dust Clouds*****************************************
	//============================================================================================
	
	protected void generateDustClouds(DustCloudData.LOD lod, Random random)
	{
		for(int i = 0; i < renderedObject.getDustClouds(); i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = renderedObject.clumpDustCloudsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
			double theta = random.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * renderedObject.getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= renderedObject.dustCloudStretch().xStretch();
			cartesian.y *= renderedObject.dustCloudStretch().yStretch();
			cartesian.z *= renderedObject.dustCloudStretch().zStretch();
			
			renderedObject.getAxisRotation().quaterniond().transform(cartesian);
			
			lod.newDustCloud(getDustCloudInfo().getRandomDustCloudType(random), random, cartesian.x, cartesian.y, cartesian.z, 1);
		}
	}
	
	protected void generateArmDustClouds(DustCloudData.LOD lod, AxisRotation axisRotation, DustCloudInfo dustCloudInfo, Random random, double sizeMultiplier, StarField.SpiralArm arm, int armIndex)
	{
		for(int i = 0; i < arm.armDustClouds(); i++)
		{
			// Milky Way is 90 000 ly across
			
			double progress = (double) i / arm.armDustClouds();
			
			double phi = arm.armLength() * Math.PI * progress - arm.armRotation();
			double r = StellarCoordinates.spiralR(5, phi, arm.armRotation());
			progress++;
			
			// This generates random coordinates for the Star close to the camera
			double distance = arm.clumpDustCloudsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
			double theta = random.nextDouble() * 2F * Math.PI;
			double sphericalphi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * arm.armThickness(), theta, sphericalphi).toCartesianD();
			
			double x =  r * Math.cos(phi)/* + cartesian.x * arm.armThickness() / (progress * 1.5)*/;
			double z =  r * Math.sin(phi)/* + cartesian.z * arm.armThickness() / (progress * 1.5)*/;
			double y =  0/*cartesian.y * arm.armThickness() / (progress * 1.5)*/;
			
			cartesian.x = x * sizeMultiplier;
			cartesian.y = y * sizeMultiplier;
			cartesian.z = z * sizeMultiplier;
			
			axisRotation.quaterniond().transform(cartesian);
			
			lod.newDustCloud(getArmDustCloudInfo(armIndex) == null ? dustCloudInfo.getRandomDustCloudType(random) : getArmDustCloudInfo(armIndex).getRandomDustCloudType(random), random, cartesian.x, cartesian.y, cartesian.z, (1 / progress) + 0.2);
		}
	}
	
	protected void setDustClouds()
	{
		double sizeMultiplier = renderedObject.getDiameter() / 30D;
		
		dustCloudData = new DustCloudData()
		{
			@Override
			protected LOD newDustClouds()
			{
				Random random;
				random = new Random(renderedObject.getSeed());
				
				LOD lod = new LOD(totalDustClouds);
				
				generateDustClouds(lod, random);
				
				for(int i = 0; i < renderedObject.getSpiralArms().size(); i++) //Draw each arm
				{
					generateArmDustClouds(lod, renderedObject.getAxisRotation(), getDustCloudInfo(), random, sizeMultiplier, renderedObject.getSpiralArm(i), i);
				}
				
				return lod;
			}
		};
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, Vector3f parentVector, AxisRotation parentRotation)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(spaceCoords());
		
		if(starData == null)
			setStars();
		else if(requiresReset())
		{
			hasTexture = GeneralConfig.textured_stars.get();
			starData.reset();
		}
		
		if(!GeneralConfig.disable_stars.get() && viewCenter.starBrightness() > 0.0F)
		{
			stack.pushPose();
			
			//stack.translate(0, 0, 0);
			if(hasTexture)
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, viewCenter.starBrightness());
			if(hasTexture)
				RenderSystem.setShaderTexture(0, renderedObject().getStarTexture());
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			stack.mulPose(q);
			this.starData.renderStars(StarField.LevelOfDetail.fromDistance(difference), stack.last().pose(), projectionMatrix, difference, viewCenter.isStatic(), hasTexture);
			
			setupFog.run();
			stack.popPose();
		}
		
		for(SpaceObjectRenderer<?> child : children)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector, new AxisRotation(0, 0, 0));
		}
	}
	
	public void renderDustClouds(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
								 Matrix4f projectionMatrix, Runnable setupFog, float brightness)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(spaceCoords());
		
		if(StarField.LevelOfDetail.fromDistance(difference) == StarField.LevelOfDetail.LOD1)
			return;
		
		if(dustCloudData == null)
			setDustClouds();
		else if(requiresReset())
			dustCloudData.reset();
		
		if(brightness > 0.0F)
		{
			stack.pushPose();
			
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, brightness);
			RenderSystem.setShaderTexture(0, renderedObject.getDustCloudTexture());
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			stack.mulPose(q);
			this.dustCloudData.renderDustClouds(stack.last().pose(), projectionMatrix, difference, viewCenter.isStatic());
			
			setupFog.run();
			stack.popPose();
		}
	}
}
