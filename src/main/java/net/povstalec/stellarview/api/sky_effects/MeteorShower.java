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

public class MeteorShower extends SkyEffect
{
	public static final ResourceLocation SHOOTING_STAR_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/supernova.png");
	
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
			
	        this.renderEffect(bufferbuilder, stack.last().pose(), SHOOTING_STAR_TEXTURE, FULL_UV, size, rotation, 0, 0, getBrightness(level, camera, partialTicks));
			stack.popPose();
		}
	}
}
