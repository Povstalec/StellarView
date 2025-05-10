package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Moon;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import org.joml.Matrix4f;

public class MoonRenderer<T extends Moon> extends PlanetRenderer<T>
{
	public MoonRenderer(T moon)
	{
		super(moon);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder,
									  Matrix4f lastMatrix, SphericalCoords sphericalCoords,
									  double fade, long ticks, double distance, float partialTicks)
	{
		if(!StellarView.isEnhancedCelestialsLoaded() && !StellarView.isLunarLoaded())
		{
			super.renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, fade, ticks, distance, partialTicks);
			return;
		}
		
		Color.FloatRGBA moonRGBA = renderedObject.moonRGBA(level, partialTicks);
		
		if(moonRGBA.alpha() <= 0.0F || textureLayer.rgba().alpha() <= 0)
			return;
		
		float size = (float) textureLayer.mulSize(renderedObject.distanceSize(distance));
		
		if(size < textureLayer.minSize())
		{
			if(textureLayer.clampAtMinSize())
				size = (float) textureLayer.minSize();
			else
				return;
		}
		
		size *= renderedObject.sizeMultiplier(level);
		
		renderOnSphere(textureLayer.rgba(), moonRGBA, textureLayer.texture(), textureLayer.uv(),
				level, camera, bufferbuilder, lastMatrix, sphericalCoords,
				ticks, distance, partialTicks, LightEffects.dayBrightness(viewCenter, size, ticks, level, camera, partialTicks) * (float) fade, size, (float) textureLayer.rotation(), textureLayer.shoulBlend());
	}
}
