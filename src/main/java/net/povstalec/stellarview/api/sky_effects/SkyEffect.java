package net.povstalec.stellarview.api.sky_effects;

import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.config.StellarViewConfigValue;

public abstract class SkyEffect
{
	public static final float[] FULL_UV = new float[] {0.0F, 0.0F, 1.0F, 1.0F};
	public static final float DEFAULT_DISTANCE = 100.0F;
	
	protected Optional<StellarViewConfigValue.IntValue> value = Optional.empty();
	protected int defaultRarity = 0;
	
	public SkyEffect(int defaultRarity)
	{
		this.defaultRarity = defaultRarity;
	}
	
	public SkyEffect setRarityValue(StellarViewConfigValue.IntValue value)
	{
		this.value = Optional.of(value);
		
		return this;
	}
	
	public SkyEffect setDefaultRarity(int defaultRarity)
	{
		this.defaultRarity = defaultRarity;
		
		return this;
	}
	
	public int getRarity()
	{
		return value.isPresent() ? value.get().get() : defaultRarity;
	}
	
	protected float getBrightness(ClientLevel level, Camera camera, float partialTicks)
	{
		float brightness = level.getStarBrightness(partialTicks);
		brightness = StellarViewConfig.day_stars.get() && brightness < 0.5F ? 
				0.5F : brightness;
		if(StellarViewConfig.bright_stars.get())
			brightness = brightness * (1 + ((float) (15 - level.getLightEngine().getRawBrightness(camera.getEntity().getOnPos().above(), 15)) / 15));
		return brightness * (1.0F - level.getRainLevel(partialTicks));
	}
	
	
	
	protected void renderEffect(BufferBuilder bufferbuilder, Matrix4f lastMatrix, ResourceLocation texture, float[] uv,
			float size, float rotation, float theta, float phi, float brightness)
	{
		if(uv == null || uv.length < 4)
			uv = FULL_UV;
		
		Vector3f corner00 = StellarCoordinates.placeOnSphere(-size, -size, DEFAULT_DISTANCE, theta, phi, rotation);
		Vector3f corner10 = StellarCoordinates.placeOnSphere(size, -size, DEFAULT_DISTANCE, theta, phi, rotation);
		Vector3f corner11 = StellarCoordinates.placeOnSphere(size, size, DEFAULT_DISTANCE, theta, phi, rotation);
		Vector3f corner01 = StellarCoordinates.placeOnSphere(-size, size, DEFAULT_DISTANCE, theta, phi, rotation);
		
		if(brightness > 0.0F)
		{
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, brightness);
			
			RenderSystem.setShaderTexture(0, texture);
	        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
	        bufferbuilder.vertex(lastMatrix, corner00.x, corner00.y, corner00.z).uv(uv[0], uv[1]).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner10.x, corner10.y, corner10.z).uv(uv[2], uv[1]).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner11.x, corner11.y, corner11.z).uv(uv[2], uv[3]).endVertex();
	        bufferbuilder.vertex(lastMatrix, corner01.x, corner01.y, corner01.z).uv(uv[0], uv[3]).endVertex();
	        BufferUploader.drawWithShader(bufferbuilder.end());
			RenderSystem.defaultBlendFunc();
		}
	}
	
	public abstract void render(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder);
}
