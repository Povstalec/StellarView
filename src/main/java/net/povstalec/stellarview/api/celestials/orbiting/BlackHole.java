package net.povstalec.stellarview.api.celestials.orbiting;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class BlackHole extends OrbitingCelestialObject
{
	public static final ResourceLocation DEFAULT_BLACK_HOLE_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/black_hole/black_hole.png");
	public static final ResourceLocation DEFAULT_BLACK_HOLE_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/black_hole/black_hole_halo.png");
	
	public BlackHole(ResourceLocation sunTexture, float size)
	{
		super(sunTexture, size);
		this.visibleDuringDay();
	}
	
	public static class DefaultBlackHole extends BlackHole
	{
		public DefaultBlackHole(float size)
		{
			super(DEFAULT_BLACK_HOLE_TEXTURE, size);
			this.halo(DEFAULT_BLACK_HOLE_HALO_TEXTURE, size);
		}
		
		public DefaultBlackHole()
		{
			this(30.0F);
		}
	}
}
