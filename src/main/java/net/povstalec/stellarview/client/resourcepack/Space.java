package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.povstalec.stellarview.client.resourcepack.objects.*;
import net.povstalec.stellarview.common.config.GeneralConfig;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.objects.distinct.Sol;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public final class Space
{
	private static final List<SpaceObject> SPACE_OBJECTS = new ArrayList<SpaceObject>();
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private static final List<StarField> STAR_FIELDS = new ArrayList<StarField>();
	private static final List<GravityLense> GRAVITY_LENSES = new ArrayList<GravityLense>();
	
	public static final Matrix3f IDENTITY_MATRIX = new Matrix3f();
	
	public static Matrix3f lensingMatrix = IDENTITY_MATRIX;
	public static Matrix3f lensingMatrixInv = IDENTITY_MATRIX;
	public static float lensingIntensity = 0;
	
	@Nullable
	private static Sol sol = null;
	@Nullable
	private static SpaceCoords solCoords = null;
	@Nullable
	private static AxisRotation solAxisRotation = null;
	
	
	
	public static void clear()
	{
		sol = null;
		solCoords = null;
		solAxisRotation = null;
		
		SPACE_OBJECTS.clear();
		STAR_FIELDS.clear();
		GRAVITY_LENSES.clear();
	}
	
	public static void addSpaceObject(SpaceObject spaceObject)
	{
		SPACE_OBJECTS.add(spaceObject);
	}
	
	public static void setupSynodicOrbits()
	{
		for(SpaceObject spaceObject : SPACE_OBJECTS)
		{
			if(spaceObject instanceof OrbitingObject orbitingObject)
				orbitingObject.setupSynodicOrbit(null);
		}
	}
	
	public static void render(ViewCenter viewCenter, SpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		setBestLensing();
		
		if(GeneralConfig.dust_clouds.get())
		{
			float dustCloudBrightness = StarField.dustCloudBrightness(viewCenter, level, camera, partialTicks);
			for(StarField starField : STAR_FIELDS)
			{
				starField.renderDustClouds(viewCenter, level, partialTicks, stack, camera, projectionMatrix, setupFog, dustCloudBrightness);
			}
		}
		
		for(SpaceObject spaceObject : SPACE_OBJECTS)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation(0, 0, 0));
		}
		
		masterParent.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation(0, 0, 0));
	}
	
	
	
	private static void setBestLensing()
	{
		lensingMatrix = IDENTITY_MATRIX;
		lensingMatrixInv = IDENTITY_MATRIX;
		lensingIntensity = 0;
		
		for(GravityLense gravityLense : GRAVITY_LENSES)
		{
			if(gravityLense.getLensingIntensity() > lensingIntensity)
				gravityLense.setupLensing();
		}
	}
	
	
	
	public static void addStarField(StarField starField)
	{
		STAR_FIELDS.add(starField);
	}
	
	public static void resetStarFields()
	{
		for(StarField starField : STAR_FIELDS)
		{
			starField.reset();
		}
	}
	
	
	
	public static void addGravityLense(GravityLense gravityLense)
	{
		GRAVITY_LENSES.add(gravityLense);
	}
	
	
	
	public static void addSol(Sol solStar)
	{
		if(sol != null)
		{
			StellarView.LOGGER.error("Could not set Sol as a distinct Space Object because it has already been set");
			return;
		}

		StellarView.LOGGER.debug("Setting Sol as a distinct Space Object");
		
		sol = solStar;
		solCoords = solStar.getCoords().copy();
		solAxisRotation = solStar.getAxisRotation().copy();
		
		updateSol();
	}
	
	public static void updateSol()
	{
		if(sol == null)
			return;
		
		if(OverworldConfig.config_priority.get())
    	{
			SpaceCoords coords = solCoords.copy().add(OverworldConfig.sol_x_offset.get() * 1000, OverworldConfig.sol_y_offset.get() * 1000, OverworldConfig.sol_z_offset.get() * 1000);
    		AxisRotation axisRotation = solAxisRotation.copy().add(new AxisRotation(OverworldConfig.sol_x_rotation.get(), OverworldConfig.sol_y_rotation.get(), OverworldConfig.sol_z_rotation.get()));
    		
    		sol.setPosAndRotation(coords, axisRotation);
    	}
		else
			sol.setPosAndRotation(solCoords.copy(), solAxisRotation.copy());
	}
}
