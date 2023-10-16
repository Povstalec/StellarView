package net.povstalec.stellarview.api.celestials.orbiting;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class Sun extends OrbitingCelestialObject
{
	public static final ResourceLocation VANILLA_SUN_TEXTURE = new ResourceLocation("textures/environment/sun.png");
	
	public static final ResourceLocation DEFAULT_SUN_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sun.png");
	public static final ResourceLocation DEFAULT_SUN_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sun_halo.png");
	
	public Sun(ResourceLocation sunTexture, float size)
	{
		super(sunTexture, size);
		this.visibleDuringDay();
	}
	
	public static class VanillaSun extends Sun
	{
		public VanillaSun()
		{
			super(VANILLA_SUN_TEXTURE, 30.0F);
			this.blends();
		}
	}
	
	public static class DefaultSun extends Sun
	{
		public DefaultSun()
		{
			super(DEFAULT_SUN_TEXTURE, 30.0F);
			this.halo(DEFAULT_SUN_HALO_TEXTURE, 30.0F);
		}
	}
}
