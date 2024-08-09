package net.povstalec.stellarview.api;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.client.render.level.StellarViewSky;
import net.povstalec.stellarview.client.resourcepack.ViewCenters;

/**
 * Extend this to create your own custom Special Effects
 * @author Povstalec
 *
 */
public class StellarViewSpecialEffects extends DimensionSpecialEffects
{
	public StellarViewSky skyRenderer;
	
	public StellarViewSpecialEffects(StellarViewSky skyRenderer, float cloudLevel, boolean hasGround, SkyType skyType, 
			boolean forceBrightLightmap, boolean constantAmbientLight)
	{
		super(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
		
		this.skyRenderer = skyRenderer;
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

	/*@Override
	public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix)
    {
        return false;
    }*/
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		//ViewCenters.renderViewCenterSky(level.dimension().location(), level, partialTick, poseStack, Tesselator.getInstance().getBuilder());
		//this.skyRenderer.renderSky(level, partialTick, poseStack, camera, projectionMatrix, setupFog);
        //return true;
        return ViewCenters.renderViewCenterSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
    }
	
	/*@Override
	public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ)
    {
        return false;
    }*/
	
	public static float getSkyDarken(ClientLevel level, float partialTicks)
	{
		float timeOfDay = level.getTimeOfDay(partialTicks);
		float darken = 1.0F - (Mth.cos(timeOfDay * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
		darken = Mth.clamp(darken, 0.0F, 1.0F);
		darken = 1.0F - darken;
		darken *= 1.0F - level.getRainLevel(partialTicks) * 5.0F / 16.0F;
		darken *= 1.0F - level.getThunderLevel(partialTicks) * 5.0F / 16.0F;
		
		float darkMultiplier = 0.8F;
		
		return darken * darkMultiplier + 0.2F;
	}
	
	@Override
	public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float skyLight, float blockLight, int pixelX, int pixelY, Vector3f colors)
    {
		float darkMultiplier = getSkyDarken(level, 1.0F);
		
		boolean darkerWorld = true;
		if(darkerWorld)
		{
			float trueSkyDarken;
			if(level.getSkyFlashTime() > 0)
				trueSkyDarken = 1.0F;
			else
				trueSkyDarken = darkMultiplier * 0.95F + 0.05F;
			
			Vector3f skyVector = (new Vector3f(trueSkyDarken, trueSkyDarken, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
			Vector3f lightColor = new Vector3f();
			float naturalLight = LightTexture.getBrightness(level.dimensionType(), pixelY) * trueSkyDarken; // pixelY represents natural light
			float artificialLight = LightTexture.getBrightness(level.dimensionType(), pixelX) * skyLight; // pixelX represents artificial light
			float f10 = artificialLight * ((artificialLight * 0.6F + 0.4F) * 0.6F + 0.4F);
			float f11 = artificialLight * (artificialLight * artificialLight * 0.6F + 0.4F);
			lightColor.set(artificialLight, f10, f11);
			if(level.effects().forceBrightLightmap())
			{
				lightColor.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
				clampColor(lightColor);
			}
			else
			{
				Minecraft minecraft = Minecraft.getInstance();
				Vector3f vector3f2 = (new Vector3f((Vector3fc)skyVector)).mul(naturalLight);
				lightColor.add(vector3f2);
				lightColor.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
				if(minecraft.gameRenderer.getDarkenWorldAmount(partialTicks) > 0.0F)
				{
					float f12 = minecraft.gameRenderer.getDarkenWorldAmount(partialTicks);
					Vector3f vector3f3 = (new Vector3f((Vector3fc)lightColor)).mul(0.7F, 0.6F, 0.6F);
					lightColor.lerp(vector3f3, f12);
				}
			}
			colors.set(lightColor);
		}
	}
	
	/**
	 * @param color
	 * @see Copied from LightTexture#clampColor(Vector3f)
	 */
	public static void clampColor(Vector3f color)
	{
		color.set(Mth.clamp(color.x, 0.0F, 1.0F), Mth.clamp(color.y, 0.0F, 1.0F), Mth.clamp(color.z, 0.0F, 1.0F));
	}
}
