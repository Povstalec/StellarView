package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.Tesselator;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.objects.StarField;
import net.povstalec.stellarview.client.resourcepack.objects.distinct.Sol;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public final class Space
{
	private static final List<SpaceObject> SPACE_OBJECTS = new ArrayList<SpaceObject>();
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private static final List<StarField> STAR_FIELDS = new ArrayList<StarField>();
	
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
	}
	
	public static void addSpaceObject(SpaceObject spaceObject)
	{
		SPACE_OBJECTS.add(spaceObject);
	}
	
	public static void render(ViewCenter viewCenter, SpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, Tesselator tesselator)
	{
		for(SpaceObject spaceObject : SPACE_OBJECTS)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, NULL_VECTOR, new AxisRotation(0, 0, 0));
		}
		
		masterParent.render(viewCenter, level, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog, tesselator, NULL_VECTOR, new AxisRotation(0, 0, 0));
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
