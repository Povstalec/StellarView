package net.povstalec.stellarview.api.celestials;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public class Supernova extends StellarObject
{
	public static final ResourceLocation SUPERNOVA_TEXTURE = new ResourceLocation(StellarView.MODID, "textures/environment/supernova.png");

	protected final static float INITIAL_SIZE = 1;
	
	protected ResourceLocation nebulaTexture; //TODO Leave a Nebula where there used to be a Supernova
	
	protected float maxSize;
	protected long start;
	protected long duration;
	
	public Supernova(ResourceLocation texture, float maxSize, long start, long duration)
	{
		super(texture, INITIAL_SIZE);
		this.maxSize = maxSize;
		this.start = start;
		this.duration = duration;
	}
	
	public Supernova(float maxSize, long start, long duration)
	{
		this(SUPERNOVA_TEXTURE, maxSize, start, duration);
	}

	@Override
	protected boolean shouldBlend(ClientLevel level, Camera camera)
	{
		return true;
	}

	@Override
	protected boolean isVisibleDuringDay(ClientLevel level, Camera camera)
	{
		return true;
	}

	@Override
	protected boolean shouldRender(ClientLevel level, Camera camera)
	{
		long gameTime = level.getDayTime();
		return gameTime <= (start + duration);
	}
	
	@Override
	protected float getSize(ClientLevel level, float partialTicks)
	{
		long gameTime = level.getDayTime();
		long lifeTime = gameTime - start;
		float superNovaSize = (float) (maxSize * Math.sin(Math.PI * lifeTime / duration));
		
		float visualSize = gameTime > start && ((superNovaSize >= size) || lifeTime > duration / 2) ? superNovaSize : size;
		
		return distanceSize(visualSize) * 10;
	}
	
	@Override
	protected float getRotation(ClientLevel level, float partialTicks)
	{
		long gameTime = level.getDayTime();
		long lifeTime = gameTime - start;
		
		return gameTime > start ? (float) (Math.PI * lifeTime / duration) + rotation : rotation;
	}
}
