package net.povstalec.stellarview.client.render.level.misc;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

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
		
		for(int i = -180; i <= 180; i += 45) {
			builder.vertex((double)(f1 * Mth.cos((float)i * ((float)Math.PI / 180F))), (double)scale, (double)(512.0F * Mth.sin((float)i * ((float)Math.PI / 180F)))).endVertex();
		}
		
		return builder.end();
	}
	
	default void renderSunrise(ClientLevel level, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		float[] sunriseColor = level.effects().getSunriseColor(level.getTimeOfDay(partialTicks), partialTicks);
		if(sunriseColor != null)
		{
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			stack.pushPose();
			stack.mulPose(Axis.XP.rotationDegrees(90.0F));
			float sunAngle = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
			stack.mulPose(Axis.ZP.rotationDegrees(sunAngle));
			stack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			float sunriseR = sunriseColor[0];
			float sunriseG = sunriseColor[1];
			float sunriseB = sunriseColor[2];
			float sunriseA = sunriseColor[2];
			Matrix4f sunriseMatrix = stack.last().pose();
			bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			bufferbuilder.vertex(sunriseMatrix, 0.0F, 100.0F, 0.0F).color(sunriseR, sunriseG, sunriseB, sunriseA).endVertex();
			
			for(int i = 0; i <= 16; ++i)
			{
				//TODO Find out what these do
				float f7 = (float)i * ((float)Math.PI * 2F) / 16.0F;
				float f8 = Mth.sin(f7);
				float f9 = Mth.cos(f7);
				bufferbuilder.vertex(sunriseMatrix, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * sunriseA).color(sunriseR, sunriseG, sunriseB, 0.0F).endVertex();
			}
			
			BufferUploader.drawWithShader(bufferbuilder.end());
			stack.popPose();
		}
	}
}
