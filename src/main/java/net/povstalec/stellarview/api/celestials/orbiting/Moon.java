package net.povstalec.stellarview.api.celestials.orbiting;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class Moon extends Planet
{
	public static final ResourceLocation VANILLA_MOON_TEXTURE = new ResourceLocation("textures/environment/moon_phases.png");
	
	public static final ResourceLocation DEFAULT_MOON_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases.png");
	public static final ResourceLocation DEFAULT_MOON_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_halo_phases.png");

	public static final ResourceLocation IO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/io_phases.png");
	public static final ResourceLocation IO_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/io_halo_phases.png");
	
	public static final ResourceLocation EUROPA_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/europa_phases.png");
	public static final ResourceLocation EUROPA_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/europa_halo_phases.png");
	
	public static final ResourceLocation CALLISTO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/callisto_phases.png");
	public static final ResourceLocation CALLISTO_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/callisto_halo_phases.png");
	
	public static final ResourceLocation GANYMEDE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/ganymede_phases.png");
	public static final ResourceLocation GANYMEDE_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/ganymede_halo_phases.png");

	public static final ResourceLocation ENCELADUS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/enceladus_phases.png");
	public static final ResourceLocation ENCELADUS_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/enceladus_halo_phases.png");

	public static final ResourceLocation TETHYS_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/tethys_phases.png");
	public static final ResourceLocation TETHYS_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/tethys_halo_phases.png");

	public static final ResourceLocation DIONE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/dione_phases.png");
	public static final ResourceLocation DIONE_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/dione_halo_phases.png");

	public static final ResourceLocation RHEA_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/rhea_phases.png");
	public static final ResourceLocation RHEA_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/rhea_halo_phases");
	
	public static final ResourceLocation TITAN_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/titan_phases.png");
	public static final ResourceLocation TITAN_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/moon_phases/titan_halo_phases.png");
	
	public Moon(ResourceLocation sunTexture, float size)
	{
		super(sunTexture, size);
		this.visibleDuringDay();
		this.blendsDuringDay();
	}

	protected boolean hasPhases() { return true; }
	
	protected float[] getUV(ClientLevel level, Camera camera, float partialTicks)
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
	
	public static class VanillaMoon extends Moon
	{
		public VanillaMoon()
		{
			super(VANILLA_MOON_TEXTURE, 20.0F);
			this.blends();
		}
	}
	
	public static class DefaultMoon extends Moon
	{
		public DefaultMoon(float size)
		{
			super(DEFAULT_MOON_TEXTURE, size);
			this.halo(DEFAULT_MOON_HALO_TEXTURE, size);
		}
		
		public DefaultMoon()
		{
			this(20.0F);
		}
	}
}
