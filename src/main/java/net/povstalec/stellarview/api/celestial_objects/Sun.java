package net.povstalec.stellarview.api.celestial_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class Sun extends CelestialObject
{
	public static final ResourceLocation VANILLA_SUN_TEXTURE = new ResourceLocation("textures/environment/sun.png");
	
	public static final ResourceLocation DEFAULT_SUN_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sun.png");
	public static final ResourceLocation DEFAULT_SUN_HALO_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/sun_halo.png");
	
	public Sun(ResourceLocation sunTexture, float size)
	{
		super(sunTexture, 100.0F, size);
		this.visibleDuringDay();
	}
	
	public static class VanillaSun extends Sun
	{
		public VanillaSun()
		{
			super(VANILLA_SUN_TEXTURE, 30.0F);
			this.blends();
		}
		
		@Override
		public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
				float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
		{
			if(shouldRender())
				super.render(level, camera, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
	
	public static class DefaultSun extends Sun
	{
		public DefaultSun()
		{
			super(DEFAULT_SUN_TEXTURE, 30.0F);
			this.halo(DEFAULT_SUN_HALO_TEXTURE, 30.0F);
		}
		
		@Override
		public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
				float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
		{
			if(shouldRender())
				super.render(level, camera, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
}
