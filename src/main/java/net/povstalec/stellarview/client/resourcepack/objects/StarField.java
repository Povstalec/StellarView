package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.DustCloudInfo;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.client.resourcepack.StarInfo;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;

public class StarField extends SpaceObject
{
	public enum LevelOfDetail
	{
		LOD1((short) 225, 10000000L), // Very far away, most stars can't be seen
		LOD2((short) 190, 5000000L), // Middle point, some stars can be seen
		LOD3((short) 0, 0); // Very close, even the dimmest stars are seen
		
		short minBrightness;
		long minDistanceSquared;
		
		LevelOfDetail(short minBrightness, long minDistance)
		{
			this.minBrightness = minBrightness;
			this.minDistanceSquared = minDistance * minDistance;
		}
		
		public static LevelOfDetail fromBrightness(short brightness) // Majority of stars should be dim, so we're starting with LOD3
		{
			if(brightness < LOD2.minBrightness)
				return LOD3;
			
			if(brightness < LOD1.minBrightness)
				return LOD2;
			
			return LOD1;
		}
		
		public static LevelOfDetail fromDistance(long distance) // Majority of galaxies should be far away, so we're starting with LOD1
		{
			long distanceSquared = distance * distance;
			
			if(distanceSquared >= LOD1.minDistanceSquared)
				return LOD1;
			
			if(distanceSquared >= LOD2.minDistanceSquared)
				return LOD2;
			
			return LOD3;
		}
		
		public static LevelOfDetail fromDistance(SpaceCoords difference) // Majority of galaxies should be far away, so we're starting with LOD1
		{
			long distanceSquared = difference.lyDistanceSquared();
			
			if(distanceSquared >= LOD1.minDistanceSquared)
				return LOD1;
			
			if(distanceSquared >= LOD2.minDistanceSquared)
				return LOD2;
			
			return LOD3;
		}
	}
	
	public static final ResourceLocation DEFAULT_DUST_CLOUD_TEXTURE = ResourceLocation.fromNamespaceAndPath(StellarView.MODID,"textures/environment/dust_cloud.png");
	
	public static final String SEED = "seed";
	public static final String DIAMETER_LY = "diameter_ly";
	public static final String STARS = "stars";
	public static final String TOTAL_STARS = "total_stars";
	public static final String STAR_INFO = "star_info";
	public static final String SPIRAL_ARMS = "spiral_arms";
	public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
	public static final String X_STRETCH = "x_stretch";
	public static final String Y_STRETCH = "y_stretch";
	public static final String Z_STRETCH = "z_stretch";
	public static final String DUST_CLOUDS = "dust_clouds";
	public static final String TOTAL_DUST_CLOUDS = "total_dust_clouds";
	public static final String DUST_CLOUD_INFO = "dust_cloud_info";
	public static final String DUST_CLOUD_TEXTURE = "dust_cloud_texture";
	
	@Nullable
	protected DustCloudBuffer dustCloudBuffer;
	protected DustCloudData dustCloudData;
	protected int dustClouds;
	protected int totalDustClouds;
	protected ResourceLocation dustCloudTexture;
	
	protected StarData starData;

	protected StarInfo starInfo;
	protected DustCloudInfo dustCloudInfo;
	
	protected long seed;
	protected boolean clumpStarsInCenter;
	
	protected int diameter;
	protected int stars;
	protected int lod1stars = 0;
	protected int lod2stars = 0;
	
	protected double xStretch;
	protected double yStretch;
	protected double zStretch;
	
	protected ArrayList<SpiralArm> spiralArms;
	
	
	protected int totalLOD1stars;
	protected int totalLOD2stars;
	protected int totalStars;
	
	protected boolean hasTexture = GeneralConfig.textured_stars.get();
	
	public static final Codec<StarField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(StarField::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(StarField::getAxisRotation),

			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(StarField::getFadeOutHandler),
			
			Codec.intRange(0, 4000).optionalFieldOf("dust_clouds", 0).forGetter(StarField::getDustClouds),
			DustCloudInfo.CODEC.optionalFieldOf("dust_cloud_info", DustCloudInfo.DEFAULT_DUST_CLOUD_INFO).forGetter(StarField::getDustCloudInfo),
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_texture", DEFAULT_DUST_CLOUD_TEXTURE).forGetter(StarField::getDustCloudTexture),
			
			StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(StarField::getStarInfo),
			Codec.LONG.fieldOf("seed").forGetter(StarField::getSeed),
			Codec.INT.fieldOf("diameter_ly").forGetter(StarField::getDiameter),
			
			Codec.intRange(0, 30000).fieldOf("stars").forGetter(StarField::getStars),
			Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(StarField::clumpStarsInCenter),
			
			Codec.DOUBLE.optionalFieldOf("x_stretch", 1.0).forGetter(StarField::xStretch),
			Codec.DOUBLE.optionalFieldOf("y_stretch", 1.0).forGetter(StarField::yStretch),
			Codec.DOUBLE.optionalFieldOf("z_stretch", 1.0).forGetter(StarField::zStretch),
			
			SpiralArm.CODEC.listOf().optionalFieldOf("spiral_arms", new ArrayList<SpiralArm>()).forGetter(starField -> starField.spiralArms)
			).apply(instance, StarField::new));
	
	public StarField() {}
	
	public StarField(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
			FadeOutHandler fadeOutHandler, int dustClouds, DustCloudInfo dustCloudInfo, ResourceLocation dustCloudTexture, StarInfo starInfo, long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
			double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		super(parent, coords, axisRotation, fadeOutHandler);
		
		this.dustClouds = dustClouds;
		this.dustCloudInfo = dustCloudInfo;
		this.dustCloudTexture = dustCloudTexture;
		
		this.starInfo = starInfo;
		this.seed = seed;
		this.diameter = diameter;
		
		this.stars = numberOfStars;
		this.clumpStarsInCenter = clumpStarsInCenter;
		
		this.xStretch = xStretch;
		this.yStretch = yStretch;
		this.zStretch = zStretch;
		
		this.spiralArms = new ArrayList<SpiralArm>(spiralArms);
		
		this.totalStars = stars;
		setupLOD(starInfo);

		// Calculate the total amount of stars
		int totalDustClouds = dustClouds;
		for(SpiralArm arm : this.spiralArms)
		{
			arm.setupLOD(starInfo);
			this.totalStars += arm.armStars();
			this.totalLOD1stars += arm.lod1stars;
			this.totalLOD2stars += arm.lod2stars;
			
			totalDustClouds += arm.armDustClouds;
		}
		
		this.totalDustClouds = totalDustClouds;
	}
	
	public int getDustClouds()
	{
		return dustClouds;
	}
	
	public DustCloudInfo getDustCloudInfo()
	{
		return dustCloudInfo;
	}
	
	public ResourceLocation getDustCloudTexture()
	{
		return dustCloudTexture;
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
	
	public boolean clumpStarsInCenter()
	{
		return clumpStarsInCenter;
	}
	
	public double xStretch()
	{
		return xStretch;
	}
	
	public double yStretch()
	{
		return yStretch;
	}
	
	public double zStretch()
	{
		return zStretch;
	}
	
	public List<SpiralArm> getSpiralArms()
	{
		return spiralArms;
	}
	
	protected void setupLOD(StarInfo starInfo)
	{
		this.lod1stars = (int) (stars * ((float) starInfo.lod1Weight() / starInfo.totalWeight()));
		this.lod2stars = (int) (stars * ((float) starInfo.lod2Weight() / starInfo.totalWeight()));
		
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
	
	protected void generateStars(StarData.LOD lod, LevelOfDetail levelOfDetail, Random random)
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
				stars = this.stars - lod1stars - lod2stars;
		}
		
		for(int i = 0; i < stars; i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(Math.abs(random.nextDouble()));
			double theta = random.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= xStretch;
			cartesian.y *= yStretch;
			cartesian.z *= zStretch;
			
			axisRotation.quaterniond().transform(cartesian);
			
			switch(levelOfDetail)
			{
				case LOD1:
					lod.newStar(starInfo.randomLOD1StarType(random), random, cartesian.x, cartesian.y, cartesian.z, hasTexture);
					break;
				case LOD2:
					lod.newStar(starInfo.randomLOD2StarType(random), random, cartesian.x, cartesian.y, cartesian.z, hasTexture);
					break;
				default:
					lod.newStar(starInfo.randomLOD3StarType(random), random, cartesian.x, cartesian.y, cartesian.z, hasTexture);
			}
		}
	}
	
	protected void generateDustClouds(BufferBuilder bufferBuilder, Random random)
	{
		for(int i = 0; i < dustClouds; i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
			double theta = random.nextDouble() * 2F * Math.PI;
			double phi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
			
			Vector3d cartesian = new SphericalCoords(distance * getDiameter(), theta, phi).toCartesianD();
			
			cartesian.x *= xStretch;
			cartesian.y *= yStretch;
			cartesian.z *= zStretch;
			
			axisRotation.quaterniond().transform(cartesian);
			
			dustCloudData.newDustCloud(dustCloudInfo, bufferBuilder, random, cartesian.x, cartesian.y, cartesian.z, 1, i);
		}
	}
	
	protected void setStars()
	{
		double sizeMultiplier = diameter / 30D;
		
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
						random = new Random(getSeed());
						stars = totalLOD1stars;
						break;
					case LOD2:
						random = new Random(getSeed() + 1);
						stars = totalLOD2stars;
						break;
					default:
						random = new Random(getSeed() + 2);
						stars = totalStars - totalLOD1stars - totalLOD2stars;
				}
				LOD lod = new LOD(stars);
				
				generateStars(lod, levelOfDetail, random);
				
				for(SpiralArm arm : spiralArms) //Draw each arm
				{
					arm.generateStars(lod, levelOfDetail, axisRotation, starInfo, random, sizeMultiplier, hasTexture);
				}
				
				return lod;
			}
		};
	}
	
	
	
	protected MeshData generateDustCloudBuffer(Tesselator tesselator, Random random)
	{
		final var bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get());
		double sizeMultiplier = diameter / 30D;
		
		dustCloudData = new DustCloudData(totalDustClouds);
		
		generateDustClouds(bufferBuilder, random);
		
		int numberOfDustClouds = dustClouds;
		for(SpiralArm arm : spiralArms) //Draw each arm
		{
			arm.generateDustClouds(bufferBuilder, axisRotation, dustCloudData, dustCloudInfo, random, numberOfDustClouds, sizeMultiplier);
			numberOfDustClouds += arm.armDustClouds();
		}
		
		return bufferBuilder.build();
	}
	
	public StarField setupDustCloudBuffer()
	{
		if(dustCloudBuffer != null)
			dustCloudBuffer.close();
		
		dustCloudBuffer = new DustCloudBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		
		MeshData mesh = generateDustCloudBuffer(tesselator, new Random(getSeed()));
		
		if(mesh == null)
			return this;
		
		dustCloudBuffer.bind();
		dustCloudBuffer.upload(mesh);
		VertexBuffer.unbind();
		
		return this;
	}
	
	
	
	public void renderDustClouds(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
								 Matrix4f projectionMatrix, Runnable setupFog, float brightness)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(getCoords());
		
		if(dustCloudBuffer == null)
			setupDustCloudBuffer();
		
		if(brightness > 0.0F && totalDustClouds > 0)
		{
			final var transformedModelView = new Matrix4f(modelViewMatrix);
			
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, brightness);
			RenderSystem.setShaderTexture(0, getDustCloudTexture());
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			transformedModelView.rotate(q);
			this.dustCloudBuffer.bind();
			this.dustCloudBuffer.drawWithShader(transformedModelView, projectionMatrix, difference, StellarViewShaders.starDustCloudShader());
			VertexBuffer.unbind();
			
			setupFog.run();
		}
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator,
			Vector3f parentVector, AxisRotation parentRotation)
	{
		SpaceCoords difference = viewCenter.getCoords().sub(getCoords());
		
		if(starData == null)
			setStars();
		else if(requiresReset())
			starData.reset();
		
		float starBrightness = StarLike.getStarBrightness(viewCenter, level, camera, partialTicks);
		
		if(!GeneralConfig.disable_stars.get() && starBrightness > 0.0F && totalStars > 0)
		{
			final var transformedModelView = new Matrix4f(modelViewMatrix);
			
			//stack.translate(0, 0, 0);
			if(hasTexture)
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			if(hasTexture)
				RenderSystem.setShaderTexture(0, getStarInfo().getStarTexture());
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			transformedModelView.rotate(q);
			this.starData.renderStars(LevelOfDetail.fromDistance(difference), transformedModelView, projectionMatrix, difference, hasTexture);
			
			setupFog.run();
		}
		
		for(SpaceObject child : children)
		{
			child.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, parentVector, new AxisRotation(0, 0, 0));
		}
	}
	
	public static float dustCloudBrightness(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = level.getStarBrightness(partialTicks);
		
		if(viewCenter.starsAlwaysVisible() && brightness < 0.5F)
			brightness = 0.5F;
		
		if(GeneralConfig.bright_stars.get())
			brightness = brightness * StellarView.lightSourceDustCloudDimming(level, camera);
		
		brightness = brightness * StellarView.rainDimming(level, partialTicks);
		
		return brightness;
	}
	
	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		
		this.starInfo = StarInfo.fromTag(tag.getCompound(STAR_INFO));
		
		dustClouds = tag.getInt(DUST_CLOUDS);
		totalDustClouds = tag.getInt(TOTAL_DUST_CLOUDS);
		dustCloudTexture = ResourceLocation.parse(tag.getString(DUST_CLOUD_TEXTURE));
		
		seed = tag.getLong(SEED);
		
		diameter = tag.getInt(DIAMETER_LY);
		stars = tag.getInt(STARS);
		totalStars = tag.getInt(TOTAL_STARS);
		setupLOD(starInfo);
		
		clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
		
		xStretch = tag.getDouble(X_STRETCH);
		yStretch = tag.getDouble(Y_STRETCH);
		zStretch = tag.getDouble(Z_STRETCH);
		
		this.spiralArms = new ArrayList<SpiralArm>();
		CompoundTag armsTag = tag.getCompound(SPIRAL_ARMS);
		for(int i = 0; i < armsTag.size(); i++)
		{
			SpiralArm arm = new SpiralArm();
			arm.fromTag(armsTag.getCompound("spiral_arm_" + i));
			arm.setupLOD(starInfo);
			spiralArms.add(arm);
			this.totalLOD1stars += arm.lod1stars;
			this.totalLOD2stars += arm.lod2stars;
		}
		
		this.dustCloudInfo = DustCloudInfo.fromTag(tag.getCompound(DUST_CLOUD_INFO));
	}
	
	
	
	public static class SpiralArm
	{
		public static final String STARS = "stars";
		public static final String ARM_ROTATION = "arm_rotation";
		public static final String ARM_LENGTH = "arm_length";
		public static final String ARM_THICKNESS = "arm_thickness";
		public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
		
		@Nullable
		protected DustCloudInfo dustCloudInfo;
		protected int armDustClouds;
		
		protected int armStars;
		protected int lod1stars = 0;
		protected int lod2stars = 0;
		
		protected double armRotation;
		protected double armLength;
		protected double armThickness;
		protected boolean clumpStarsInCenter;
		
		public static final Codec<SpiralArm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf(DUST_CLOUDS, 0).forGetter(SpiralArm::armDustClouds),
				DustCloudInfo.CODEC.optionalFieldOf(DUST_CLOUD_INFO).forGetter(SpiralArm::getDustCloudInfo),
				
				Codec.INT.fieldOf(STARS).forGetter(SpiralArm::armStars),
				Codec.DOUBLE.fieldOf(ARM_ROTATION).forGetter(SpiralArm::armRotation),
				Codec.DOUBLE.fieldOf(ARM_LENGTH).forGetter(SpiralArm::armLength),
				Codec.DOUBLE.fieldOf(ARM_THICKNESS).forGetter(SpiralArm::armThickness),
				Codec.BOOL.optionalFieldOf(CLUMP_STARS_IN_CENTER, true).forGetter(SpiralArm::clumpStarsInCenter)
		).apply(instance, SpiralArm::new));
		
		public SpiralArm() {}
		
		public SpiralArm(int armDustClouds, Optional<DustCloudInfo> dustCloudInfo, int armStars, double armRotationDegrees, double armLength, double armThickness, boolean clumpStarsInCenter)
		{
			this.armDustClouds = armDustClouds;
			if(dustCloudInfo.isPresent())
				this.dustCloudInfo = dustCloudInfo.get();
			else
				this.dustCloudInfo = null;
			
			this.armStars = armStars;
			this.armRotation = Math.toRadians(armRotationDegrees);
			this.armLength = armLength;
			this.armThickness = armThickness;
			
			this.clumpStarsInCenter = clumpStarsInCenter;
		}
		
		public int armDustClouds()
		{
			return armDustClouds;
		}
		
		public Optional<DustCloudInfo> getDustCloudInfo()
		{
			return Optional.ofNullable(dustCloudInfo);
		}
		
		public int armStars()
		{
			return armStars;
		}
		
		public double armRotation()
		{
			return armRotation;
		}
		
		public double armLength()
		{
			return armLength;
		}
		
		public double armThickness()
		{
			return armThickness;
		}
		
		public boolean clumpStarsInCenter()
		{
			return clumpStarsInCenter;
		}
		
		protected void setupLOD(StarInfo starInfo)
		{
			this.lod1stars = (int) (armStars * ((float) starInfo.lod1Weight() / starInfo.totalWeight()));
			this.lod2stars = (int) (armStars * ((float) starInfo.lod2Weight() / starInfo.totalWeight()));
		}
		
		protected void generateStars(StarData.LOD lod, LevelOfDetail levelOfDetail, AxisRotation axisRotation, StarInfo starInfo, Random random, double sizeMultiplier, boolean hasTexture)
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
				stars = armStars - lod1stars - lod2stars;
			}
			
			for(int i = 0; i < stars; i++)
			{
				// Milky Way is 90 000 ly across
				
				double progress = (double) i / stars;
				
				double phi = armLength * Math.PI * progress - armRotation;
				double r = StellarCoordinates.spiralR(5, phi, armRotation);
				
				// This generates random coordinates for the Star close to the camera
				double distance = clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
				double theta = random.nextDouble() * 2F * Math.PI;
				double sphericalphi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens

				Vector3d cartesian = new SphericalCoords(distance * armThickness(), theta, sphericalphi).toCartesianD();
				
				double x =  r * Math.cos(phi) + cartesian.x * armThickness() / (progress * 1.5);
				double z =  r * Math.sin(phi) + cartesian.z * armThickness() / (progress * 1.5);
				double y =  cartesian.y * armThickness() / (progress * 1.5);
				
				cartesian.x = x * sizeMultiplier;
				cartesian.y = y * sizeMultiplier;
				cartesian.z = z * sizeMultiplier;
				
				axisRotation.quaterniond().transform(cartesian);
				
				switch(levelOfDetail)
				{
					case LOD1:
						lod.newStar(starInfo.randomLOD1StarType(random), random, cartesian.x, cartesian.y, cartesian.z, hasTexture);
						break;
					case LOD2:
						lod.newStar(starInfo.randomLOD2StarType(random), random, cartesian.x, cartesian.y, cartesian.z, hasTexture);
						break;
					default:
						lod.newStar(starInfo.randomLOD3StarType(random), random, cartesian.x, cartesian.y, cartesian.z, hasTexture);
				}
			}
		}
		
		protected void generateDustClouds(BufferBuilder bufferBuilder, AxisRotation axisRotation, DustCloudData dustCloudData, DustCloudInfo dustCloudInfo, Random random, int numberOfDustClouds, double sizeMultiplier)
		{
			for(int i = 0; i < armDustClouds; i++)
			{
				// Milky Way is 90 000 ly across
				
				double progress = (double) i / armDustClouds;
				
				double phi = armLength * Math.PI * progress - armRotation;
				double r = StellarCoordinates.spiralR(5, phi, armRotation);
				progress++;
				
				// This generates random coordinates for the Star close to the camera
				double distance = clumpStarsInCenter() ? random.nextDouble() : Math.cbrt(random.nextDouble());
				double theta = random.nextDouble() * 2F * Math.PI;
				double sphericalphi = Math.acos(2F * random.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
				
				Vector3d cartesian = new SphericalCoords(distance * armThickness(), theta, sphericalphi).toCartesianD();
				
				double x =  r * Math.cos(phi) + cartesian.x * armThickness() / (progress * 1.5);
				double z =  r * Math.sin(phi) + cartesian.z * armThickness() / (progress * 1.5);
				double y =  cartesian.y * armThickness() / (progress * 1.5);
				
				cartesian.x = x * sizeMultiplier;
				cartesian.y = y * sizeMultiplier;
				cartesian.z = z * sizeMultiplier;
				
				axisRotation.quaterniond().transform(cartesian);
				
				dustCloudData.newDustCloud(this.dustCloudInfo == null ? dustCloudInfo : this.dustCloudInfo, bufferBuilder, random, cartesian.x, cartesian.y, cartesian.z, (1 / progress) + 0.2, numberOfDustClouds + i);
			}
		}
		
		public void fromTag(CompoundTag tag)
		{
			armDustClouds = tag.getInt(DUST_CLOUDS);
			
			if(tag.contains(DUST_CLOUD_INFO))
				dustCloudInfo = DustCloudInfo.fromTag(tag.getCompound(DUST_CLOUD_INFO));
			else
				dustCloudInfo = null;
			
			armStars = tag.getInt(STARS);
			
			armRotation = tag.getDouble(ARM_ROTATION);
			armLength = tag.getDouble(ARM_LENGTH);
			armThickness = tag.getDouble(ARM_THICKNESS);
			
			clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
		}
	}
}
