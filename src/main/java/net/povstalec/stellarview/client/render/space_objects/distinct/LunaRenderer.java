package net.povstalec.stellarview.client.render.space_objects.distinct;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.common.space_objects.distinct.Luna;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Moon;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.MoonRenderer;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.PlanetRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;
import net.povstalec.stellarview.common.util.UV;
import org.joml.Matrix4f;

public class LunaRenderer extends MoonRenderer<Luna>
{
	public static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	
	public static final UV.Quad MOON_QUAD = new UV.Quad(new UV.PhaseHandler(24000, 0, 4, 2), true);
	public static final TextureLayer MOON_TEXTURE_LAYER = new TextureLayer(MOON_LOCATION,new Color.FloatRGBA(255, 255, 255, 255),
			true, 7697847.735118539, 0.15, true, Double.MAX_VALUE, false, 90, MOON_QUAD);
	
	public LunaRenderer(Luna luna)
	{
		super(luna);
	}
	
	//============================================================================================
	//*****************************************Rendering******************************************
	//============================================================================================
	
	@Override
	
	protected void renderTextureLayers(ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
	{
		double fade = renderedObject.fadeOut(distance);
		
		if(fade <= 0)
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		if(OverworldConfig.vanilla_moon.get())
			renderTextureLayer(MOON_TEXTURE_LAYER, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, fade, ticks, distance, partialTicks);
		else
		{
			for(TextureLayer textureLayer : renderedObject.getTextureLayers())
			{
				renderTextureLayer(textureLayer, viewCenter, level, camera, bufferbuilder, lastMatrix, sphericalCoords, fade, ticks, distance, partialTicks);
			}
		}
	}
}
