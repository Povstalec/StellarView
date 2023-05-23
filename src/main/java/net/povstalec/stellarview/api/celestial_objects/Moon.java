package net.povstalec.stellarview.api.celestial_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class Moon extends CelestialObject
{
	public static final ResourceLocation VANILLA_MOON_TEXTURE = new ResourceLocation("textures/environment/moon_phases.png");
	
	public static final ResourceLocation DEFAULT_MOON_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases.png");
	public static final ResourceLocation DEFAULT_MOON_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_halo_phases.png");
	
	public Moon(ResourceLocation sunTexture, float size)
	{
		super(sunTexture, 100.0F, size);
		this.initialTheta((float) Math.toRadians(180));
		this.initialPhi((float) Math.toRadians(180));
	}
	
	@Override
	public void render(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
			float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
	{
		int phase = level.getMoonPhase();
		int x = phase % 4;
        int y = phase / 4 % 2;
        float xStart = (float)(x + 0) / 4.0F;
        float yStart = (float)(y + 0) / 2.0F;
        float xEnd = (float)(x + 1) / 4.0F;
        float yEnd = (float)(y + 1) / 2.0F;
        
        uv = StellarViewConfig.disable_moon_phases.get() ? new float[] {0.0F, 0.0F, 0.25F, 0.5F} : new float[] {xStart, yStart, xEnd, yEnd};
		
		super.render(level, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle + 360.0F * ((float) level.getDayTime() / 24000 / 8), playerYAngle, playerZAngle);
	}
	
	public static final class VanillaMoon extends Moon
	{
		public VanillaMoon()
		{
			super(VANILLA_MOON_TEXTURE, 20.0F);
			this.blends();
		}
		
		@Override
		public final void render(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
				float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
		{
			if(!StellarViewConfig.disable_moon.get())
				super.render(level, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
	
	public static final class DefaultMoon extends Moon
	{
		public DefaultMoon()
		{
			super(DEFAULT_MOON_TEXTURE, 20.0F);
			this.halo(DEFAULT_MOON_HALO_TEXTURE, 20.0F);
		}
		
		@Override
		public final void render(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
				float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
		{
			if(!StellarViewConfig.disable_moon.get())
				super.render(level, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
}
