package net.povstalec.stellarview.api.celestials.orbiting;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class Moon extends Planet
{
	public static final ResourceLocation VANILLA_MOON_TEXTURE = new ResourceLocation("textures/environment/moon_phases.png");
	public static final ResourceLocation VANILLA_MOON_HALO_TEXTURE = new ResourceLocation("textures/environment/moon_halo_phases.png");

	public static final int LUNA_ORBIT_DAYS = 8;
	
	protected int ticksPerPhase;
	protected int phaseTickOffset;
	
	public Moon(ResourceLocation sunTexture, float size, int ticksPerPhase, int phaseTickOffset)
	{
		super(sunTexture, size);
		
		this.visibleDuringDay();
		this.blendsDuringDay();
		
		this.ticksPerPhase = ticksPerPhase;
		this.phaseTickOffset = phaseTickOffset;
	}
	
	public Moon(ResourceLocation sunTexture, float size)
	{
		this(sunTexture, size, 24000, 0);
	}

	protected boolean hasPhases()
	{
		return true;
	}

	@Override
	protected float[] getUV(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera,
			float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, Vector3f skyAxisRotation, Vector3f coords)
	{
		long ticks = level.getDayTime() + phaseTickOffset;
		
		int phase = (int) (ticks % ticksPerPhase * 8) / ticksPerPhase;
		int x = phase % 4;
        int y = phase / 4 % 2;
        float xStart = (float)(x + 0) / 4.0F;
        float yStart = (float)(y + 0) / 2.0F;
        float xEnd = (float)(x + 1) / 4.0F;
        float yEnd = (float)(y + 1) / 2.0F;
        
        return hasPhases() ? new float[] {xStart, yStart, xEnd, yEnd} : new float[] {0.0F, 0.0F, 0.25F, 0.5F};
	}
	
	public static class VanillaMoon extends Moon
	{
		public VanillaMoon()
		{
			super(VANILLA_MOON_TEXTURE, 20.0F);
			this.halo(VANILLA_MOON_HALO_TEXTURE, 20.0F);
			this.blends();
			this.flipUV();
		}

		@Override
		protected float[] getUV(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera,
				float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, Vector3f skyAxisRotation, Vector3f coords)
		{
			int phase = level.getMoonPhase();
			int x = phase % 4;
	        int y = phase / 4 % 2;
	        float xStart = (float)(x + 0) / 4.0F;
	        float yStart = (float)(y + 0) / 2.0F;
	        float xEnd = (float)(x + 1) / 4.0F;
	        float yEnd = (float)(y + 1) / 2.0F;
	        
	        return hasPhases() ? new float[] {xStart, yStart, xEnd, yEnd} : new float[] {0.0F, 0.0F, 0.25F, 0.5F};
		}
	}
	
	public static class DefaultMoon extends Moon
	{
		public DefaultMoon(float size)
		{
			super(DEFAULT_MOON_TEXTURE, size);
			this.halo(DEFAULT_MOON_HALO_TEXTURE, size);
			this.flipUV();
		}
		
		@Override
		protected float[] getUV(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera,
				float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, Vector3f skyAxisRotation, Vector3f coords)
		{
			int phase = level.getMoonPhase();
			int x = phase % 4;
	        int y = phase / 4 % 2;
	        float xStart = (float)(x + 0) / 4.0F;
	        float yStart = (float)(y + 0) / 2.0F;
	        float xEnd = (float)(x + 1) / 4.0F;
	        float yEnd = (float)(y + 1) / 2.0F;
	        
	        return hasPhases() ? new float[] {xStart, yStart, xEnd, yEnd} : new float[] {0.0F, 0.0F, 0.25F, 0.5F};
		}
		
		public DefaultMoon()
		{
			this(20.0F);
		}
	}
}
