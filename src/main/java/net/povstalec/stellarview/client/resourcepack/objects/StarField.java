package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.ArrayList;
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
import com.mojang.datafixers.util.Either;
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
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StarBuffer;
import net.povstalec.stellarview.common.util.StarData;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;

public class StarField extends SpaceObject
{
	@Nullable
	protected StarBuffer starBuffer;
	protected StarData starData;

	protected StarInfo starInfo;
	
	protected final long seed;
	protected final boolean clumpStarsInCenter;
	
	protected final int diameter;
	protected final int stars;
	
	private final double xStretch;
	private final double yStretch;
	private final double zStretch;
	
	protected final ArrayList<SpiralArm> spiralArms;
	
	protected final int totalStars;
	
	public static final Codec<StarField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(StarField::getParentKey),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(StarField::getAxisRotation),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(StarField::getTextureLayers),

			SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_STAR_FIELD_HANDLER).forGetter(StarField::getFadeOutHandler),

			StarInfo.CODEC.optionalFieldOf("star_info", StarInfo.DEFAULT_STAR_INFO).forGetter(StarField::getStarInfo),
			Codec.LONG.fieldOf("seed").forGetter(StarField::getSeed),
			Codec.INT.fieldOf("diameter_ly").forGetter(StarField::getDiameter),
			
			Codec.intRange(1, 30000).fieldOf("stars").forGetter(StarField::getStars),
			Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(StarField::clumpStarsInCenter),
			
			Codec.DOUBLE.optionalFieldOf("x_stretch", 1.0).forGetter(StarField::xStretch),
			Codec.DOUBLE.optionalFieldOf("y_stretch", 1.0).forGetter(StarField::yStretch),
			Codec.DOUBLE.optionalFieldOf("z_stretch", 1.0).forGetter(StarField::zStretch),
			
			SpiralArm.CODEC.listOf().optionalFieldOf("spiral_arms").forGetter(starField -> Optional.ofNullable(starField.spiralArms))
			).apply(instance, StarField::new));
	
	public StarField(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, List<TextureLayer> textureLayers,
			FadeOutHandler fadeOutHandler, StarInfo starInfo, long seed, int diameter, int numberOfStars, boolean clumpStarsInCenter,
			double xStretch, double yStretch, double zStretch, Optional<List<SpiralArm>> spiralArms)
	{
		super(parent, coords, axisRotation, textureLayers, fadeOutHandler);
		
		this.starInfo = starInfo;
		this.seed = seed;
		this.diameter = diameter;
		
		this.stars = numberOfStars;
		this.clumpStarsInCenter = clumpStarsInCenter;
		
		this.xStretch = xStretch;
		this.yStretch = yStretch;
		this.zStretch = zStretch;
		
		if(spiralArms.isPresent())
			this.spiralArms = new ArrayList<SpiralArm>(spiralArms.get());
		else
			this.spiralArms = new ArrayList<SpiralArm>();

		// Calculate the total amount of stars
		int totalStars = stars;
		for(SpiralArm arm : this.spiralArms)
		{
			totalStars += arm.armStars();
		}
		
		this.totalStars = totalStars;
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
	
	public boolean requiresSetup()
	{
		return starBuffer == null;
	}
	
	public void reset()
	{
		starBuffer = null;
	}
	
	protected void generateStars(BufferBuilder bufferBuilder, SpaceCoords relativeCoords, RandomSource randomsource)
	{
		for(int i = 0; i < stars; i++)
		{
			// This generates random coordinates for the Star close to the camera
			double distance = clumpStarsInCenter ? randomsource.nextDouble() : Math.cbrt(randomsource.nextDouble());
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
		bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR_POS_COLOR_LY);
		
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
	
	protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
	{
		RandomSource randomsource = RandomSource.create(seed);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR_POS_COLOR_LY);
		
		for(int i = 0; i < totalStars; i++)
		{
			starData.createStar(bufferBuilder, randomsource, relativeCoords, i);
		}
		return bufferBuilder.end();
	}
	
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
		//System.out.println(this + " " + viewCenter.getCoords());
		SpaceCoords difference = viewCenter.getCoords().sub(getCoords());
		
		if(requiresSetup())
			setupBuffer(difference);
		//else
		//	setStarBuffer(difference); // This could be viable with fewer stars
		
		float starBrightness = StarLike.getStarBrightness(viewCenter, level, camera, partialTicks);
		
		if(!GeneralConfig.disable_stars.get() && starBrightness > 0.0F)
		{
			stack.pushPose();
			
			//stack.translate(0, 0, 0);
			RenderSystem.setShaderColor(1, 1, 1, starBrightness);
			//RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/sun.png"));
			FogRenderer.setupNoFog();
			
			Quaternionf q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			stack.mulPose(q);
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, difference, StellarViewShaders.starShader());
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
	
	public static class SpiralArm
	{
		protected final int armStars;
		protected final double armRotation;
		protected final double armLength;
		protected final double armThickness;
		protected final boolean clumpStarsInCenter;
		
		public static final Codec<SpiralArm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("stars").forGetter(SpiralArm::armStars),
				Codec.DOUBLE.fieldOf("arm_rotation").forGetter(SpiralArm::armRotation),
				Codec.DOUBLE.fieldOf("arm_length").forGetter(SpiralArm::armLength),
				Codec.DOUBLE.fieldOf("arm_thickness").forGetter(SpiralArm::armThickness),
				Codec.BOOL.optionalFieldOf("clump_stars_in_center", true).forGetter(SpiralArm::clumpStarsInCenter)
				).apply(instance, SpiralArm::new));
		
		public SpiralArm(int armStars, double armRotationDegrees, double armLength, double armThickness, boolean clumpStarsInCenter)
		{
			this.armStars = armStars;
			this.armRotation = Math.toRadians(armRotationDegrees);
			this.armLength = armLength;
			this.armThickness = armThickness;
			
			this.clumpStarsInCenter = clumpStarsInCenter;
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
		
		protected void generateStars(BufferBuilder bufferBuilder, SpaceCoords relativeCoords, AxisRotation axisRotation, StarData starData, StarInfo starInfo, RandomSource randomsource, int numberOfStars, double sizeMultiplier)
		{
			for(int i = 0; i < armStars; i++)
			{
				// Milky Way is 90 000 ly across
				
				double progress = (double) i / armStars;
				
				double phi = armLength * Math.PI * progress - armRotation;
				double r = StellarCoordinates.spiralR(5, phi, armRotation);

				// This generates random coordinates for the Star close to the camera
				double distance = clumpStarsInCenter ? randomsource.nextDouble() : Math.cbrt(randomsource.nextDouble());
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
