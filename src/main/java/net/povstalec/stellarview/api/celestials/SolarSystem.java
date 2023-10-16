package net.povstalec.stellarview.api.celestials;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.celestials.orbiting.OrbitingCelestialObject;
import net.povstalec.stellarview.api.celestials.orbiting.Sun;

public class SolarSystem extends GalacticObject
{
	private Sun sun;
	
	private float xAxisRotation;
	private float yAxisRotation;
	private float zAxisRotation;
	
	private List<OrbitingCelestialObject> celestialObjects = new ArrayList<OrbitingCelestialObject>();
	
	public SolarSystem(Sun sun, float x, float y, float z, float xAxisRotation, float yAxisRotation, float zAxisRotation)
	{
		super(Sun.DEFAULT_SUN_TEXTURE, 1);
		this.sun = sun;
		
		this.setGalacticPosition(x, y, z);

		this.xAxisRotation = xAxisRotation;
		this.yAxisRotation = yAxisRotation;
		this.zAxisRotation = zAxisRotation;
	}
	
	public final SolarSystem addCelestialObject(OrbitingCelestialObject object)
	{
		this.celestialObjects.add(object);
		return this;
	}
	
	public void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder,
			float xRotation, float yRotation, float zRotation)
	{
		this.celestialObjects.stream().forEach(orbitingObject ->
		{
			orbitingObject.render(level, camera, partialTicks, stack, bufferbuilder, xRotation, yRotation, zRotation);
		});
	}
	
	//============================================================================================
	//************************************Getters and setters*************************************
	//============================================================================================
	
	public float getX()
	{
		return this.galacticX;
	}
	
	public float getY()
	{
		return this.galacticY;
	}
	
	public float getZ()
	{
		return this.galacticZ;
	}
	
	public float getXAxisRotation()
	{
		return this.xAxisRotation;
	}
	
	public float getYAxisRotation()
	{
		return this.yAxisRotation;
	}
	
	public float getZAxisRotation()
	{
		return this.zAxisRotation;
	}
}
