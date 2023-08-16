package net.povstalec.stellarview.client.render.level.misc;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public class StellarViewSkybox
{
	public static final float DEFAULT_DISTANCE = 150.0F;
	public static final float[] FULL_UV = new float[] {0.0F, 0.0F, 1.0F, 1.0F};
	public static final float[] UP_UV = new float[] {1/3F, 0.0F, 2/3F, 0.5F};
	public static final float[] NORTH_UV = new float[] {2/3F, 0.0F, 1.0F, 0.5F};
	public static final float[] EAST_UV = new float[] {0.0F, 0.5F, 1/3F, 1.0F};
	public static final float[] SOUTH_UV = new float[] {1/3F, 0.5F, 2/3F, 1.0F};
	public static final float[] WEST_UV = new float[] {2/3F, 0.5F, 1.0F, 1.0F};
	public static final float[] DOWN_UV = new float[] {0.0F, 0.0F, 1/3F, 0.5F};
	
	public static final float[][] UV = new float[][] {UP_UV, NORTH_UV, EAST_UV, SOUTH_UV, WEST_UV, DOWN_UV};
	
	public static final float[][] THETA_PHI = new float[][]
	{
		{0.0F, 0.0F}, // UP
		{(float) Math.toRadians(180), (float) Math.toRadians(90)}, // NORTH
		{(float) Math.toRadians(90), (float) Math.toRadians(90)}, // EAST
		{(float) Math.toRadians(0), (float) Math.toRadians(90)}, // SOUTH
		{(float) Math.toRadians(-90), (float) Math.toRadians(90)}, // West
		{0.0F, (float) Math.toRadians(180)} // DOWN
	};
	
	public static final String[] SUFFIXES = new String[]
	{
		"_up",
		"_north",
		"_east",
		"_south",
		"_west",
		"_down"
	};
	
	protected Minecraft minecraft = Minecraft.getInstance();

	protected ResourceLocation texturePath;
	
	public StellarViewSkybox(ResourceLocation texturePath)
	{
		this.texturePath = texturePath;
	}
	
	/**
	 * 
	 * @param level
	 * @param partialTicks
	 * @param stack
	 * @param bufferbuilder
	 * @param uv
	 * @param playerDistance
	 * @param playerXAngle
	 * @param playerYAngle
	 * @param playerZAngle
	 */
	public void render(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder,
			float skyXAngle, float skyYAngle, float skyZAngle)
	{
		
		stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(skyXAngle));
        stack.mulPose(Axis.ZP.rotationDegrees(skyYAngle));
        stack.mulPose(Axis.XP.rotationDegrees(skyZAngle));
        
        Matrix4f lastMatrix = stack.last().pose();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.5F);
        
        for(int i = 0; i < 6; i++)
        {
        	String path = texturePath.getPath() + ".png";
        	boolean resourcePresent = minecraft.getResourceManager().getResource(texturePath.withPath(path)).isPresent();
        	path = resourcePresent ? texturePath.getPath() : texturePath.getPath() + SUFFIXES[i];
        	path = path + ".png";
        	
        	float[] uv = resourcePresent ? UV[i] : FULL_UV;
        	
        	this.renderFacade(bufferbuilder, lastMatrix, texturePath.withPath(path), THETA_PHI[i][0], THETA_PHI[i][1], uv);
        }
        
		/*this.renderFacade(bufferbuilder, lastMatrix, texturePath, 0.0F, 0.0F, UP_UV);
		this.renderFacade(bufferbuilder, lastMatrix, texturePath, (float) Math.toRadians(180), (float) Math.toRadians(90), NORTH_UV);
		this.renderFacade(bufferbuilder, lastMatrix, texturePath, (float) Math.toRadians(90), (float) Math.toRadians(90), EAST_UV);
		this.renderFacade(bufferbuilder, lastMatrix, texturePath, (float) Math.toRadians(0), (float) Math.toRadians(90), SOUTH_UV);
		this.renderFacade(bufferbuilder, lastMatrix, texturePath, (float) Math.toRadians(-90), (float) Math.toRadians(90), WEST_UV);
		this.renderFacade(bufferbuilder, lastMatrix, texturePath, 0.0F, (float) Math.toRadians(180), DOWN_UV);*/
        
		RenderSystem.defaultBlendFunc();
        stack.popPose();
	}
	
	protected void renderFacade(BufferBuilder bufferbuilder, Matrix4f lastMatrix, ResourceLocation texture,
			float theta, float phi, float[] uv)
	{
		if(!minecraft.getResourceManager().getResource(texture).isPresent())
			return;
		
		float[] corner00 = StellarCoordinates.placeOnSphere(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE, theta, phi, 0);
		float[] corner10 = StellarCoordinates.placeOnSphere(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE, theta, phi, 0);
		float[] corner11 = StellarCoordinates.placeOnSphere(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE, theta, phi, 0);
		float[] corner01 = StellarCoordinates.placeOnSphere(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE, theta, phi, 0);
		
		RenderSystem.setShaderTexture(0, texture);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(lastMatrix, corner00[0], corner00[1], corner00[2]).uv(uv[0], uv[3]).endVertex();
        bufferbuilder.vertex(lastMatrix, corner10[0], corner10[1], corner10[2]).uv(uv[2], uv[3]).endVertex();
        bufferbuilder.vertex(lastMatrix, corner11[0], corner11[1], corner11[2]).uv(uv[2], uv[1]).endVertex();
        bufferbuilder.vertex(lastMatrix, corner01[0], corner01[1], corner01[2]).uv(uv[0], uv[1]).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
	}
}
