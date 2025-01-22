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
	
	public static final ResourceLocation DEFAULT_STAR_TEXTURE = new ResourceLocation(StellarView.MODID,"textures/environment/star.png");
	public static final ResourceLocation DEFAULT_DUST_CLOUD_TEXTURE = new ResourceLocation(StellarView.MODID,"textures/environment/dust_cloud.png");
	
	public static final String SEED = "seed";
	public static final String DIAMETER_LY = "diameter_ly";
	public static final String STARS = "stars";
	public static final String TOTAL_STARS = "total_stars";
	public static final String STAR_INFO = "star_info";
	public static final String SPIRAL_ARMS = "spiral_arms";
	public static final String STAR_TEXTURE = "star_texture";
	public static final String CLUMP_STARS_IN_CENTER = "clump_stars_in_center";
	public static final String X_STRETCH = "x_stretch";
	public static final String Y_STRETCH = "y_stretch";
	public static final String Z_STRETCH = "z_stretch";
	public static final String DUST_CLOUDS = "dust_clouds";
	public static final String TOTAL_DUST_CLOUDS = "total_dust_clouds";
	public static final String DUST_CLOUD_INFO = "dust_cloud_info";
	public static final String DUST_CLOUD_TEXTURE = "dust_cloud_texture";
	
	protected int dustClouds;
	protected ResourceLocation dustCloudInfo;
	protected ResourceLocation dustCloudTexture;
	
	protected int stars;
	protected ResourceLocation starInfo;
	protected ResourceLocation starTexture;
	
	protected long seed;
	protected boolean clumpStarsInCenter;
	
	protected int diameter;
	
	protected double xStretch;
	protected double yStretch;
	protected double zStretch;
	
	protected ArrayList<SpiralArm> spiralArms;
	
	public static final Codec<StarField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(StarField::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(StarField::getAxisRotation),
			
			Codec.intRange(0, 4000).optionalFieldOf("dust_clouds", 0).forGetter(StarField::getDustClouds),
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_info").forGetter(starField -> Optional.ofNullable(starField.dustCloudInfo)),
			ResourceLocation.CODEC.optionalFieldOf("dust_cloud_texture", DEFAULT_DUST_CLOUD_TEXTURE).forGetter(StarField::getDustCloudTexture),
			
			ResourceLocation.CODEC.optionalFieldOf("star_info").forGetter(starField -> Optional.ofNullable(starField.starInfo)),
			ResourceLocation.CODEC.optionalFieldOf("star_texture", DEFAULT_STAR_TEXTURE).forGetter(starField -> starField.starTexture),
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
	
	public StarField(Optional<ResourceLocation> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, int dustClouds, Optional<ResourceLocation> dustCloudInfo, ResourceLocation dustCloudTexture, Optional<ResourceLocation> starInfo, ResourceLocation starTexture, long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
			double xStretch, double yStretch, double zStretch, List<SpiralArm> spiralArms)
	{
		super(parent, coords, axisRotation);
		
		this.dustClouds = dustClouds;
		this.dustCloudInfo = dustCloudInfo.isPresent() ? dustCloudInfo.get() : null;
		this.dustCloudTexture = dustCloudTexture;
		
		this.starInfo = starInfo.isPresent() ? starInfo.get() : null;
		this.seed = seed;
		this.diameter = diameter;
		
		this.stars = numberOfStars;
		this.starTexture = starTexture;
		this.clumpStarsInCenter = clumpStarsInCenter;
		
		this.xStretch = xStretch;
		this.yStretch = yStretch;
		this.zStretch = zStretch;
		
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
		
		tag.putLong(SEED, seed);
		
		tag.putInt(DIAMETER_LY, diameter);
		tag.putInt(STARS, stars);
		tag.putString(STAR_TEXTURE, starTexture.toString());
		
		tag.putBoolean(CLUMP_STARS_IN_CENTER, clumpStarsInCenter);
		
		tag.putDouble(X_STRETCH, xStretch);
		tag.putDouble(Y_STRETCH, yStretch);
		tag.putDouble(Z_STRETCH, zStretch);
		
		CompoundTag armsTag = new CompoundTag();
		for(int i = 0; i < spiralArms.size(); i++)
		{
			armsTag.put("spiral_arm_" + i, spiralArms.get(i).serializeNBT());
		}
		
		tag.put(SPIRAL_ARMS, armsTag);
		
		if(starInfo != null)
			tag.putString(STAR_INFO, starInfo.toString());
		if(dustCloudInfo != null)
			tag.putString(DUST_CLOUD_INFO, dustCloudInfo.toString());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag)
	{
		super.deserializeNBT(tag);
		
		dustClouds = tag.getInt(DUST_CLOUDS);
		dustCloudTexture = new ResourceLocation(tag.getString(DUST_CLOUD_TEXTURE));
		
		seed = tag.getLong(SEED);
		
		diameter = tag.getInt(DIAMETER_LY);
		stars = tag.getInt(STARS);
		starTexture = new ResourceLocation(tag.getString(STAR_TEXTURE));
		
		clumpStarsInCenter = tag.getBoolean(CLUMP_STARS_IN_CENTER);
		
		xStretch = tag.getDouble(X_STRETCH);
		yStretch = tag.getDouble(Y_STRETCH);
		zStretch = tag.getDouble(Z_STRETCH);
		
		this.spiralArms = new ArrayList<SpiralArm>();
		CompoundTag armsTag = tag.getCompound(SPIRAL_ARMS);
		for(String key : armsTag.getAllKeys())
		{
			SpiralArm arm = new SpiralArm();
			arm.deserializeNBT(armsTag.getCompound(key));
			spiralArms.add(arm);
		}
		
		starInfo = tag.contains(STAR_INFO) ? new ResourceLocation(tag.getString(STAR_INFO)) : null;
		dustCloudInfo = tag.contains(DUST_CLOUD_INFO) ? new ResourceLocation(tag.getString(DUST_CLOUD_INFO)) : null;
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
		}
	}
}
