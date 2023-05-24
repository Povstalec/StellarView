package net.povstalec.stellarview.api;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.api.celestial_objects.CelestialObject;
import net.povstalec.stellarview.client.render.level.StellarViewSky;

public class StellarViewSpecialEffects extends DimensionSpecialEffects
{
	protected StellarViewSky skyRenderer;
	
	public StellarViewSpecialEffects(float cloudLevel, boolean hasGround, SkyType skyType, 
			boolean forceBrightLightmap, boolean constantAmbientLight)
	{
		super(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
		
		this.skyRenderer = new StellarViewSky();
	}
	
	/**
	 * Creates a Spiral Galaxy with 4 arms
	 * @param seed Seed used for Star generation
	 * @param numberOfStars Number of Stars per arm
	 * @param xOffset Offset on the X-Axis
	 * @param yOffset Offset on the Y-Axis
	 * @param zOffset Offset on the Z-Axis
	 * @param alpha Rotation around the X-axis
	 * @param beta Rotation around the Z-axis
	 * @param gamma Rotation around the Y-axis
	 * @return self
	 */
	public final StellarViewSpecialEffects spiralGalaxy4Arms(long seed, int numberOfStars,
			double xOffset, double yOffset, double zOffset, double alpha, double beta, double gamma)
	{
		this.skyRenderer = this.skyRenderer.spiralGalaxy4Arms(seed, numberOfStars,
				xOffset, yOffset, zOffset, alpha, beta, gamma);
		return this;
	}
	
	/**
	 * Creates a Spiral Galaxy with 2 arms
	 * @param seed Seed used for Star generation
	 * @param numberOfStars Number of Stars per arm
	 * @param xOffset Offset on the X-Axis
	 * @param yOffset Offset on the Y-Axis
	 * @param zOffset Offset on the Z-Axis
	 * @param alpha Rotation around the X-axis
	 * @param beta Rotation around the Z-axis
	 * @param gamma Rotation around the Y-axis
	 * @return self
	 */
	public final StellarViewSpecialEffects spiralGalaxy2Arms(long seed, int numberOfStars,
			double xOffset, double yOffset, double zOffset, double alpha, double beta, double gamma)
	{
		this.skyRenderer = this.skyRenderer.spiralGalaxy2Arms(seed, numberOfStars,
				xOffset, yOffset, zOffset, alpha, beta, gamma);
		return this;
	}
	
	/**
	 * Adds a Celestial Object
	 * @param object Celestial Object that will be added
	 * @return self
	 */
	public final StellarViewSpecialEffects celestialObject(CelestialObject object)
	{
		this.skyRenderer = this.skyRenderer.celestialObject(object);
		return this;
	}
	
	/**
	 * Adds a Sun with properties comparable to that of Vanilla Minecraft
	 * @return self
	 */
	public final StellarViewSpecialEffects vanillaSun()
	{
		this.skyRenderer = this.skyRenderer.vanillaSun();
		return this;
	}

	/**
	 * Adds a Moon with properties comparable to that of Vanilla Minecraft
	 * @return self
	 */
	public final StellarViewSpecialEffects vanillaMoon()
	{
		this.skyRenderer = this.skyRenderer.vanillaMoon();
		return this;
	}
	
	public final StellarViewSpecialEffects skybox(ResourceLocation texture)
	{
		this.skyRenderer = this.skyRenderer.skybox(texture);
		return this;
	}
	
	
	
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight)
	{
		return biomeFogColor.multiply((double)(daylight * 0.94F + 0.06F), (double)(daylight * 0.94F + 0.06F), (double)(daylight * 0.91F + 0.09F));
	}

	@Override
	public boolean isFoggyAt(int x, int y)
	{
		return false;
	}

	@Override
	public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix)
    {
        return false;
    }
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		this.skyRenderer.renderSky(level, partialTick, poseStack, camera, projectionMatrix, setupFog);
        return true;
    }
	
	@Override
	public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ)
    {
        return false;
    }
}
