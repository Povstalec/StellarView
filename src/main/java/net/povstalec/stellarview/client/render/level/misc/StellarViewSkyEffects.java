package net.povstalec.stellarview.client.render.level.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public interface StellarViewSkyEffects
{
	default VertexBuffer createDarkSky()
	{
		VertexBuffer darkBuffer = new VertexBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, -16.0F);
		darkBuffer.bind();
		darkBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return darkBuffer;
	}

	default VertexBuffer createLightSky()
	{
		VertexBuffer skyBuffer = new VertexBuffer();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, 16.0F);
		skyBuffer.bind();
		skyBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
		
		return skyBuffer;
	}

	public static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder builder, float scale)
	{
		//TODO Find out what this does
		float f = 512.0F;
		float f1 = Math.signum(scale) * f;
		RenderSystem.setShader(GameRenderer::getPositionShader);
		builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		builder.vertex(0.0D, (double)scale, 0.0D).endVertex();
		
		for(int i = -180; i <= 180; i += 45)
		{
			builder.vertex((double)(f1 * Mth.cos((float)i * ((float)Math.PI / 180F))), (double)scale, (double)(512.0F * Mth.sin((float)i * ((float)Math.PI / 180F)))).endVertex();
		}
		
		return builder.end();
	}
	
	default void renderSunrise(ClientLevel level, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, BufferBuilder bufferbuilder)
	{
		float[] sunriseColor = level.effects().getSunriseColor(level.getTimeOfDay(partialTicks), partialTicks);
		if(sunriseColor != null)
		{
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.disableTexture();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			stack.pushPose();
			stack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			float sunAngle = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
			stack.mulPose(Vector3f.ZP.rotationDegrees(sunAngle));
			stack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
			float sunriseR = sunriseColor[0];
			float sunriseG = sunriseColor[1];
			float sunriseB = sunriseColor[2];
			float sunriseA = sunriseColor[2];
			Matrix4f sunriseMatrix = stack.last().pose();
			bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			bufferbuilder.vertex(sunriseMatrix, 0.0F, 100.0F, 0.0F).color(sunriseR, sunriseG, sunriseB, sunriseA).endVertex();
			
			for(int i = 0; i <= 16; ++i)
			{
				// Thanks to tehgreatdoge for finding out what these do
				// Create a circle to act as the slanted portion of the sunrise
				float rotation = (float)i * ((float)Math.PI * 2F) / 16.0F;
				float x = Mth.sin(rotation);
				float y = Mth.cos(rotation);
				// The Z coordinate is multiplied by -y to make the circle angle upwards towards the sun
				bufferbuilder.vertex(sunriseMatrix, x * 120.0F, y * 120.0F, -y * 40.0F * sunriseA).color(sunriseR, sunriseG, sunriseB, 0.0F).endVertex();
			}
			
			BufferUploader.drawWithShader(bufferbuilder.end());
			stack.popPose();
		}
	}
	
	default void renderSky(Minecraft minecraft, VertexBuffer skyBuffer, ClientLevel level, float partialTicks,
			PoseStack stack, Matrix4f projectionMatrix, ShaderInstance shaderinstance)
	{
		skyBuffer.bind();
		skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
	}
	
	default void renderDark(Minecraft minecraft, VertexBuffer darkBuffer, ClientLevel level, float partialTicks,
			PoseStack stack, Matrix4f projectionMatrix, ShaderInstance shaderinstance, Vec3 skyColor)
	{
		float skyX = (float)skyColor.x;
        float skyY = (float)skyColor.y;
        float skyZ = (float)skyColor.z;
        
		RenderSystem.disableTexture();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double height = minecraft.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
        if(height < 0.0D)
        {
        	stack.pushPose();
        	stack.translate(0.0F, 12.0F, 0.0F);
        	darkBuffer.bind();
        	darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
        	VertexBuffer.unbind();
        	stack.popPose();
        }
        
        if(level.effects().hasGround())
        	RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
        else
        	RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
        
        RenderSystem.enableTexture();
	}
}
