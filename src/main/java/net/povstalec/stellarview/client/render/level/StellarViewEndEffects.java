package net.povstalec.stellarview.client.render.level;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.client.resourcepack.ViewCenters;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class StellarViewEndEffects extends DimensionSpecialEffects
{
	public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
	
	public StellarViewEndEffects()
	{
		super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
	}

    public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight)
    {
       return biomeFogColor.scale((double) 0.15F);
    }

    public boolean isFoggyAt(int x, int y)
    {
       return false;
    }
    
    public static void renderEndSky(PoseStack poseStack)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation("textures/environment/end_sky.png"));
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for(int i = 0; i < 6; ++i) {
           poseStack.pushPose();
           if (i == 1) {
              poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
           }

           if (i == 2) {
              poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
           }

           if (i == 3) {
              poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
           }

           if (i == 4) {
              poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
           }

           if (i == 5) {
              poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
           }

           Matrix4f matrix4f = poseStack.last().pose();
           bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
           bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
           bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
           bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
           bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
           tesselator.end();
           poseStack.popPose();
        }
     }
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(StellarViewConfig.replace_end.get())
			return ViewCenters.renderViewCenterSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return false;
    }
}
