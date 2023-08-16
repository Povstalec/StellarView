package net.povstalec.stellarview.api.celestial_objects;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.StellarView;

public class ShootingStar extends RarityObject
{
	public static final ResourceLocation SHOOTING_STAR_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/supernova.png");
	
	protected static final int TICKS = 1000;
	protected static final float MAX_SIZE = 1;
	protected static final int DURATION = 20;
	
	public ShootingStar(ResourceLocation texture)
	{
		super(texture, 100.0F, 0, 100);
		this.blends();
	}
	
	public ShootingStar()
	{
		this(SHOOTING_STAR_TEXTURE);
	}
	
	@Override
	public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
			float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
	{
		
		long tickSeed = level.getDayTime() / TICKS;
		int specificTime = (int) (level.getDayTime() % TICKS);

		Random randomizer = new Random(tickSeed);
		
		int shootingStarchance = randomizer.nextInt(1, 101);
		int randomStart = randomizer.nextInt(0, TICKS - DURATION);
		
		if(shootingStarchance <= value.get() && specificTime >= randomStart && specificTime < randomStart + DURATION)
		{
			double position = level.getDayTime() % DURATION;
			
			long shootingStarRandomizer = level.getDayTime() / DURATION;
			
			Random random = new Random(shootingStarRandomizer);
			
			playerXAngle = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
			playerYAngle = random.nextInt(0, 360);
			playerZAngle = random.nextInt(-70, 70);
			
			this.rotation = (float) (Math.PI * position / 4);
			this.size = (float) (Math.sin(Math.PI * position / DURATION));
			
			super.render(level, camera, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
}
