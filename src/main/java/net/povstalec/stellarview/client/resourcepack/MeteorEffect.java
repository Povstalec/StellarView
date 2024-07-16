package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.common.util.UV;

public abstract class MeteorEffect
{
	public static final UV.Quad UV = new UV.Quad(false);
	public static final float DEFAULT_DISTANCE = 100.0F;
	public static final SphericalCoords SPHERICAL_START = new SphericalCoords(DEFAULT_DISTANCE, 0, 0);
	
	private final ArrayList<MeteorType> meteorTypes;
	private int totalWeight = 0;
	
	private double rarity;
	
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
	
	public boolean canRender()
	{
		return meteorTypes.size() > 0;
	}
	
	public double getRarity()
	{
		return rarity;
	}
	
	protected boolean shouldAppear(long seed)
	{
		Random random = new Random(seed);
		
		return random.nextDouble() <= rarity;
	}
	
	protected MeteorType getMeteorType(long seed)
	{
		// TODO Randomize getting it
		
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
	
	public float getBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = level.getStarBrightness(partialTicks);
		brightness = StellarViewConfig.day_stars.get() && brightness < 0.5F ? 
				0.5F : brightness;
		
		if(StellarViewConfig.bright_stars.get())
			brightness = brightness * (1 + ((float) (15 - level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15)) / 15));
		
		return brightness * (1.0F - level.getRainLevel(partialTicks));
	}
	
	public abstract void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder);
	
	public void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			float xRotation, float yRotation, float zRotation,
			MeteorType meteorType, float mulSize, float addRotation)
	{
		stack.pushPose();
		
		stack.mulPose(Axis.YP.rotationDegrees(yRotation));
        stack.mulPose(Axis.ZP.rotationDegrees(zRotation));
        stack.mulPose(Axis.XP.rotationDegrees(xRotation));
		
		meteorType.render(bufferbuilder, stack.last().pose(), SPHERICAL_START, getBrightness(level, camera, partialTicks), mulSize, addRotation);
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
		
		public final void render(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, float brightness, float mulSize, float addRotation)
		{
			for(TextureLayer textureLayer : textureLayers)
			{
				textureLayer.render(bufferbuilder, lastMatrix, sphericalCoords, brightness, mulSize, addRotation);
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
		
		@Override
		public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
		{
			if(!canRender())
				return;
			
			long tickSeed = level.getDayTime() / TICKS;
			int specificTime = (int) (level.getDayTime() % TICKS);

			Random randomizer = new Random(tickSeed);
			
			int randomStart = randomizer.nextInt(0, TICKS - DURATION);
			
			if(shouldAppear(tickSeed) && specificTime >= randomStart && specificTime < randomStart + DURATION)
			{
				double position = level.getDayTime() % DURATION;
				
				long shootingStarRandomizer = level.getDayTime() / DURATION;
				
				Random random = new Random(shootingStarRandomizer);
				
				float xRotation = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
				float yRotation = random.nextInt(0, 360);
				float zRotation = random.nextInt(-70, 70);

				MeteorType meteorType = getMeteorType(tickSeed);
				
				float rotation = (float) (Math.PI * position / 4);
				float size = (float) (Math.sin(Math.PI * position / DURATION));

				this.render(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation, meteorType, size, rotation);
			}
		}
	}
	
	
	
	public static class MeteorShower extends MeteorEffect
	{
		protected static final int TICKS = 1000;
		protected static final float MAX_SIZE = 1;
		protected static final int DURATION = 20;
		protected static final int DAY_LENGTH = 24000;
		
		public static final Codec<MeteorShower> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MeteorType.CODEC.listOf().fieldOf("meteor_types").forGetter(MeteorShower::getMeteorTypes),
				Codec.DOUBLE.fieldOf("probability").forGetter(MeteorShower::getRarity)
				).apply(instance, MeteorShower::new));
		
		public MeteorShower(List<MeteorType> meteorTypes, double rarity)
		{
			super(meteorTypes, rarity);
		}
		
		@Override
		public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
		{
			if(!canRender())
				return;
			
			long dailySeed = level.getDayTime() / DAY_LENGTH;
			
			if(shouldAppear(dailySeed))
			{
				double position = level.getDayTime() % DURATION;
				
				long meteorRandomizer = level.getDayTime() / DURATION;
				
				Random random = new Random(meteorRandomizer);
				
				float xRotation = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
				float yRotation = random.nextInt(0, 360);
				float zRotation = random.nextInt(-70, 70);

				MeteorType meteorType = getMeteorType(dailySeed);
				
				float rotation = (float) (Math.PI * position / 4);
				float size = (float) (Math.sin(Math.PI * position / DURATION));

				this.render(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation, meteorType, size, rotation);
			}
		}
	}
}
