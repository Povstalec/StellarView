package net.povstalec.stellarview.client.resourcepack;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.misc.StellarViewFogEffects;
import net.povstalec.stellarview.client.render.level.misc.StellarViewSkyEffects;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public class ViewCenter implements StellarViewSkyEffects, StellarViewFogEffects
{
	@Nullable
	private ResourceKey<SpaceObject> viewCenterKey;
	@Nullable
	private SpaceObject viewCenter;
	
	@Nullable
	private List<Skybox> skyboxes;
	
	private Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	private VertexBuffer skyBuffer = createLightSky();
	@Nullable
	private VertexBuffer darkBuffer = createDarkSky();
	
	private SpaceCoords coords;
	private AxisRotation axisRotation; //TODO Is this really necessary? I'd say the viewCenter axis rotation could be used here instead
	
	@Nullable
	private MeteorEffect.ShootingStar shootingStar;
	@Nullable
	private MeteorEffect.MeteorShower meteorShower;
	
    
    public static final Codec<ViewCenter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		SpaceObject.RESOURCE_KEY_CODEC.optionalFieldOf("view_center").forGetter(ViewCenter::getViewCenterKey),
			Skybox.CODEC.listOf().optionalFieldOf("skyboxes").forGetter(ViewCenter::getSkyboxes),
			
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(ViewCenter::getAxisRotation),
			
			MeteorEffect.ShootingStar.CODEC.optionalFieldOf("shooting_star").forGetter(ViewCenter::getShootingStar),
			MeteorEffect.MeteorShower.CODEC.optionalFieldOf("meteor_shower").forGetter(ViewCenter::getMeteorShower)
			).apply(instance, ViewCenter::new));
	
	public ViewCenter(Optional<ResourceKey<SpaceObject>> viewCenterKey, Optional<List<Skybox>> skyboxes, AxisRotation axisRotation, Optional<MeteorEffect.ShootingStar> shootingStar, Optional<MeteorEffect.MeteorShower> meteorShower)
	{
		if(viewCenterKey.isPresent())
			this.viewCenterKey = viewCenterKey.get();
		
		if(skyboxes.isPresent())
			this.skyboxes = skyboxes.get();
		
		this.axisRotation = axisRotation;
		
		if(shootingStar.isPresent())
			this.shootingStar = shootingStar.get();
		
		if(meteorShower.isPresent())
			this.meteorShower = meteorShower.get();
	}
	
	public boolean setViewCenterObject(HashMap<ResourceLocation, SpaceObject> spaceObjects)
	{
		if(viewCenterKey != null)
		{
			if(spaceObjects.containsKey(viewCenterKey.location()))
			{
				viewCenter = spaceObjects.get(viewCenterKey.location());
				return true;
			}
			
			StellarView.LOGGER.error("Failed to register View Center because view center object " + viewCenterKey.location() + " could not be found");
			return false;
		}
		
		return true;
	}
	
	public Optional<ResourceKey<SpaceObject>> getViewCenterKey()
	{
		if(viewCenterKey != null)
			return Optional.of(viewCenterKey);
		
		return Optional.empty();
	}
	
	public Optional<List<Skybox>> getSkyboxes()
	{
		if(skyboxes != null)
			return Optional.of(skyboxes);
		
		return Optional.empty();
	}
	
	public SpaceCoords getCoords()
	{
		return coords;
	}
	
	public void addCoords(SpaceCoords other)
	{
		this.coords = this.coords.add(other);
	}
	
	public void addCoords(Vector3f vector)
	{
		this.coords = this.coords.add(vector);
	}
	
	public void subCoords(SpaceCoords other)
	{
		this.coords = this.coords.sub(other);
	}
	
	public AxisRotation getAxisRotation()
	{
		return axisRotation;
	}
	
	public Optional<MeteorEffect.ShootingStar> getShootingStar()
	{
		if(shootingStar != null)
			return Optional.of(shootingStar);
		
		return Optional.empty();
	}
	
	public Optional<MeteorEffect.MeteorShower> getMeteorShower()
	{
		if(meteorShower != null)
			return Optional.of(meteorShower);
		
		return Optional.empty();
	}
	
	public boolean objectEquals(SpaceObject spaceObject)
	{
		if(this.viewCenter != null)
			return spaceObject == this.viewCenter;
		
		return false;
	}
	
	private boolean renderSkybox(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		if(skyboxes == null)
			return false;
		
		for(Skybox skybox : skyboxes)
		{
			skybox.render(level, partialTicks, stack, bufferbuilder);
		}
		
		return true;
	}
	
	private void renderSkyEvents(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		if(shootingStar != null)
			shootingStar.render(level, camera, partialTicks, stack, bufferbuilder);
		
		if(meteorShower != null)
			meteorShower.render(level, camera, partialTicks, stack, bufferbuilder);
	}
	
	private boolean renderSkyObjectsFrom(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		if(viewCenter == null)
			return false;
		
		coords = viewCenter.getCoords();
		
		stack.pushPose();

		stack.mulPose(Axis.YP.rotationDegrees((float) axisRotation.yAxis)); //TODO Rotation of the sky depending on where you are
		stack.mulPose(Axis.ZP.rotationDegrees((float) axisRotation.zAxis)); //TODO Rotation of the sky because you're on the surface
		stack.mulPose(Axis.XP.rotationDegrees((float) axisRotation.xAxis)); //TODO Rotation of the planet
		
		viewCenter.renderFrom(this, level, partialTicks, stack, camera, projectionMatrix, isFoggy(minecraft, camera), setupFog, bufferbuilder);

		stack.popPose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		renderSkyEvents(level, camera, partialTicks, stack, bufferbuilder);
		return true;
	}
	
	public void renderSkyObjects(ClientLevel level, float partialTicks, PoseStack stack, Camera camera, 
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		Space.render(this, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	public boolean renderSky(ClientLevel level, int ticks, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(viewCenter == null && skyboxes == null)
			return false;
		
		setupFog.run();
		
		if(!this.isFoggy(this.minecraft, camera))
		{
			RenderSystem.disableTexture();
			Vec3 skyColor = level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTicks);
			float skyX = (float)skyColor.x;
	        float skyY = (float)skyColor.y;
	        float skyZ = (float)skyColor.z;
	        FogRenderer.levelFogColor();
			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
			ShaderInstance shaderinstance = RenderSystem.getShader();
			this.skyBuffer.bind();
			this.skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
			VertexBuffer.unbind();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			this.renderSunrise(level, partialTicks, stack, projectionMatrix, bufferbuilder);
			
			RenderSystem.enableTexture();

			RenderSystem.setShader(GameRenderer::getPositionTexShader);

			renderSkybox(level, partialTicks, stack, bufferbuilder);
			renderSkyObjectsFrom(level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder);
	        
	        RenderSystem.disableTexture();
	        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderSystem.disableBlend();
	        
	        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
	        double height = this.minecraft.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
	        if(height < 0.0D)
	        {
	        	stack.pushPose();
	        	stack.translate(0.0F, 12.0F, 0.0F);
	        	this.darkBuffer.bind();
	        	this.darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
	        	VertexBuffer.unbind();
	        	stack.popPose();
	        }
	        
	        if(level.effects().hasGround())
	        	RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
	        else
	        	RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
	        
	        RenderSystem.enableTexture();
	        RenderSystem.depthMask(true);
		}
		
		return true;
	}
}
