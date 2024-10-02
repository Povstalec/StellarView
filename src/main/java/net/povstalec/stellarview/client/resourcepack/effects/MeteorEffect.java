package net.povstalec.stellarview.client.resourcepack.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.StellarCoordinates;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.common.util.UV;

public abstract class MeteorEffect
{
	public static final UV.Quad UV = new UV.Quad(false);
	public static final float DEFAULT_DISTANCE = 100.0F;
	public static final SphericalCoords SPHERICAL_START = new SphericalCoords(DEFAULT_DISTANCE, 0, 0);
	
	protected final ArrayList<MeteorType> meteorTypes;
	protected int totalWeight = 0;
	
	protected double rarity;
	
	public MeteorEffect(List<MeteorType> meteorTypes, double rarity)
	{
		this.meteorTypes = new ArrayList<MeteorType>(meteorTypes);
		this.rarity = rarity;
		
		for(MeteorType meteorType : meteorTypes)
		{
			this.totalWeight += meteorType.getWeight();
		}
	}
	
	public List<MeteorType> getMeteorTypes()
	{
		return meteorTypes;
	}
	
	public boolean canRender(ViewCenter viewCenter)
	{
		return getRarity(viewCenter) > 0 && meteorTypes.size() > 0;
	}
	
	public abstract double getRarity(ViewCenter viewCenter);
	
	public double getRarity()
	{
		return rarity;
	}
	
	protected boolean shouldAppear(ViewCenter viewCenter, long seed)
	{
		Random random = new Random(seed);
		
		return random.nextDouble() <= getRarity(viewCenter);
	}
	
	protected MeteorType getRandomMeteorType(long seed)
	{
		Random random = new Random(seed);
		
		int i = 0;
		
		for(int weight = random.nextInt(0, totalWeight); i < meteorTypes.size() - 1; i++)
		{
			weight -= meteorTypes.get(i).getWeight();
			
			if(weight <= 0)
				break;
		}
		
		return meteorTypes.get(i);
	}
	
	public Color.FloatRGBA rgba(ViewCenter viewCenter, ClientLevel level, Camera camera, long ticks, float partialTicks)
	{
		float brightness = level.getStarBrightness(partialTicks);
		brightness = viewCenter.starsAlwaysVisible() && brightness < 0.5F ? 
				0.5F : brightness;
		
		if(GeneralConfig.bright_stars.get())
			brightness = brightness * (1 + ((float) (15 - level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15)) / 15));
		
		brightness *= (1.0F - level.getRainLevel(partialTicks));
		
		return new Color.FloatRGBA(1, 1, 1, brightness);
	}
	
	public abstract void render(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder);
	
	public void render(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			float xRotation, float yRotation, float zRotation,
			MeteorType meteorType, float mulSize, float addRotation)
	{
		stack.pushPose();
		
		stack.mulPose(Vector3f.YP.rotationDegrees(yRotation));
        stack.mulPose(Vector3f.ZP.rotationDegrees(zRotation));
        stack.mulPose(Vector3f.XP.rotationDegrees(xRotation));
		
		meteorType.render(bufferbuilder, stack.last().pose(), SPHERICAL_START, rgba(viewCenter, level, camera, level.getDayTime(), partialTicks), level.getDayTime(), mulSize, addRotation);
		stack.popPose();
	}
	
	public static class MeteorType
	{
		private final ArrayList<TextureLayer> textureLayers;
		private final int weight;
		
		public static final Codec<MeteorType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(MeteorType::getTextureLayers),
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(MeteorType::getWeight)
				).apply(instance, MeteorType::new));
		
		public MeteorType(List<TextureLayer> textureLayers, int weight)
		{
			this.textureLayers = new ArrayList<TextureLayer>(textureLayers);
			this.weight = weight;
		}
		
		public ArrayList<TextureLayer> getTextureLayers()
		{
			return textureLayers;
		}
		
		public int getWeight()
		{
			return weight;
		}
		
		protected void renderTextureLayer(TextureLayer textureLayer, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, Color.FloatRGBA rgba, long ticks, float mulSize, float addRotation)
		{
			if(rgba.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
				return;
			
			float size = (float) textureLayer.mulSize(mulSize);
			
			if(size < textureLayer.minSize())
			{
				if(textureLayer.clampAtMinSize())
					size = (float) textureLayer.minSize();
				else
					return;
			}
			
			float rotation = (float) textureLayer.rotation();
			
			Vector3f corner00 = StellarCoordinates.placeOnSphere(-size, -size, sphericalCoords, rotation);
			Vector3f corner10 = StellarCoordinates.placeOnSphere(size, -size, sphericalCoords, rotation);
			Vector3f corner11 = StellarCoordinates.placeOnSphere(size, size, sphericalCoords, rotation);
			Vector3f corner01 = StellarCoordinates.placeOnSphere(-size, size, sphericalCoords, rotation);
		
		
			if(textureLayer.shoulBlend())
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			
			RenderSystem.setShaderColor(rgba.red() * textureLayer.rgba().red() / 255F, rgba.green() * textureLayer.rgba().green() / 255F, rgba.blue() * textureLayer.rgba().blue() / 255F, rgba.alpha() * textureLayer.rgba().alpha() / 255F);
			
			RenderSystem.setShaderTexture(0, textureLayer.texture());
	        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
	        
	        bufferbuilder.vertex(lastMatrix, corner00.x(), corner00.y(), corner00.z()).uv(textureLayer.uv().topRight().u(ticks), textureLayer.uv().topRight().v(ticks)).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner10.x(), corner10.y(), corner10.z()).uv(textureLayer.uv().bottomRight().u(ticks), textureLayer.uv().bottomRight().v(ticks)).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner11.x(), corner11.y(), corner11.z()).uv(textureLayer.uv().bottomLeft().u(ticks), textureLayer.uv().bottomLeft().v(ticks)).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner01.x(), corner01.y(), corner01.z()).uv(textureLayer.uv().topLeft().u(ticks), textureLayer.uv().topLeft().v(ticks)).endVertex();
	        
	        BufferUploader.drawWithShader(bufferbuilder.end());
	        
	        RenderSystem.defaultBlendFunc();
		}
		
		public final void render(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, Color.FloatRGBA rgba, long ticks, float mulSize, float addRotation)
		{
			for(TextureLayer textureLayer : textureLayers)
			{
				renderTextureLayer(textureLayer, bufferbuilder, lastMatrix, sphericalCoords, rgba, ticks, mulSize, addRotation);
			}
		}
	}
	
	
	
	public static class ShootingStar extends MeteorEffect
	{
		protected static final int TICKS = 1000;
		protected static final float MAX_SIZE = 1;
		protected static final int DURATION = 20;
		
		public static final Codec<ShootingStar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MeteorType.CODEC.listOf().fieldOf("meteor_types").forGetter(ShootingStar::getMeteorTypes),
				Codec.DOUBLE.fieldOf("probability").forGetter(ShootingStar::getRarity)
				).apply(instance, ShootingStar::new));
		
		public ShootingStar(List<MeteorType> meteorTypes, double rarity)
		{
			super(meteorTypes, rarity);
		}
		
		public ShootingStar()
		{
			this(new ArrayList<MeteorType>(), 0);
		}
		
		public double getRarity(ViewCenter viewCenter)
		{
			if(!viewCenter.overrideMeteorEffects())
				return rarity;
			
			return viewCenter.overrideShootingStarRarity();
		}
		
		@Override
		public final void render(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
		{
			if(!canRender(viewCenter))
				return;
			
			long tickSeed = level.getDayTime() / TICKS;
			int specificTime = (int) (level.getDayTime() % TICKS);

			Random randomizer = new Random(tickSeed);
			
			int randomStart = randomizer.nextInt(0, TICKS - DURATION);
			
			if(shouldAppear(viewCenter, tickSeed) && specificTime >= randomStart && specificTime < randomStart + DURATION)
			{
				double position = level.getDayTime() % DURATION;
				
				long shootingStarRandomizer = level.getDayTime() / DURATION;
				
				Random random = new Random(shootingStarRandomizer);
				
				float xRotation = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
				float yRotation = random.nextInt(0, 360);
				float zRotation = random.nextInt(-70, 70);

				MeteorType meteorType = getRandomMeteorType(tickSeed);
				
				float rotation = (float) (Math.PI * position / 4);
				float size = (float) (Math.sin(Math.PI * position / DURATION));

				this.render(viewCenter, level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation, meteorType, size, rotation);
			}
		}
	}
	
	
	
	public static class MeteorShower extends MeteorEffect
	{
		protected static final int TICKS = 1000;
		protected static final float MAX_SIZE = 1;
		protected static final int DURATION = 20;
		
		public static final Codec<MeteorShower> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MeteorType.CODEC.listOf().fieldOf("meteor_types").forGetter(MeteorShower::getMeteorTypes),
				Codec.DOUBLE.fieldOf("probability").forGetter(MeteorShower::getRarity)
				).apply(instance, MeteorShower::new));
		
		public MeteorShower(List<MeteorType> meteorTypes, double rarity)
		{
			super(meteorTypes, rarity);
		}
		
		public MeteorShower()
		{
			this(new ArrayList<MeteorType>(), 0);
		}
		
		public double getRarity(ViewCenter viewCenter)
		{
			if(!viewCenter.overrideMeteorEffects())
				return rarity;
			
			return viewCenter.overrideMeteorShowerRarity();
		}
		
		@Override
		public final void render(ViewCenter viewCenter, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
		{
			if(!canRender(viewCenter))
				return;
			
			long dailySeed = level.getDayTime() / viewCenter.getRotationPeriod();
			
			if(shouldAppear(viewCenter, dailySeed))
			{
				double position = level.getDayTime() % DURATION;
				
				long meteorRandomizer = level.getDayTime() / DURATION;
				
				Random random = new Random(meteorRandomizer);
				
				float xRotation = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
				float yRotation = random.nextInt(0, 360);
				float zRotation = random.nextInt(-70, 70);

				MeteorType meteorType = getRandomMeteorType(dailySeed);
				
				float rotation = (float) (Math.PI * position / 4);
				float size = (float) (Math.sin(Math.PI * position / DURATION));

				this.render(viewCenter, level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation, meteorType, size, rotation);
			}
		}
	}
}
