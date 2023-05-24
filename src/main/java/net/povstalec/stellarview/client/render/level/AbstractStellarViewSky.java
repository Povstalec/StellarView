package net.povstalec.stellarview.client.render.level;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.api.celestial_objects.CelestialObject;
import net.povstalec.stellarview.client.render.level.misc.StellarViewFogEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewGalaxy;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkyEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkybox;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public abstract class AbstractStellarViewSky implements StellarViewSkyEffects, StellarViewFogEffects
{
	public static final float[] FULL_UV = new float[] {0.0F, 0.0F, 1.0F, 1.0F};
	protected Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	protected VertexBuffer starBuffer;
	@Nullable
	protected VertexBuffer skyBuffer;
	@Nullable
	protected VertexBuffer darkBuffer;
	
	protected StellarViewSkybox skybox = null;
	
	protected List<CelestialObject> celestialObjects = new ArrayList<CelestialObject>();
	
	protected AbstractStellarViewSky()
	{
		this.starBuffer = StellarViewGalaxy.createStars(StellarViewGalaxy.Type.VANILLA, 0, 0, 0, 0, 0, 0, 0, 0);
		this.skyBuffer = createLightSky();
		this.darkBuffer = createDarkSky();
	}
	
	// Ecliptic plane
	protected void renderEcliptic(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder, float rain)
	{
		double zPos = camera.getEntity().getPosition(partialTicks).z();
		float zRotation = 2 * (float) Math.toDegrees(Math.atan(zPos / 30000000));
		
		stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        stack.mulPose(Axis.ZP.rotationDegrees(zRotation));
        stack.mulPose(Axis.XP.rotationDegrees((level.getTimeOfDay(partialTicks) + (float) level.getDayTime() / 24000 / 96) * 360.0F));
        
        if(!StellarViewConfig.disable_stars.get())
        	this.renderStars(level, camera, partialTicks, rain, stack, projectionMatrix, setupFog);

        stack.popPose();
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		if(skybox != null)
			skybox.render(level, partialTicks, stack, bufferbuilder, 0, 0, 0);
		
        RenderSystem.setShaderColor(rain, rain, rain, rain);
        
        //TODO Render Celestial Bodies
        this.celestialObjects.stream().forEach(object ->
        {
        	object.render(level, partialTicks, stack, bufferbuilder, FULL_UV,
            		100.0F, 360 * level.getTimeOfDay(partialTicks), -90.0F, zRotation);
        });
	}
	
	protected void renderStars(ClientLevel level, Camera camera, float partialTicks, float rain, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog)
	{
		//TODO Stars visible during the day
		float starBrightness = level.getStarBrightness(partialTicks);
		starBrightness = StellarViewConfig.day_stars.get() && starBrightness < 0.5F ? 
				0.5F : starBrightness;
		if(StellarViewConfig.bright_stars.get())
			starBrightness = starBrightness * (1 + ((float) (15 - level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15)) / 15));
		starBrightness = starBrightness * rain;
		
		if(starBrightness > 0.0F)
		{
			RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
			FogRenderer.setupNoFog();
			
			this.starBuffer.bind();
			this.starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
			VertexBuffer.unbind();
			
			setupFog.run();
		}
	}
	
	public void renderSky(ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, Runnable setupFog)
	{
		setupFog.run();
		
		if(this.isFoggy(this.minecraft, camera))
			return;
		
		RenderSystem.disableTexture();
		Vec3 skyColor = level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTicks);
		float skyX = (float)skyColor.x;
        float skyY = (float)skyColor.y;
        float skyZ = (float)skyColor.z;
        FogRenderer.levelFogColor();
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
		ShaderInstance shaderinstance = RenderSystem.getShader();
		this.skyBuffer.bind();
		this.skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
		VertexBuffer.unbind();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		this.renderSunrise(level, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder);
		
		RenderSystem.enableTexture();
		
		float rain = 1.0F - level.getRainLevel(partialTicks);
		this.renderEcliptic(level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder, rain);
        
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double height = this.minecraft.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
        if(height < 0.0D)
        {
        	stack.pushPose();
        	stack.translate(0.0F, 12.0F, 0.0F);
        	this.darkBuffer.bind();
        	this.darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
        	VertexBuffer.unbind();
        	stack.popPose();
        }
        
        if(level.effects().hasGround())
        	RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
        else
        	RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
        
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
	}
}
