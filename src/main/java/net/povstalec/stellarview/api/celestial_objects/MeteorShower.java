package net.povstalec.stellarview.api.celestial_objects;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.povstalec.stellarview.StellarView;

public class MeteorShower extends RarityObject
{
	public static final ResourceLocation SHOOTING_STAR_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/supernova.png");
	
	protected static final int DAY_LENGTH = 24000;
	protected static final int DURATION = 20;
	
	public MeteorShower(ResourceLocation texture)
	{
		super(texture, 100.0F, 0, 100);
		this.blends();
	}
	
	public MeteorShower()
	{
		this(SHOOTING_STAR_TEXTURE);
	}
	
	@Override
	public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
			float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
	{
		
		long dailySeed = level.getDayTime() / DAY_LENGTH;

		Random randomizer = new Random(dailySeed);
		
		int meteorShowerChance = randomizer.nextInt(1, 101);
		
		if(meteorShowerChance <= value.get())
		{
			double position = level.getDayTime() % DURATION;
			
			long meteorRandomizer = level.getDayTime() / DURATION;
			
			Random random = new Random(meteorRandomizer);
			
			playerXAngle = (float) (random.nextInt(0, 45) + Math.PI * Mth.lerp(partialTicks, position - 1, position));
			playerYAngle = random.nextInt(0, 360);
			playerZAngle = random.nextInt(-70, 70);
			
			this.rotation = (float) (Math.PI * position / 4);
			this.size = (float) (Math.sin(Math.PI * position / DURATION));
			
			super.render(level, camera, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
}
