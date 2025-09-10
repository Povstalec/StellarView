package net.povstalec.stellarview.client.resourcepack;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.client.render.LightEffects;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.render.space_objects.ViewObjectRenderer;
import net.povstalec.stellarview.client.util.InstanceBuffer;
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
import net.povstalec.stellarview.common.config.GeneralConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public class ViewCenter
{
	public static final float DAY_MAX_BRIGHTNESS = 0.25F;
	
	public static final float DAY_MIN_VISIBLE_SIZE = 2.5F;
	public static final float DAY_MAX_VISIBLE_SIZE = 10F;
	
	protected int levelTicks;
	protected boolean updateTicks;
	
	protected long ticks;
	protected long oldTicks;
	protected long dayTicks;
	protected long oldDayTicks;
	
	protected float starBrightness;
	protected float dustCloudBrightness;
	
	@Nullable
	protected ResourceKey<SpaceObject> viewCenterKey;
	@Nullable
	protected ViewObjectRenderer viewObject;
	
	@Nullable
	protected List<Skybox> skyboxes;
	
	protected DayBlending dayBlending;
	protected DayBlending sunDayBlending;
	
	protected Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	protected VertexBuffer skyBuffer;
	@Nullable
	protected VertexBuffer darkBuffer;
	//TODO
	protected InstanceBuffer instanceBuffer;
	
	protected SpaceCoords coords;
	protected AxisRotation axisRotation;
	protected long rotationPeriod;
	
	@Nullable
	protected final MeteorEffect.ShootingStar shootingStar;
	@Nullable
	protected final MeteorEffect.MeteorShower meteorShower;
	
	public final boolean createHorizon;
	public final boolean createVoid;
	
	public final boolean starsAlwaysVisible;
	public final boolean starsIgnoreFog;
	public final boolean starsIgnoreRain;
	public final int zRotationMultiplier;
    
    public static final Codec<ViewCenter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		SpaceObject.RESOURCE_KEY_CODEC.optionalFieldOf("view_center").forGetter(ViewCenter::getViewCenterKey),
			Skybox.CODEC.listOf().optionalFieldOf("skyboxes").forGetter(ViewCenter::getSkyboxes),
			
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(ViewCenter::getAxisRotation),
			Codec.LONG.optionalFieldOf("rotation_period", 0L).forGetter(ViewCenter::getRotationPeriod),
			
			DayBlending.CODEC.optionalFieldOf("day_blending", DayBlending.DAY_BLENDING).forGetter(viewCenter -> viewCenter.dayBlending),
			DayBlending.CODEC.optionalFieldOf("sun_day_blending", DayBlending.SUN_DAY_BLENDING).forGetter(viewCenter -> viewCenter.sunDayBlending),
			
			MeteorEffect.ShootingStar.CODEC.optionalFieldOf("shooting_star").forGetter(viewCenter -> Optional.ofNullable(viewCenter.shootingStar)),
			MeteorEffect.MeteorShower.CODEC.optionalFieldOf("meteor_shower").forGetter(viewCenter -> Optional.ofNullable(viewCenter.meteorShower)),
			
			Codec.BOOL.optionalFieldOf("create_horizon", true).forGetter(viewCenter -> viewCenter.createHorizon),
			Codec.BOOL.optionalFieldOf("create_void", true).forGetter(viewCenter -> viewCenter.createVoid),
			
			Codec.BOOL.optionalFieldOf("stars_always_visible", false).forGetter(viewCenter -> viewCenter.starsAlwaysVisible),
			Codec.BOOL.optionalFieldOf("stars_ignore_fog", false).forGetter(viewCenter -> viewCenter.starsIgnoreFog),
			Codec.BOOL.optionalFieldOf("stars_ignore_rain", false).forGetter(viewCenter -> viewCenter.starsIgnoreRain),
			Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("z_rotation_multiplier", 30000000).forGetter(viewCenter -> viewCenter.zRotationMultiplier)
			).apply(instance, ViewCenter::new));
	
	public ViewCenter(Optional<ResourceKey<SpaceObject>> viewCenterKey, Optional<List<Skybox>> skyboxes, AxisRotation axisRotation,
			long rotationPeriod, DayBlending dayBlending, DayBlending sunDayBlending,
			Optional<MeteorEffect.ShootingStar> shootingStar, Optional<MeteorEffect.MeteorShower> meteorShower,
			boolean createHorizon, boolean createVoid,
			boolean starsAlwaysVisible, boolean starsIgnoreFog, boolean starsIgnoreRain, int zRotationMultiplier)
	{
		this.levelTicks = 0;
		this.updateTicks = false;
		
		this.ticks = 0;
		this.oldDayTicks = 0;
		
		this.starBrightness = 0;
		this.dustCloudBrightness = 0;
		
		if(viewCenterKey.isPresent())
			this.viewCenterKey = viewCenterKey.get();
		
		if(skyboxes.isPresent())
			this.skyboxes = skyboxes.get();
		
		this.axisRotation = axisRotation;
		this.rotationPeriod = rotationPeriod;
		
		this.dayBlending = dayBlending;
		this.sunDayBlending = sunDayBlending;
		
		this.shootingStar = shootingStar.isPresent() ? shootingStar.get() : null;
		this.meteorShower = meteorShower.isPresent() ? meteorShower.get() : null;
		
		this.createHorizon = createHorizon;
		this.createVoid = createVoid;
		
		if(createHorizon)
			skyBuffer = StellarViewSkyEffects.createLightSky();
		if(createVoid)
			darkBuffer = StellarViewSkyEffects.createDarkSky();
		
		this.starsAlwaysVisible = starsAlwaysVisible;
		this.starsIgnoreFog = starsIgnoreFog;
		this.starsIgnoreRain = starsIgnoreRain;
		this.zRotationMultiplier = zRotationMultiplier;
	}
	
	public void setViewObjectRenderer(ViewObjectRenderer<?> object)
	{
		viewObject = object;
	}
	
	public boolean setViewObjectRenderer(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects)
	{
		if(viewCenterKey != null)
		{
			if(spaceObjects.containsKey(viewCenterKey.location()))
			{
				if(spaceObjects.get(viewCenterKey.location()) instanceof ViewObjectRenderer viewObject)
					setViewObjectRenderer(viewObject);
				else
					StellarView.LOGGER.error("Failed to register View Center because " + viewCenterKey.location() + " is not an instance of ViewCenterObject");
				return true;
			}
			
			StellarView.LOGGER.error("Failed to register View Center because view center object " + viewCenterKey.location() + " could not be found");
			return false;
		}
		
		return true;
	}
	
	public boolean isStatic()
	{
		return GeneralConfig.static_sky.get();
	}
	
	public long ticks()
	{
		return ticks;
	}
	
	public long tickDifference()
	{
		return ticks - oldTicks;
	}
	
	public long dayTicks()
	{
		return dayTicks;
	}
	
	public long dayTickDifference()
	{
		return dayTicks - oldDayTicks;
	}
	
	public float starBrightness()
	{
		return starBrightness;
	}
	
	public float dustCloudBrightness()
	{
		return dustCloudBrightness;
	}
	
	public AxisRotation getObjectAxisRotation()
	{
		if(viewObject != null)
			return viewObject.axisRotation();
		
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
	
	public DayBlending dayBlending()
	{
		return dayBlending;
	}
	
	public DayBlending sunDayBlending()
	{
		return sunDayBlending;
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
	
	public boolean starsIgnoreFog()
	{
		return starsIgnoreFog;
	}
	
	public boolean starsIgnoreRain()
	{
		return starsIgnoreRain;
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
	
	@Nullable
	public MeteorEffect.ShootingStar getShootingStar()
	{
		return shootingStar;
	}
	
	@Nullable
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
	
	public boolean objectEquals(SpaceObjectRenderer spaceObject)
	{
		if(this.viewObject != null)
			return spaceObject == this.viewObject;
		
		return false;
	}
	
	protected boolean renderSkybox(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		if(skyboxes == null)
			return false;
		
		for(Skybox skybox : skyboxes)
		{
			skybox.render(level, partialTicks, stack, bufferbuilder);
		}
		
		return true;
	}
	
	protected void renderSkyEvents(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		if(shootingStar != null)
			shootingStar.render(this, level, camera, partialTicks, stack, bufferbuilder);
		
		if(meteorShower != null)
			meteorShower.render(this, level, camera, partialTicks, stack, bufferbuilder);
	}
	
	protected float getTimeOfDay(float partialTicks)
	{
		if(rotationPeriod <= 0)
			return 0;
		
		double d0 = Mth.frac((double) (this.oldDayTicks % this.rotationPeriod + dayTickDifference() * partialTicks) / (double) this.rotationPeriod - 0.25D);
		double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
		
		return (float) (d0 * 2.0D + d1) / 3.0F;
	}
	
	protected boolean renderSkyObjectsFrom(ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		if(viewObject == null)
			return false;
		
		coords = viewObject.spaceCoords();
		
		if(updateTicks)
		{
			this.oldTicks = this.ticks;
			this.ticks = GeneralConfig.tick_multiplier.get() * (GeneralConfig.use_game_ticks.get() ? level.getGameTime() : level.getDayTime());
		}
		this.starBrightness = LightEffects.starBrightness(this, level, camera, partialTicks);
		this.dustCloudBrightness = GeneralConfig.dust_clouds.get() ? LightEffects.dustCloudBrightness(this, level, camera, partialTicks) : 0;
		
		stack.pushPose();
		
		//TODO
		// Bind the celestial sphere to a physical location in the world
		stack.translate(-camera.getPosition().x(), -camera.getPosition().y() + 300, -camera.getPosition().z());
		
		if(!GeneralConfig.disable_view_center_rotation.get())
		{
			if(updateTicks)
			{
				this.oldDayTicks = this.dayTicks;
				this.dayTicks = level.getDayTime();
			}
			double rotation = 2 * Math.PI * getTimeOfDay(partialTicks) + Math.PI;
			
			if(viewObject.orbitInfo() != null)
				rotation -= viewObject.orbitInfo().meanAnomaly(this.ticks % viewObject.orbitInfo().orbitalPeriod().ticks(), tickDifference() * partialTicks);
			
			stack.mulPose(Axis.YP.rotation((float) getAxisRotation().yAxis()));
			stack.mulPose(Axis.ZP.rotation((float) getAxisRotation().zAxis()));
			stack.mulPose(Axis.XP.rotation((float) getAxisRotation().xAxis()));
			
			stack.mulPose(Axis.YP.rotation((float) rotation));
			stack.mulPose(Axis.ZP.rotation((float) getZRotation(level, camera, partialTicks)));
		}
		
		viewObject.renderFrom(this, level, tickDifference() * partialTicks, stack, camera, projectionMatrix, StellarViewFogEffects.isFoggy(minecraft, camera), setupFog, bufferbuilder);
		
		//TODO Test Buffer
		if(instanceBuffer == null)
		{
			instanceBuffer = new InstanceBuffer();
			
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			RenderSystem.setShader(GameRenderer::getPositionShader);
			BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = InstanceBuffer.createStarMesh(bufferBuilder);
			
			instanceBuffer.bind();
			instanceBuffer.upload(bufferbuilder$renderedbuffer);
			instanceBuffer.drawWithShader(stack.last().pose(), projectionMatrix, StellarViewShaders.instancedShader());
			InstanceBuffer.unbind();
		}
		else
		{
			instanceBuffer.bind();
			instanceBuffer.drawWithShader(stack.last().pose(), projectionMatrix, StellarViewShaders.instancedShader());
			InstanceBuffer.unbind();
		}
		
		stack.popPose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		renderSkyEvents(level, camera, partialTicks, stack, bufferbuilder);
		return true;
	}
	
	public void renderSkyObjects(SpaceObjectRenderer masterParent, ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		SpaceRenderer.render(this, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
	}
	
	public boolean renderSky(ClientLevel level, int ticks, float partialTicks, PoseStack stack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		minecraft.getProfiler().push(StellarView.MODID);
		
		if(viewObject == null && skyboxes == null)
			return false;
		
		if(this.levelTicks != ticks)
		{
			this.updateTicks = true;
			this.levelTicks = ticks;
		}
		
		setupFog.run();
		
		if(starsIgnoreFog() || !StellarViewFogEffects.isFoggy(this.minecraft, camera))
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
			
			if(createHorizon)
			{
				this.skyBuffer.bind();
				this.skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
			}
			
			VertexBuffer.unbind();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			StellarViewSkyEffects.renderSunrise(level, partialTicks, stack, projectionMatrix, bufferbuilder);
			
			RenderSystem.enableTexture();

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			
			renderSkybox(level, partialTicks, stack, bufferbuilder);
			
			RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F); // Added this here
			renderSkyObjectsFrom(level, camera, partialTicks, stack, projectionMatrix, setupFog, bufferbuilder);
	        
	        RenderSystem.disableTexture();
	        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderSystem.disableBlend();
	        
	        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
	        
	        if(createVoid)
	        {
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
	        }
	        
	        if(level.effects().hasGround())
	        	RenderSystem.setShaderColor(skyX * 0.2F + 0.04F, skyY * 0.2F + 0.04F, skyZ * 0.6F + 0.1F, 1.0F);
	        else
	        	RenderSystem.setShaderColor(skyX, skyY, skyZ, 1.0F);
	        
	        RenderSystem.enableTexture();
	        RenderSystem.depthMask(true);
		}
		
		if(this.updateTicks)
			this.updateTicks = false;
		
		minecraft.getProfiler().pop();
		
		return true;
	}
	
	
	
	public static class DayBlending
	{
		public static final DayBlending DAY_BLENDING = new DayBlending(1, 10, 30);
		public static final DayBlending SUN_DAY_BLENDING = new DayBlending(1, 10, 20);
		
		private final float dayMaxBrightness;
		
		private final float dayMinVisibleSize;
		private final float dayMaxVisibleSize;
		
		public static final Codec<DayBlending> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("max_brightness", DAY_MAX_BRIGHTNESS).forGetter(dayBlending -> dayBlending.dayMinVisibleSize),
				
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("min_visible_size", DAY_MIN_VISIBLE_SIZE).forGetter(dayBlending -> dayBlending.dayMinVisibleSize),
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("max_visible_size", DAY_MAX_VISIBLE_SIZE).forGetter(dayBlending -> dayBlending.dayMaxVisibleSize)
		).apply(instance, DayBlending::new));
		
		public DayBlending(float dayMaxBrightness, float dayMinVisibleSize, final float dayMaxVisibleSize)
		{
			this.dayMaxBrightness = dayMaxBrightness;
			
			this.dayMinVisibleSize = dayMinVisibleSize;
			this.dayMaxVisibleSize = dayMaxVisibleSize;
		}
		
		public float dayMaxBrightness()
		{
			return dayMaxBrightness;
		}
		
		public float dayMaxVisibleSize()
		{
			return dayMaxVisibleSize;
		}
		
		public float dayMinVisibleSize()
		{
			return dayMinVisibleSize;
		}
		
		public float dayVisibleRange()
		{
			return dayMaxVisibleSize - dayMinVisibleSize;
		}
	}
}
