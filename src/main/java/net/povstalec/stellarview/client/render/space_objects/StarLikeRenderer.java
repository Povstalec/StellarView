package net.povstalec.stellarview.client.render.space_objects;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.api.common.space_objects.StarLike;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import org.joml.Matrix4f;

public abstract class StarLikeRenderer<T extends StarLike> extends OrbitingObjectRenderer<T>
{
	public StarLikeRenderer(T starLike)
	{
		super(starLike);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  double fade, long ticks, double distance, float partialTicks)
	{
		double lyDistance = distance / SpaceCoords.KM_PER_LY;
		
		Color.FloatRGBA starRGBA = renderedObject.starRGBA(lyDistance);
		
		if(starRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(renderedObject.distanceSize(distance));

		if(size < textureLayer.minSize()) {
			if (textureLayer.clampAtMinSize()) {
				size = (float) textureLayer.minSize();

				// Once the star has reached its usual min size, it will start getting smaller slowly again, but only up to a certain point
				size = renderedObject.starSize(size, lyDistance);
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
		
		renderOnSphere(textureLayer.rgba(), starRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, LightEffects.dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
	
	
}
