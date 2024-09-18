package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.client.resourcepack.StarInfo;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StarData;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Galaxy
{
	public static class EllipticalGalaxy extends StarField
	{
		private final double xStretch;
		private final double yStretch;
		private final double zStretch;
		
		public static final Codec<EllipticalGalaxy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(EllipticalGalaxy::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(EllipticalGalaxy::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(EllipticalGalaxy::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(EllipticalGalaxy::getTextureLayers),

				SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(EllipticalGalaxy::getFadeOutHandler),
				
				StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(EllipticalGalaxy::getStarInfo),
				Codec.LONG.fieldOf("seed").forGetter(EllipticalGalaxy::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(EllipticalGalaxy::getDiameter),
				
				Codec.INT.fieldOf("stars").forGetter(EllipticalGalaxy::getStars),
				
				Codec.DOUBLE.fieldOf("x_stretch").forGetter(EllipticalGalaxy::xStretch),
				Codec.DOUBLE.fieldOf("y_stretch").forGetter(EllipticalGalaxy::yStretch),
				Codec.DOUBLE.fieldOf("z_stretch").forGetter(EllipticalGalaxy::zStretch)
				).apply(instance, EllipticalGalaxy::new));
		
		public EllipticalGalaxy(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler, StarInfo starInfo,
				long seed, int diameter, int stars, double xStretch, double yStretch, double zStretch)
		{
			super(parent, coords, axisRotation, textureLayers, fadeOutHandler, starInfo, seed, diameter, stars);
			
			this.xStretch = xStretch;
			this.yStretch = yStretch;
			this.zStretch = zStretch;
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
		
		protected void generateStars(BufferBuilder bufferBuilder, SpaceCoords relativeCoords, RandomSource randomsource)
		{
			boolean clumpInCenter = true; // TODO Let resourcepacks change this
			
			for(int i = 0; i < stars; i++)
			{
				// This generates random coordinates for the Star close to the camera
				double distance = clumpInCenter ? randomsource.nextDouble() : Math.cbrt(randomsource.nextDouble());
				double theta = randomsource.nextDouble() * 2F * Math.PI;
				double phi = Math.acos(2F * randomsource.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens
				
				Vector3d cartesian = new SphericalCoords(distance * diameter, theta, phi).toCartesianD();
				
				cartesian.x *= xStretch;
				cartesian.y *= yStretch;
				cartesian.z *= zStretch;
				
				axisRotation.quaterniond().transform(cartesian);

				starData.newStar(starInfo, bufferBuilder, randomsource, relativeCoords, cartesian.x, cartesian.y, cartesian.z, i);
			}
		}

		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			starData = new StarData(stars);
			
			generateStars(bufferBuilder, relativeCoords, randomsource);
			
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
	
	public static class SpiralGalaxy extends EllipticalGalaxy
	{
		protected final ArrayList<SpiralArm> spiralArms;
		
		protected final int totalStars;
		
		public static final Codec<SpiralGalaxy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(SpiralGalaxy::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(SpiralGalaxy::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(SpiralGalaxy::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(SpiralGalaxy::getTextureLayers),

				SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(SpiralGalaxy::getFadeOutHandler),

				StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(SpiralGalaxy::getStarInfo),
				Codec.LONG.fieldOf("seed").forGetter(SpiralGalaxy::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(SpiralGalaxy::getDiameter),
				
				Codec.intRange(1, 30000).fieldOf("stars").forGetter(SpiralGalaxy::getStars),
				
				Codec.DOUBLE.fieldOf("x_stretch").forGetter(SpiralGalaxy::xStretch),
				Codec.DOUBLE.fieldOf("y_stretch").forGetter(SpiralGalaxy::yStretch),
				Codec.DOUBLE.fieldOf("z_stretch").forGetter(SpiralGalaxy::zStretch),
				
				SpiralArm.CODEC.listOf().fieldOf("spiral_arms").forGetter(SpiralGalaxy::getSpiralArms)
				).apply(instance, SpiralGalaxy::new));
		
		public SpiralGalaxy(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, FadeOutHandler fadeOutHandler, StarInfo starInfo, 
				long seed, int diameter, int stars, double xStretch, double yStretch, double zStretch,
				List<SpiralArm> spiralArms)
		{
			super(parent, coords, axisRotation, textureLayers, fadeOutHandler, starInfo, seed, diameter, stars, xStretch, yStretch, zStretch);
			
			this.spiralArms = new ArrayList<SpiralArm>(spiralArms);

			// Calculate the total amount of stars
			int totalStars = stars;
			for(SpiralArm arm : spiralArms)
			{
				totalStars += arm.armStars();
			}
			
			this.totalStars = totalStars;
		}
		
		public List<SpiralArm> getSpiralArms()
		{
			return spiralArms;
		}
		
		@Override
		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			double sizeMultiplier = diameter / 30D;
			
			starData = new StarData(totalStars);
			
			generateStars(bufferBuilder, relativeCoords, randomsource);
			
			int numberOfStars = stars;
			for(SpiralArm arm : spiralArms) //Draw each arm
			{
				arm.generateStars(bufferBuilder, relativeCoords, axisRotation, starData, starInfo, randomsource, numberOfStars, sizeMultiplier);
				numberOfStars += arm.armStars();
			}
			
			return bufferBuilder.end();
		}
		
		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			for(int i = 0; i < totalStars; i++)
			{
				starData.createStar(bufferBuilder, randomsource, relativeCoords, i);
			}
			return bufferBuilder.end();
		}
		
		public static class SpiralArm
		{
			protected final int armStars;
			protected final double armRotation;
			protected final double armLength;
			protected final double armThickness;
			protected final boolean clumpInCenter;
			
			public static final Codec<SpiralArm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.INT.fieldOf("stars").forGetter(SpiralArm::armStars),
					Codec.DOUBLE.fieldOf("arm_rotation").forGetter(SpiralArm::armRotation),
					Codec.DOUBLE.fieldOf("arm_length").forGetter(SpiralArm::armLength),
					Codec.DOUBLE.fieldOf("arm_thickness").forGetter(SpiralArm::armThickness),
					Codec.BOOL.optionalFieldOf("clump_in_center", true).forGetter(SpiralArm::clumpInCenter)
					).apply(instance, SpiralArm::new));
			
			public SpiralArm(int armStars, double armRotationDegrees, double armLength, double armThickness, boolean clumpInCenter)
			{
				this.armStars = armStars;
				this.armRotation = Math.toRadians(armRotationDegrees);
				this.armLength = armLength;
				this.armThickness = armThickness;
				
				this.clumpInCenter = clumpInCenter;
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
			
			public boolean clumpInCenter()
			{
				return clumpInCenter;
			}
			
			protected void generateStars(BufferBuilder bufferBuilder, SpaceCoords relativeCoords, AxisRotation axisRotation, StarData starData, StarInfo starInfo, RandomSource randomsource, int numberOfStars, double sizeMultiplier)
			{
				for(int i = 0; i < armStars; i++)
				{
					// Milky Way is 90 000 ly across
					
					double progress = (double) i / armStars;
					
					double phi = armLength * Math.PI * progress - armRotation;
					double r = StellarCoordinates.spiralR(5, phi, armRotation);

					// This generates random coordinates for the Star close to the camera
					double distance = clumpInCenter ? randomsource.nextDouble() : Math.cbrt(randomsource.nextDouble());
					double theta = randomsource.nextDouble() * 2F * Math.PI;
					double sphericalphi = Math.acos(2F * randomsource.nextDouble() - 1F); // This prevents the formation of that weird streak that normally happens

					Vector3d cartesian = new SphericalCoords(distance * armThickness, theta, sphericalphi).toCartesianD();
					
					double x =  r * Math.cos(phi) + cartesian.x * armThickness / (progress * 1.5);
					double z =  r * Math.sin(phi) + cartesian.z * armThickness / (progress * 1.5);
					double y =  cartesian.y * armThickness / (progress * 1.5);
					
					cartesian.x = x * sizeMultiplier;
					cartesian.y = y * sizeMultiplier;
					cartesian.z = z * sizeMultiplier;
					
					axisRotation.quaterniond().transform(cartesian);
					
					starData.newStar(starInfo, bufferBuilder, randomsource, relativeCoords, cartesian.x, cartesian.y, cartesian.z, numberOfStars + i);
				}
			}
		}
	}
}
