package net.povstalec.stellarview.client.render.level.util;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class StellarViewSkyEffects {
    public static VertexBuffer createDarkSky() {
        VertexBuffer darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);

        MeshData mesh = buildSkyDisc(bufferbuilder, -16.0F);
        darkBuffer.bind();
        darkBuffer.upload(mesh);
        VertexBuffer.unbind();

        return darkBuffer;
    }

    public static VertexBuffer createLightSky() {
        VertexBuffer skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);

        MeshData mesh = buildSkyDisc(bufferbuilder, 16.0F);
        skyBuffer.bind();
        skyBuffer.upload(mesh);
        VertexBuffer.unbind();

        return skyBuffer;
    }

    // Create the dark blue or black shading in the sky / the black circle below the horizon when in the void or below ground
    public static MeshData buildSkyDisc(BufferBuilder builder, float scale) {
        // invert the base radius based on the sign of scale to ensure the faces are facing the correct way.
        float baseRadius = 512.0F;
        float invertibleBaseRadius = Math.signum(scale) * baseRadius;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        // Create a circle with it's vertex centered by the player
        // the circle is further above / below the horizon depending on the scale
        builder.addVertex(0.0F, scale, 0.0F);
        // Create the circle
        for (int i = -180; i <= 180; i += 45) {
            float radians = (float) Math.toRadians(i);

            builder.addVertex(invertibleBaseRadius * Mth.cos(radians),
                    scale,
                    baseRadius * Mth.sin(radians));
        }

        return builder.build();
    }

    public static void renderSunrise(ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Tesselator tesselator) {
        float[] sunriseColor = level.effects().getSunriseColor(level.getTimeOfDay(partialTicks), partialTicks);
        if (sunriseColor != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            final var transformedModelView = new Matrix4f(modelViewMatrix);
            transformedModelView.rotate(Axis.XP.rotationDegrees(90.0F));
            float sunAngle = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
            transformedModelView.rotate(Axis.ZP.rotationDegrees(sunAngle));
            transformedModelView.rotate(Axis.ZP.rotationDegrees(90.0F));
            float sunriseR = sunriseColor[0];
            float sunriseG = sunriseColor[1];
            float sunriseB = sunriseColor[2];
            float sunriseA = sunriseColor[2];
            Matrix4f sunriseMatrix = transformedModelView;
            // Thanks to tehgreatdoge for finding out what these do
            // Create a cone with the vertex near the sun to act as the slanted part of the sunrise
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.addVertex(sunriseMatrix, 0.0F, 100.0F, 0.0F).setColor(sunriseR, sunriseG, sunriseB, sunriseA);
            // Make the base be around the player
            for (int i = 0; i <= 16; ++i) {
                float rotation = (float) i * ((float) Math.PI * 2F) / 16.0F;
                float x = Mth.sin(rotation);
                float y = Mth.cos(rotation);
                // The Z coordinate is multiplied by -y to make the base angle upwards towards the sun
                bufferbuilder.addVertex(sunriseMatrix, x * 120.0F, y * 120.0F, -y * 40.0F * sunriseA).setColor(sunriseR, sunriseG, sunriseB, 0.0F);
            }

            BufferUploader.drawWithShader(bufferbuilder.build());
        }
    }

    public static void renderSky(Minecraft minecraft, VertexBuffer skyBuffer, ClientLevel level, float partialTicks,
                                 PoseStack stack, Matrix4f projectionMatrix, ShaderInstance shaderinstance) {
        skyBuffer.bind();
        skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
    }

    public static void renderDark(Minecraft minecraft, VertexBuffer darkBuffer, ClientLevel level, float partialTicks,
                                  PoseStack stack, Matrix4f projectionMatrix, ShaderInstance shaderinstance, Vec3 skyColor) {
        float skyX = (float) skyColor.x;
        float skyY = (float) skyColor.y;
        float skyZ = (float) skyColor.z;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double height = minecraft.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
        if (height < 0.0D) {
            stack.pushPose();
            stack.translate(0.0F, 12.0F, 0.0F);
            darkBuffer.bind();
            darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
            VertexBuffer.unbind();
            stack.popPose();
        }

        if (level.effects().hasGround())
            RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
        else
            RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
    }
}
