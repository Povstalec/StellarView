package net.povstalec.stellarview.api.sky_effects;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.StellarView;

public class ShootingStar extends SkyEffect
{
	public static final ResourceLocation SHOOTING_STAR_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/supernova.png");
	
	protected static final int TICKS = 1000;
	protected static final float MAX_SIZE = 1;
	protected static final int DURATION = 20;
	
	public ShootingStar()
	{
		super(100);
	}
	
	@Override
	public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		
		long tickSeed = level.getDayTime() / TICKS;
		int specificTime = (int) (level.getDayTime() % TICKS);

		Random randomizer = new Random(tickSeed);
		
		int shootingStarchance = randomizer.nextInt(1, 101);
		int randomStart = randomizer.nextInt(0, TICKS - DURATION);
		
		if(shootingStarchance <= getRarity() && specificTime >= randomStart && specificTime < randomStart + DURATION)
		{
			double position = level.getDayTime() % DURATION;
			
			long shootingStarRandomizer = level.getDayTime() / DURATION;
			
			Random random = new Random(shootingStarRandomizer);
			
			float xRotation = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
			float yRotation = random.nextInt(0, 360);
			float zRotation = random.nextInt(-70, 70);
			
			float rotation = (float) (Math.PI * position / 4);
			float size = (float) (Math.sin(Math.PI * position / DURATION));

			stack.pushPose();
			
			stack.mulPose(Axis.YP.rotationDegrees(yRotation));
	        stack.mulPose(Axis.ZP.rotationDegrees(zRotation));
	        stack.mulPose(Axis.XP.rotationDegrees(xRotation));
			
			this.renderEffect(bufferbuilder, stack.last().pose(), SHOOTING_STAR_TEXTURE, FULL_UV, size, rotation, 0, 0, getBrightness(level, camera, partialTicks));
			stack.popPose();
		}
	}
}
