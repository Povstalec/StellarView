package net.povstalec.stellarview.client.render.level;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.api.celestials.orbiting.OrbitingCelestialObject;
import net.povstalec.stellarview.client.render.level.misc.StellarViewFogEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkyEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkybox;

public class StellarViewSky implements StellarViewSkyEffects, StellarViewFogEffects
{
	protected Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	protected OrbitingCelestialObject center;
	@Nullable
	protected VertexBuffer skyBuffer;
	@Nullable
	protected VertexBuffer darkBuffer;
	
	protected StellarViewSkybox skybox = null;
	
	public StellarViewSky(OrbitingCelestialObject center)
	{
		this.center = center;
		
		this.skyBuffer = createLightSky();
		this.darkBuffer = createDarkSky();
	}
	
	public final StellarViewSky skybox(ResourceLocation texture)
	{
		this.skybox = new StellarViewSkybox(texture);
		return this;
	}
	
	
	
	//============================================================================================
	//******************************************Rendering*****************************************
	//============================================================================================
	
	// Ecliptic plane
	protected void renderEcliptic(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		if(skybox != null)
			skybox.render(level, partialTicks, stack, bufferbuilder, 0, 0, 0);
		
        this.center.renderLocalSky(level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder);
	}
	
	public void renderSky(ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, Runnable setupFog)
	{
		setupFog.run();
		
		if(this.isFoggy(this.minecraft, camera))
			return;
		
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
		
		this.renderSunrise(level, partialTicks, stack, projectionMatrix, bufferbuilder);
		
		this.renderEcliptic(level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder);
        
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

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
	}
}
