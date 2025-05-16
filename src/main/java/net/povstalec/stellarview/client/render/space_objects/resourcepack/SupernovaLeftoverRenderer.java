package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.api.common.space_objects.SupernovaLeftover;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Star;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.space_objects.GravityLenseRenderer;
import net.povstalec.stellarview.client.render.space_objects.StarLikeRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import org.joml.Matrix4f;

public class SupernovaLeftoverRenderer<T extends SupernovaLeftover> extends StarLikeRenderer<T>
{
    private Star originObject;
    public SupernovaLeftoverRenderer(Star parent, T leftover)
    {
        super(leftover);
        this.originObject = parent;
    }

    //============================================================================================
    //*****************************************Rendering******************************************
    //============================================================================================

    @Override
    protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
                                      Matrix4f lastMatrix, SphericalCoords sphericalCoords,
                                      double fade, long ticks, double distance, float partialTicks)
    {
        if(originObject.starSize((float) textureLayer.mulSize(1/distance), distance) > renderedObject().getMinStarSize())
            return;

        double lyDistance = distance / SpaceCoords.KM_PER_LY;

        Color.FloatRGBA starRGBA = renderedObject.starRGBA(lyDistance);

        if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
            return;

        float size = (float) textureLayer.mulSize(renderedObject.distanceSize(distance));

        if(size < textureLayer.minSize())
        {
            if(textureLayer.clampAtMinSize())
            {
                size = (float) textureLayer.minSize();

                // Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
                size = renderedObject.starSize(size, lyDistance);
            }
            else
                return;
        }

        renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
                level, camera, bufferbuilder, lastMatrix, sphericalCoords,
                ticks, distance, partialTicks, LightEffects.starDayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
    }

    @Override
    protected void renderTextureLayers(ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
    {
        double fade = renderedObject.fadeOut(distance);

        if(fade <= 0)
            return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        for(TextureLayer textureLayer : renderedObject.getTextureLayers())
        {
            renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, fade, ticks, distance, partialTicks);
        }
    }
}
