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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.render.level.util.StellarViewFogEffects;
import net.povstalec.stellarview.client.render.level.util.StellarViewSkyEffects;
import net.povstalec.stellarview.client.resourcepack.effects.MeteorEffect;
import net.povstalec.stellarview.client.resourcepack.objects.OrbitingObject;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public class ViewCenter
{
	public static final float DAY_MAX_BRIGHTNESS = 0.25F;
	
	public static final float DAY_MIN_VISIBLE_SIZE = 2.5F;
	public static final float DAY_MAX_VISIBLE_SIZE = 10F;
	
	@Nullable
	private ResourceKey<SpaceObject> viewCenterKey;
	@Nullable
	private SpaceObject viewCenterObject;
	
	@Nullable
	private List<Skybox> skyboxes;
	
	private Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	private VertexBuffer skyBuffer;
	@Nullable
	private VertexBuffer darkBuffer;
	
	private SpaceCoords coords;
	private AxisRotation axisRotation;
	private long rotationPeriod;
	
	private final MeteorEffect.ShootingStar shootingStar;
	private final MeteorEffect.MeteorShower meteorShower;
	
	public final float dayMaxBrightness;

	public final float dayMinVisibleSize;
	public final float dayMaxVisibleSize;
	public final float dayVisibleSizeRange;
	
	public final boolean createHorizon;
	public final boolean createVoid;
	
	public final boolean starsAlwaysVisible;
	public final int zRotationMultiplier;
    
    public static final Codec<ViewCenter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		SpaceObject.RESOURCE_KEY_CODEC.optionalFieldOf("view_center").forGetter(ViewCenter::getViewCenterKey),
			Skybox.CODEC.listOf().optionalFieldOf("skyboxes").forGetter(ViewCenter::getSkyboxes),
			
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(ViewCenter::getAxisRotation),
			Codec.LONG.optionalFieldOf("rotation_period", 0L).forGetter(ViewCenter::getRotationPeriod),
			
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("day_max_brightness", DAY_MAX_BRIGHTNESS).forGetter(viewCenter -> viewCenter.dayMaxBrightness),
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("day_min_visible_size", DAY_MIN_VISIBLE_SIZE).forGetter(viewCenter -> viewCenter.dayMinVisibleSize),
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("day_max_visible_size", DAY_MAX_VISIBLE_SIZE).forGetter(viewCenter -> viewCenter.dayMaxVisibleSize),
			
			MeteorEffect.ShootingStar.CODEC.optionalFieldOf("shooting_star", new MeteorEffect.ShootingStar()).forGetter(ViewCenter::getShootingStar),
			MeteorEffect.MeteorShower.CODEC.optionalFieldOf("meteor_shower", new MeteorEffect.MeteorShower()).forGetter(ViewCenter::getMeteorShower),
			
			Codec.BOOL.optionalFieldOf("create_horizon", true).forGetter(viewCenter -> viewCenter.createHorizon),
			Codec.BOOL.optionalFieldOf("create_void", true).forGetter(viewCenter -> viewCenter.createVoid),
			
			Codec.BOOL.optionalFieldOf("stars_always_visible", false).forGetter(viewCenter -> viewCenter.starsAlwaysVisible),
			Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("z_rotation_multiplier", 30000000).forGetter(viewCenter -> viewCenter.zRotationMultiplier)
			).apply(instance, ViewCenter::new));
	
	public ViewCenter(Optional<ResourceKey<SpaceObject>> viewCenterKey, Optional<List<Skybox>> skyboxes, AxisRotation axisRotation,
			long rotationPeriod, float dayMaxBrightness, float dayMinVisibleSize, float dayMaxVisibleSize,
			MeteorEffect.ShootingStar shootingStar, MeteorEffect.MeteorShower meteorShower,
			boolean createHorizon, boolean createVoid,
			boolean starsAlwaysVisible, int zRotationMultiplier)
	{
		if(viewCenterKey.isPresent())
			this.viewCenterKey = viewCenterKey.get();
		
		if(skyboxes.isPresent())
			this.skyboxes = skyboxes.get();
		
		this.axisRotation = axisRotation;
		this.rotationPeriod = rotationPeriod;
		
		this.shootingStar = shootingStar;
		this.meteorShower = meteorShower;
		
		this.dayMaxBrightness = dayMaxBrightness;
		
		this.dayMinVisibleSize = dayMinVisibleSize;
		this.dayMaxVisibleSize = dayMaxVisibleSize;
		this.dayVisibleSizeRange = dayMaxVisibleSize - dayMinVisibleSize;
		
		this.createHorizon = createHorizon;
		this.createVoid = createVoid;
		
		if(createHorizon)
			skyBuffer = StellarViewSkyEffects.createLightSky();
		if(createVoid)
			darkBuffer = StellarViewSkyEffects.createDarkSky();
		
		this.starsAlwaysVisible = starsAlwaysVisible;
		this.zRotationMultiplier = zRotationMultiplier;
	}
	
	public boolean setViewCenterObject(HashMap<ResourceLocation, SpaceObject> spaceObjects)
	{
		if(viewCenterKey != null)
		{
			if(spaceObjects.containsKey(viewCenterKey.location()))
			{
				viewCenterObject = spaceObjects.get(viewCenterKey.location());
				return true;
			}
			
			StellarView.LOGGER.error("Failed to register View Center because view center object " + viewCenterKey.location() + " could not be found");
			return false;
		}
		
		return true;
	}
	
	public AxisRotation getObjectAxisRotation()
	{
		if(viewCenterObject != null)
			return viewCenterObject.getAxisRotation();
		
		return new AxisRotation();
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
	
	public long getRotationPeriod()
	{
		return rotationPeriod;
	}
	
	public boolean starsAlwaysVisible()
	{
		return starsAlwaysVisible;
	}
	
	public double zRotationMultiplier()
	{
		return zRotationMultiplier;
	}
	
	public double getZRotation(ClientLevel level, Camera camera, float partialTicks)
	{
		double zRotationMultiplier = zRotationMultiplier();
		
		if(zRotationMultiplier == 0)
			return 0;
		
		double zPos = camera.getEntity().getPosition(partialTicks).z();
		
		return 2 * Math.atan(zPos / zRotationMultiplier);
	}
	
	public MeteorEffect.ShootingStar getShootingStar()
	{
		return shootingStar;
	}
	
	public MeteorEffect.MeteorShower getMeteorShower()
	{
		return meteorShower;
	}
	
	public boolean overrideMeteorEffects()
	{
		return false;
	}
	
	public double overrideShootingStarRarity()
	{
		return 10;
	}
	
	public double overrideMeteorShowerRarity()
	{
		return 10;
	}
	
	public boolean objectEquals(SpaceObject spaceObject)
	{
		if(this.viewCenterObject != null)
			return spaceObject == this.viewCenterObject;
		
		return false;
	}
	
	private boolean renderSkybox(ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Tesselator tesselator)
	{
		if(skyboxes == null)
			return false;
		
		for(Skybox skybox : skyboxes)
		{
			skybox.render(level, partialTicks, modelViewMatrix, tesselator);
		}
		
		return true;
	}
	
	private void renderSkyEvents(ClientLevel level, Camera camera, float partialTicks, Matrix4f modelViewMatrix, Tesselator tesselator)
	{
		shootingStar.render(this, level, camera, partialTicks, modelViewMatrix, tesselator);
		meteorShower.render(this, level, camera, partialTicks, modelViewMatrix, tesselator);
	}
	
	private float getTimeOfDay(ClientLevel level, float partialTicks)
	{
		if(rotationPeriod <= 0)
			return 0;
		
		double d0 = Mth.frac((double) (level.getDayTime() % rotationPeriod) / (double) rotationPeriod - 0.25D);
		double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
		
		return (float) (d0 * 2.0D + d1) / 3.0F;
	}
	
	private boolean renderSkyObjectsFrom(ClientLevel level, Camera camera, float partialTicks, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Runnable setupFog, Tesselator tesselator)
	{
		if(viewCenterObject == null)
			return false;
		
		coords = viewCenterObject.getCoords();

		final var transformedModelView = new Matrix4f(modelViewMatrix);
		
		if(!GeneralConfig.disable_view_center_rotation.get())
		{
			double rotation = 2 * Math.PI * getTimeOfDay(level, partialTicks) + Math.PI;
			
			if(viewCenterObject instanceof OrbitingObject orbitingObject && orbitingObject.getOrbitInfo().isPresent())
				rotation -= orbitingObject.getOrbitInfo().get().meanAnomaly(level.getDayTime() % orbitingObject.getOrbitInfo().get().orbitalPeriod().ticks(), partialTicks);

			transformedModelView.rotate(Axis.YP.rotation((float) getAxisRotation().yAxis()));
			transformedModelView.rotate(Axis.ZP.rotation((float) getAxisRotation().zAxis()));
			transformedModelView.rotate(Axis.XP.rotation((float) getAxisRotation().xAxis()));
			
			transformedModelView.rotate(Axis.YP.rotation((float) rotation));
			transformedModelView.rotate(Axis.ZP.rotation((float) getZRotation(level, camera, partialTicks)));
		}
		
		viewCenterObject.renderFrom(this, level, partialTicks, modelViewMatrix, camera, projectionMatrix, StellarViewFogEffects.isFoggy(minecraft, camera), setupFog, tesselator);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		renderSkyEvents(level, camera, partialTicks, modelViewMatrix, tesselator);
		return true;
	}
	
	public void renderSkyObjects(SpaceObject masterParent, ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator)
	{
		Space.render(this, masterParent, level, camera, partialTicks, modelViewMatrix, projectionMatrix, isFoggy, setupFog, tesselator);
	}
	
	public boolean renderSky(ClientLevel level, int ticks, float partialTicks, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(viewCenterObject == null && skyboxes == null)
			return false;
		
		setupFog.run();
		
		if(!StellarViewFogEffects.isFoggy(this.minecraft, camera))
		{
			//RenderSystem.disableTexture();
			Vec3 skyColor = level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTicks);
			float skyX = (float)skyColor.x;
	        float skyY = (float)skyColor.y;
	        float skyZ = (float)skyColor.z;
	        FogRenderer.levelFogColor();
			Tesselator tesselator = Tesselator.getInstance();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
			ShaderInstance shaderinstance = RenderSystem.getShader();
			
			if(createHorizon)
			{
				this.skyBuffer.bind();
				this.skyBuffer.drawWithShader(modelViewMatrix, projectionMatrix, shaderinstance);
			}
			
			VertexBuffer.unbind();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			StellarViewSkyEffects.renderSunrise(level, partialTicks, modelViewMatrix, projectionMatrix, tesselator);
			
			//RenderSystem.enableTexture();

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			
			renderSkybox(level, partialTicks, modelViewMatrix, tesselator);
			
			RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F); // Added this here
			renderSkyObjectsFrom(level, camera, partialTicks, modelViewMatrix, projectionMatrix, setupFog, tesselator);
	        
	        //RenderSystem.disableTexture();
	        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderSystem.disableBlend();
	        
	        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
	        
	        if(createVoid)
	        {
	        	double height = this.minecraft.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
		        if(height < 0.0D)
		        {
					final var transformedModelView = new Matrix4f(modelViewMatrix);
					transformedModelView.translate(0.0F, 12.0F, 0.0F);
		        	this.darkBuffer.bind();
		        	this.darkBuffer.drawWithShader(transformedModelView, projectionMatrix, shaderinstance);
		        	VertexBuffer.unbind();
		        }
	        }
	        
	        if(level.effects().hasGround())
	        	RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
	        else
	        	RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
	        
	        //RenderSystem.enableTexture();
	        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderSystem.depthMask(true);
		}
		
		return true;
	}
}
