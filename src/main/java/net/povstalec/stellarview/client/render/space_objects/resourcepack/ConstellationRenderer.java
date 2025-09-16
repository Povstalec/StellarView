package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Constellation;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.client.util.StarData;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

import java.util.ArrayList;

public class ConstellationRenderer<T extends Constellation> extends SpaceObjectRenderer<T>
{
	protected boolean hasTexture = GeneralConfig.textured_stars.get();
	
	protected StarData starData;
	
	public ConstellationRenderer(T constellation)
	{
		super(constellation);
	}
	
	public ResourceLocation getStarTexture()
	{
		if(renderedObject().getStarTexture() != null)
			return renderedObject().getStarTexture();
		
		return StarField.DEFAULT_STAR_TEXTURE;
	}
	
	// Constellation should render itself only when it has no Star Field which it would be a part of, or when it specifies its own star texture
	public boolean shouldRender()
	{
		return !renderedObject().hasStarField() || renderedObject().getStarTexture() != null;
	}
	
	public boolean requiresReset()
	{
		return hasTexture != GeneralConfig.textured_stars.get();
	}
	
	public void reset()
	{
		if(starData != null)
			starData.reset();
	}
	
	//============================================================================================
	//*******************************************Stars********************************************
	//============================================================================================
	
	protected void generateStars(StarData.LOD lod, ArrayList<Constellation.StarDefinition> definedStars)
	{
		for(Constellation.StarDefinition star : definedStars)
		{
			lod.newStar(star, this.spaceCoords());
		}
	}
	
	protected void setStars()
	{
		starData = new StarData()
		{
			@Override
			protected LOD newStars(StarField.LevelOfDetail levelOfDetail)
			{
				int stars;
				ArrayList<Constellation.StarDefinition> definedStars;
				
				switch(levelOfDetail)
				{
					case LOD1:
						stars = renderedObject.lod1stars().size();
						definedStars = renderedObject.lod1stars();
						break;
					case LOD2:
						stars = renderedObject.lod2stars().size();
						definedStars = renderedObject.lod2stars();
						break;
					default:
						stars = renderedObject.lod3stars().size();
						definedStars = renderedObject.lod3stars();
				}
				
				LOD lod = new LOD(stars);
				generateStars(lod, definedStars);
				return lod;
			}
		};
	}
	
	@Override
	public void render(ViewCenter viewCenter, ClientLevel level, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder, Vector3f parentVector, AxisRotation parentRotation)
	{
		if(shouldRender() && !GeneralConfig.disable_stars.get() && viewCenter.starBrightness() > 0.0F)
		{
			SpaceCoords difference = viewCenter.getCoords().sub(spaceCoords());
			
			if(starData == null)
				setStars();
			else if(requiresReset())
			{
				hasTexture = GeneralConfig.textured_stars.get();
				starData.reset();
			}
			
			stack.pushPose();
			
			if(hasTexture)
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, viewCenter.starBrightness());
			if(hasTexture)
				RenderSystem.setShaderTexture(0, getStarTexture());
			FogRenderer.setupNoFog();
			
			Quaternion q = SpaceCoords.getQuaternionf(level, viewCenter, partialTicks);
			
			stack.mulPose(q);
			this.starData.renderStars(StarField.LevelOfDetail.fromDistance(difference), stack.last().pose(), projectionMatrix, difference, viewCenter.isStatic(), hasTexture);
			
			setupFog.run();
			stack.popPose();
		}
		
		for(SpaceObjectRenderer<?> child : children)
		{
			child.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, parentVector, new AxisRotation(0, 0, 0));
		}
	}
}
