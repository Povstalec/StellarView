package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.common.util.*;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StarField extends SpaceObject
{
	public static final long LOD_DISTANCE_HIGH = 10000000L;
	public static final long LOD_DISTANCE_MEDIUM = 5000000L;
	public static final long LOD_DISTANCE_LOW = 0L;
	
	public enum LevelOfDetail
	{
		LOD1((short) 225, LOD_DISTANCE_HIGH), // Very far away, most stars can't be seen
		LOD2((short) 190, LOD_DISTANCE_MEDIUM), // Middle point, some stars can be seen
		LOD3((short) 0, LOD_DISTANCE_LOW); // Very close, even the dimmest stars are seen
		
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
	
	public static final ResourceLocation DEFAULT_STAR_TEXTURE = new ResourceLocation(StellarView.MODID,"textures/environment/star.png");
	public static final ResourceLocation DEFAULT_DUST_CLOUD_TEXTURE = new ResourceLocation(StellarView.MODID,"textures/environment/dust_cloud.png");
	
	public static final String DUST_CLOUDS = "dust_clouds";
	public static final String DUST_CLOUD_INFO = "dust_cloud_info";
	public static final String DUST_CLOUD_TEXTURE = "dust_cloud_texture";
	public static final String CLUMP_DUST_CLOUDS_IN_CENTER = "clump_dust_clouds_in_center";
	public static final String DUST_CLOUD_STRETCH = "dust_cloud_stretch";
	
	public static final String STARS = "stars";
	public static final String STAR_INFO = "star_info";
	public static final String STAR_TEXTURE = "star_texture";
	public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
	public static final String STAR_STRETCH = "star_stretch";
	
	public static final String SEED = "seed";
	public static final String DIAMETER_LY = "diameter_ly";
	public static final String SPIRAL_ARMS = "spiral_arms";
	
	protected int dustClouds;
	protected ResourceLocation dustCloudInfo;
	protected ResourceLocation dustCloudTexture;
	protected boolean clumpDustCloudsInCenter;
	protected Stretch dustCloudStretch;
	
	protected int stars;
	protected ResourceLocation starInfo;
	protected ResourceLocation starTexture;
	protected boolean clumpStarsInCenter;
	protected Stretch starStretch;
	
	protected long seed;
	protected int diameter;
	protected ArrayList<SpiralArm> spiralArms;
	
	public static final Codec<StarField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf(PARENT_LOCATION).forGetter(StarField::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf(AXIS_ROTATION).forGetter(StarField::getAxisRotation),
			
			Codec.intRange(0, 4000).optionalFieldOf(DUST_CLOUDS, 0).forGetter(StarField::getDustClouds),
			ResourceLocation.CODEC.optionalFieldOf(DUST_CLOUD_INFO).forGetter(starField -> Optional.ofNullable(starField.dustCloudInfo)),
			ResourceLocation.CODEC.optionalFieldOf(DUST_CLOUD_TEXTURE, DEFAULT_DUST_CLOUD_TEXTURE).forGetter(StarField::getDustCloudTexture),
			Codec.BOOL.optionalFieldOf(CLUMP_DUST_CLOUDS_IN_CENTER, true).forGetter(StarField::clumpDustCloudsInCenter),
			Stretch.CODEC.optionalFieldOf(DUST_CLOUD_STRETCH, Stretch.DEFAULT_STRETCH).forGetter(StarField::dustCloudStretch),
			
			Codec.intRange(0, 30000).fieldOf(STARS).forGetter(StarField::getStars),
			ResourceLocation.CODEC.optionalFieldOf(STAR_INFO).forGetter(starField -> Optional.ofNullable(starField.starInfo)),
			ResourceLocation.CODEC.optionalFieldOf(STAR_TEXTURE, DEFAULT_STAR_TEXTURE).forGetter(starField -> starField.starTexture),
			Codec.BOOL.optionalFieldOf(CLUMP_STARS_IN_CENTER, true).forGetter(StarField::clumpStarsInCenter),
			Stretch.CODEC.optionalFieldOf(STAR_STRETCH, Stretch.DEFAULT_STRETCH).forGetter(StarField::starStretch),
			
			Codec.LONG.fieldOf(SEED).forGetter(StarField::getSeed),
			Codec.INT.fieldOf(DIAMETER_LY).forGetter(StarField::getDiameter),
			SpiralArm.CODEC.listOf().optionalFieldOf(SPIRAL_ARMS, new ArrayList<SpiralArm>()).forGetter(starField -> starField.spiralArms)
			).apply(instance, StarField::new));
	
	public StarField() {}
	
	public StarField(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
					 int dustClouds, Optional<ResourceLocation> dustCloudInfo, ResourceLocation dustCloudTexture, boolean clumpDustCloudsInCenter, Stretch dustCloudStretch,
					 int stars, Optional<ResourceLocation> starInfo, ResourceLocation starTexture, boolean clumpStarsInCenter, Stretch starStretch,
					 long seed, int diameter, List<SpiralArm> spiralArms)
	{
		super(parent, coords, axisRotation);
		
		this.dustClouds = dustClouds;
		this.dustCloudInfo = dustCloudInfo.isPresent() ? dustCloudInfo.get() : null;
		this.dustCloudTexture = dustCloudTexture;
		this.clumpDustCloudsInCenter = clumpDustCloudsInCenter;
		this.dustCloudStretch = dustCloudStretch;
		
		this.stars = stars;
		this.starInfo = starInfo.isPresent() ? starInfo.get() : null;
		this.starTexture = starTexture;
		this.clumpStarsInCenter = clumpStarsInCenter;
		this.starStretch = starStretch;
		
		this.seed = seed;
		this.diameter = diameter;
		this.spiralArms = new ArrayList<SpiralArm>(spiralArms);
	}
	
	public int getDustClouds()
	{
		return dustClouds;
	}
	
	@Nullable
	public ResourceLocation getDustCloudInfo()
	{
		return dustCloudInfo;
	}
	
	public ResourceLocation getDustCloudTexture()
	{
		return dustCloudTexture;
	}
	
	@Nullable
	public ResourceLocation getStarInfo()
	{
		return starInfo;
	}
	
	public boolean clumpDustCloudsInCenter()
	{
		return clumpDustCloudsInCenter;
	}
	
	public Stretch dustCloudStretch()
	{
		return dustCloudStretch;
	}
	
	public ResourceLocation getStarTexture()
	{
		return starTexture;
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
	
	public Stretch starStretch()
	{
		return starStretch;
	}
	
	public List<SpiralArm> getSpiralArms()
	{
		return spiralArms;
	}
	
	public SpiralArm getSpiralArm(int armIndex)
	{
		if(armIndex < 0 || armIndex >= spiralArms.size())
			return null;
		
		return spiralArms.get(armIndex);
	}
	
	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = super.serializeNBT();
		
		tag.putInt(DUST_CLOUDS, dustClouds);
		tag.putString(DUST_CLOUD_TEXTURE, dustCloudTexture.toString());
		if(dustCloudInfo != null)
			tag.putString(DUST_CLOUD_INFO, dustCloudInfo.toString());
		tag.putBoolean(CLUMP_DUST_CLOUDS_IN_CENTER, clumpDustCloudsInCenter);
		tag.put(DUST_CLOUD_STRETCH, dustCloudStretch.serializeNBT());
		
		tag.putInt(STARS, stars);
		tag.putString(STAR_TEXTURE, starTexture.toString());
		if(starInfo != null)
			tag.putString(STAR_INFO, starInfo.toString());
		tag.putBoolean(CLUMP_STARS_IN_CENTER, clumpStarsInCenter);
		tag.put(STAR_STRETCH, starStretch.serializeNBT());
		
		tag.putLong(SEED, seed);
		tag.putInt(DIAMETER_LY, diameter);
		
		CompoundTag armsTag = new CompoundTag();
		for(int i = 0; i < spiralArms.size(); i++)
		{
			armsTag.put("spiral_arm_" + i, spiralArms.get(i).serializeNBT());
		}
		tag.put(SPIRAL_ARMS, armsTag);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		dustClouds = tag.getInt(DUST_CLOUDS);
		dustCloudTexture = new ResourceLocation(tag.getString(DUST_CLOUD_TEXTURE));
		dustCloudInfo = tag.contains(DUST_CLOUD_INFO) ? new ResourceLocation(tag.getString(DUST_CLOUD_INFO)) : null;
		clumpDustCloudsInCenter = tag.getBoolean(CLUMP_DUST_CLOUDS_IN_CENTER);
		dustCloudStretch = new Stretch();
		dustCloudStretch.deserializeNBT(tag.getCompound(DUST_CLOUD_STRETCH));
		
		stars = tag.getInt(STARS);
		starTexture = new ResourceLocation(tag.getString(STAR_TEXTURE));
		starInfo = tag.contains(STAR_INFO) ? new ResourceLocation(tag.getString(STAR_INFO)) : null;
		clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
		starStretch = new Stretch();
		starStretch.deserializeNBT(tag.getCompound(STAR_STRETCH));
		
		seed = tag.getLong(SEED);
		diameter = tag.getInt(DIAMETER_LY);
		
		this.spiralArms = new ArrayList<SpiralArm>();
		CompoundTag armsTag = tag.getCompound(SPIRAL_ARMS);
		for(String key : armsTag.getAllKeys())
		{
			SpiralArm arm = new SpiralArm();
			arm.deserializeNBT(armsTag.getCompound(key));
			spiralArms.add(arm);
		}
	}
	
	
	
	public static class SpiralArm implements INBTSerializable<CompoundTag>
	{
		public static final String STARS = "stars";
		public static final String ARM_ROTATION = "arm_rotation";
		public static final String ARM_LENGTH = "arm_length";
		public static final String ARM_THICKNESS = "arm_thickness";
		public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
		
		@Nullable
		protected ResourceLocation dustCloudInfo;
		protected int armDustClouds;
		
		protected int armStars;
		
		protected double armRotation;
		protected double armLength;
		protected double armThickness;
		protected boolean clumpStarsInCenter;
		protected boolean clumpDustCloudsInCenter;
		
		public static final Codec<SpiralArm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf(DUST_CLOUDS, 0).forGetter(SpiralArm::armDustClouds),
				ResourceLocation.CODEC.optionalFieldOf(DUST_CLOUD_INFO).forGetter(arm -> Optional.ofNullable(arm.dustCloudInfo)),
				
				Codec.INT.fieldOf(STARS).forGetter(SpiralArm::armStars),
				Codec.DOUBLE.fieldOf(ARM_ROTATION).forGetter(SpiralArm::armRotation),
				Codec.DOUBLE.fieldOf(ARM_LENGTH).forGetter(SpiralArm::armLength),
				Codec.DOUBLE.fieldOf(ARM_THICKNESS).forGetter(SpiralArm::armThickness),
				Codec.BOOL.optionalFieldOf(CLUMP_STARS_IN_CENTER, true).forGetter(SpiralArm::clumpStarsInCenter)
		).apply(instance, SpiralArm::new));
		
		public SpiralArm() {}
		
		public SpiralArm(int armDustClouds, Optional<ResourceLocation> dustCloudInfo, int armStars, double armRotationDegrees, double armLength, double armThickness, boolean clumpStarsInCenter)
		{
			this.armDustClouds = armDustClouds;
			this.dustCloudInfo = dustCloudInfo.isPresent() ? dustCloudInfo.get() : null;
			
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
		
		@Nullable
		public ResourceLocation dustCloudInfo()
		{
			return dustCloudInfo;
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
		
		public boolean clumpDustCloudsInCenter()
		{
			return clumpDustCloudsInCenter;
		}
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.putInt(DUST_CLOUDS, armDustClouds);
			
			if(dustCloudInfo != null)
				tag.putString(DUST_CLOUD_INFO, dustCloudInfo.toString());
			
			tag.putInt(STARS, armStars);
			
			tag.putDouble(ARM_ROTATION, armRotation);
			tag.putDouble(ARM_LENGTH, armLength);
			tag.putDouble(ARM_THICKNESS, armThickness);
			
			tag.putBoolean(CLUMP_STARS_IN_CENTER, clumpStarsInCenter);
			tag.putBoolean(CLUMP_DUST_CLOUDS_IN_CENTER, clumpDustCloudsInCenter);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			armDustClouds = tag.getInt(DUST_CLOUDS);
			
			dustCloudInfo = tag.contains(DUST_CLOUD_INFO) ? new ResourceLocation(tag.getString(DUST_CLOUD_INFO)) : null;
			
			armStars = tag.getInt(STARS);
			
			armRotation = tag.getDouble(ARM_ROTATION);
			armLength = tag.getDouble(ARM_LENGTH);
			armThickness = tag.getDouble(ARM_THICKNESS);
			
			clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
			clumpDustCloudsInCenter = tag.getBoolean(CLUMP_DUST_CLOUDS_IN_CENTER);
		}
	}
	
	
	
	public static class Stretch implements INBTSerializable<CompoundTag>
	{
		public static final String X_STRETCH = "x";
		public static final String Y_STRETCH = "y";
		public static final String Z_STRETCH = "z";
		
		protected double xStretch;
		protected double yStretch;
		protected double zStretch;
		
		public static final Stretch DEFAULT_STRETCH = new Stretch();
		
		public static final Codec<Stretch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.optionalFieldOf(X_STRETCH, 1.0).forGetter(stretch -> stretch.xStretch),
				Codec.DOUBLE.optionalFieldOf(Y_STRETCH, 1.0).forGetter(stretch -> stretch.yStretch),
				Codec.DOUBLE.optionalFieldOf(Z_STRETCH, 1.0).forGetter(stretch -> stretch.zStretch)
		).apply(instance, Stretch::new));
		
		public Stretch(double xStretch, double yStretch, double zStretch)
		{
			this.xStretch = xStretch;
			this.yStretch = yStretch;
			this.zStretch = zStretch;
		}
		
		public Stretch()
		{
			this(1, 1, 1);
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
		
		//============================================================================================
		//*************************************Saving and Loading*************************************
		//============================================================================================
		
		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag tag = new CompoundTag();
			
			tag.putDouble(X_STRETCH, xStretch);
			tag.putDouble(Y_STRETCH, yStretch);
			tag.putDouble(Z_STRETCH, zStretch);
			
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag tag)
		{
			xStretch = tag.getDouble(X_STRETCH);
			yStretch = tag.getDouble(Y_STRETCH);
			zStretch = tag.getDouble(Z_STRETCH);
		}
	}
}
