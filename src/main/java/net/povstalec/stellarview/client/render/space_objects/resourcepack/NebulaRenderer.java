package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Nebula;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.space_objects.TexturedObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import org.joml.Matrix4f;

public class NebulaRenderer<T extends Nebula> extends TexturedObjectRenderer<T>
{
	public NebulaRenderer(T nebula)
	{
		super(nebula);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, Tesselator tesselator,
									  Matrix4f lastMatrix,SphericalCoords sphericalCoords, double fade, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;
		
		Color.FloatRGBA nebulaRGBA = renderedObject.nebulaRGBA(lyDistance);
		
		if(nebulaRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(renderedObject.distanceSize(distance));

		if(size < textureLayer.minSize()) {
			if (textureLayer.clampAtMinSize()) {
				size = (float) textureLayer.minSize();

				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = renderedObject.nebulaSize(size, lyDistance);
			}
			else
				return;
		}
		else if(size > textureLayer.maxSize()) {
			if (textureLayer.clampAtMaxSize())
				size = (float) textureLayer.maxSize();
			else
				return;
		}
		
		renderOnSphere(textureLayer.rgba(), nebulaRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, tesselator, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, LightEffects.nebulaBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
}
