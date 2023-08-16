package net.povstalec.stellarview.api.celestial_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class Supernova extends CelestialObject
{
	public static final ResourceLocation SUPERNOVA_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/supernova.png");
	
	protected final static float INITIAL_SIZE = 1;
	protected float maxSize;
	protected long start;
	protected long duration;
	
	public Supernova(ResourceLocation texture, float maxSize, long start, long duration)
	{
		super(texture, 100.0F, INITIAL_SIZE);
		this.blends();
		this.visibleDuringDay();
		this.maxSize = maxSize;
		this.start = start;
		this.duration = duration;
	}
	
	public Supernova(float maxSize, long start, long duration)
	{
		this(SUPERNOVA_TEXTURE, maxSize, start, duration);
	}
	
	@Override
	public final void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder, float[] uv,
			float playerDistance, float playerXAngle, float playerYAngle, float playerZAngle)
	{
		long gameTime = level.getDayTime();
		long lifeTime = gameTime - start;
		
		if(gameTime <= (start + duration))
		{
			float superNovaSize = (float) (maxSize * Math.sin(Math.PI * lifeTime / duration));
			
			this.size = gameTime > start && ((superNovaSize >= INITIAL_SIZE) || lifeTime > duration / 2) ? superNovaSize : INITIAL_SIZE;
			this.rotation = gameTime > start ? (float) (Math.PI * lifeTime / duration) : 0;
			super.render(level, camera, partialTicks, stack, bufferbuilder, uv, playerDistance, playerXAngle, playerYAngle, playerZAngle);
		}
	}
}
