package net.povstalec.stellarview.api.sky_effects;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestials.Star.SpectralType;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class MeteorShower extends SkyEffect
{

	public static final ResourceLocation METEOR_WHITE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_white.png");
	public static final ResourceLocation METEOR_RED_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_red.png");
	public static final ResourceLocation METEOR_ORANGE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_orange.png");
	public static final ResourceLocation METEOR_YELLOW_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_yellow.png");
	public static final ResourceLocation METEOR_CYAN_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_cyan.png");
	public static final ResourceLocation METEOR_BLUE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_blue.png");
	public static final ResourceLocation METEOR_VIOLET_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sky_effect/meteor/meteor_violet.png");
	
	public enum MeteorType
	{
		WHITE(METEOR_WHITE_TEXTURE),
		RED(METEOR_RED_TEXTURE),
		ORANGE(METEOR_ORANGE_TEXTURE),
		YELLOW(METEOR_YELLOW_TEXTURE),
		CYAN(METEOR_CYAN_TEXTURE),
		BLUE(METEOR_BLUE_TEXTURE),
		VIOLET(METEOR_VIOLET_TEXTURE);
		
		private ResourceLocation texture;
		
		MeteorType(ResourceLocation texture)
		{
			this.texture = texture;
		}
		
		public ResourceLocation getTexture()
		{
			return this.texture;
		}
		
		public static MeteorType randomMeteorType(long seed)
		{
			Random random = new Random(seed);
			
			if(StellarViewConfig.equal_spectral_types.get())
			{
				MeteorType[] meteorTypes = MeteorType.values();
				return meteorTypes[random.nextInt(0, meteorTypes.length)];
			}
			
			int value = random.nextInt(0, 100);
			
			// Slightly adjusted percentage values that can be found in SpectralType comments
			if(value < 40)
				return WHITE;
			else if(value < (40 + 10))
				return RED;
			else if(value < (40 + 20 + 15))
				return ORANGE;
			else if(value < (40 + 20 + 15 + 10))
				return YELLOW;
			else if(value < (40 + 20 + 15 + 10 + 5))
				return CYAN;
			else if(value < (40 + 20 + 15 + 10 + 5 + 5))
				return BLUE;
			
			return VIOLET;
		}
	}

	protected static final int DAY_LENGTH = 24000;
	protected static final int DURATION = 20;
	
	public MeteorShower()
	{
		super(100);
	}
	
	@Override
	public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		long dailySeed = level.getDayTime() / DAY_LENGTH;
		
		ResourceLocation showerColor = MeteorType.randomMeteorType(dailySeed).getTexture();
		Random randomizer = new Random(dailySeed);
		
		int meteorShowerChance = randomizer.nextInt(1, 101);
		
		if(meteorShowerChance <= getRarity())
		{
			double position = level.getDayTime() % DURATION;
			
			long meteorRandomizer = level.getDayTime() / DURATION;
			
			Random random = new Random(meteorRandomizer);
			
			float xRotation = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
			float yRotation = random.nextInt(0, 360);
			float zRotation = random.nextInt(-70, 70);
			
			float rotation = (float) (Math.PI * position / 4);
			float size = (float) (Math.sin(Math.PI * position / DURATION));

			stack.pushPose();
			
			stack.mulPose(Vector3f.YP.rotationDegrees(yRotation));
	        stack.mulPose(Vector3f.ZP.rotationDegrees(zRotation));
	        stack.mulPose(Vector3f.XP.rotationDegrees(xRotation));
			
	        this.renderEffect(bufferbuilder, stack.last().pose(), showerColor, FULL_UV, size, rotation, 0, 0, getBrightness(level, camera, partialTicks));
			stack.popPose();
		}
	}
}
