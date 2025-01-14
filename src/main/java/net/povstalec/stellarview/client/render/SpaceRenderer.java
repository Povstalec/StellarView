package net.povstalec.stellarview.client.render;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.povstalec.stellarview.client.resourcepack.ViewCenter;
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

public final class SpaceRenderer
{
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private static final int STAR_LIMIT = 100000;
	
	private static final HashMap<ClientSpaceRegion.RegionPos, ClientSpaceRegion> SPACE_REGIONS = new HashMap<ClientSpaceRegion.RegionPos, ClientSpaceRegion>();
	
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
	
	private static int starsPerTick = 0;
	
	public static boolean loadNewStars()
	{
		return starsPerTick < STAR_LIMIT;
	}
	
	public static void loadedStars(int starCount)
	{
		starsPerTick += starCount;
	}
	
	public static void clear()
	{
		sol = null;
		solCoords = null;
		solAxisRotation = null;
		
		SPACE_REGIONS.clear();
	}
	
	public static void addSpaceRegion(ClientSpaceRegion spaceRegion)
	{
		SPACE_REGIONS.put(spaceRegion.getRegionPos(), spaceRegion);
	}
	
	public static void removeSpaceRegion(ClientSpaceRegion.RegionPos regionPos)
	{
		SPACE_REGIONS.remove(regionPos);
	}
	
	public static void addSpaceObject(SpaceObject spaceObject)
	{
		ClientSpaceRegion region = getOrCreateRegion(spaceObject.getCoords());
		
		region.addChild(spaceObject);
	}
	
	public static void setupSynodicOrbits()
	{
		for(Map.Entry<ClientSpaceRegion.RegionPos, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			spaceRegionEntry.getValue().setupSynodicOrbits();
		}
	}
	
	public static void resetStarFields()
	{
		starsPerTick = 0;
		for(Map.Entry<ClientSpaceRegion.RegionPos, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			spaceRegionEntry.getValue().resetStarFields();
		}
	}
	
	public static void render(ViewCenter viewCenter, SpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		starsPerTick = 0;
		setBestLensing();
		
		ClientSpaceRegion.RegionPos pos = new ClientSpaceRegion.RegionPos(viewCenter.getCoords());
		
		if(viewCenter.dustCloudBrightness() > 0)
		{
			for(Map.Entry<ClientSpaceRegion.RegionPos, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
			{
				if(spaceRegionEntry.getKey().isInRange(pos, getRange()))
					spaceRegionEntry.getValue().renderDustClouds(viewCenter, level, camera, partialTicks, stack, projectionMatrix, setupFog, viewCenter.dustCloudBrightness());
			}
		}
		
		ClientSpaceRegion centerRegion = null;
		for(Map.Entry<ClientSpaceRegion.RegionPos, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			if(!spaceRegionEntry.getKey().equals(pos))
			{
				if(spaceRegionEntry.getKey().isInRange(pos, getRange()))
					spaceRegionEntry.getValue().render(viewCenter, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
			}
			else
				centerRegion = spaceRegionEntry.getValue();
		}
		
		if(centerRegion != null)
			centerRegion.render(viewCenter, masterParent, level, camera, partialTicks, stack, projectionMatrix, isFoggy, setupFog, bufferbuilder);
		
		masterParent.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation());
	}
	
	
	
	private static void setBestLensing()
	{
		lensingMatrix = IDENTITY_MATRIX;
		lensingMatrixInv = IDENTITY_MATRIX;
		lensingIntensity = 0;
		
		for(Map.Entry<ClientSpaceRegion.RegionPos, ClientSpaceRegion> spaceRegionEntry : SPACE_REGIONS.entrySet())
		{
			spaceRegionEntry.getValue().setBestLensing();
		}
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
	
	//============================================================================================
	//****************************************Space Regions***************************************
	//============================================================================================
	
	public static int getRange()
	{
		return GeneralConfig.space_region_render_distance.get();
	}
	
	@Nullable
	public static ClientSpaceRegion getRegion(ClientSpaceRegion.RegionPos pos)
	{
		if(SPACE_REGIONS.containsKey(pos))
			return SPACE_REGIONS.get(pos);
		
		return null;
	}
	
	@Nullable
	public static ClientSpaceRegion getRegion(SpaceCoords coords)
	{
		return getRegion(new ClientSpaceRegion.RegionPos(coords));
	}
	
	public static ClientSpaceRegion getOrCreateRegion(ClientSpaceRegion.RegionPos pos)
	{
		return SPACE_REGIONS.computeIfAbsent(pos, position -> new ClientSpaceRegion(pos));
	}
	
	public static ClientSpaceRegion getOrCreateRegion(SpaceCoords coords)
	{
		return getOrCreateRegion(new ClientSpaceRegion.RegionPos(coords));
	}
}
