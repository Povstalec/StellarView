package net.povstalec.stellarview.api.celestials;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.celestials.orbiting.OrbitingCelestialObject;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;

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
		//System.out.println("R");
		long gameTime = level.getDayTime();
		long lifeTime = gameTime - start;
		float superNovaSize = (float) (maxSize * Math.sin(Math.PI * lifeTime / duration));
		
		float visualSize = gameTime > start && ((superNovaSize >= size) || lifeTime > duration / 2) ? superNovaSize : size;
		
		return distanceSize(visualSize) * 50;
	}
	
	@Override
	protected float getRotation(ClientLevel level, float partialTicks)
	{
		long gameTime = level.getDayTime();
		long lifeTime = gameTime - start;
		
		return gameTime > start ? (float) (Math.PI * lifeTime / duration) + rotation : rotation;
	}
	
	//TODO Is this even useful at this point?
	/*public Vector3f getRelativeCartesianCoordinates(ClientLevel level, float partialTicks)
	{
		return this.coordinates;
	}*/
	
	/*@Override
	public void render(OrbitingCelestialObject viewCenter, Vector3f vievCenterCoords, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			Vector3f skyAxisRotation, Vector3f parentCoords)
	{
		Vector3f relativeCoords = getRelativeCartesianCoordinates(level, partialTicks);
		//Vector3f absoluteCoords = StellarCoordinates.absoluteVector(parentCoords, relativeCoords);

		super.render(viewCenter, vievCenterCoords, level, camera, partialTicks, stack, bufferbuilder, skyAxisRotation, relativeCoords);
	}*/
}
