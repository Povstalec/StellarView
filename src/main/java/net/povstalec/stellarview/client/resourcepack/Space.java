package net.povstalec.stellarview.client.resourcepack;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.objects.StarField;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;

public final class Space
{
	private static final List<SpaceObject> SPACE_OBJECTS = new ArrayList<SpaceObject>();
	private static final Vector3f NULL_VECTOR = new Vector3f();
	
	private static final List<StarField> STAR_FIELDS = new ArrayList<StarField>();
	
	
	
	public static void clear()
	{
		SPACE_OBJECTS.clear();
		STAR_FIELDS.clear();
	}
	
	public static void addSpaceObject(SpaceObject spaceObject)
	{
		SPACE_OBJECTS.add(spaceObject);
	}
	
	public static void render(ViewCenter viewCenter, SpaceObject masterParent, ClientLevel level, Camera camera, float partialTicks, PoseStack stack, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog, BufferBuilder bufferbuilder)
	{
		for(SpaceObject spaceObject : SPACE_OBJECTS)
		{
			if(spaceObject != masterParent) // Makes sure the master parent (usually galaxy) is rendered last, that way stars from other galaxies don't get rendered over planets
				spaceObject.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation(0, 0, 0));
		}
		
		masterParent.render(viewCenter, level, partialTicks, stack, camera, projectionMatrix, isFoggy, setupFog, bufferbuilder, NULL_VECTOR, new AxisRotation(0, 0, 0));
	}
	
	
	
	public static void addStarField(StarField starField)
	{
		STAR_FIELDS.add(starField);
	}
	
	public static void updateStarFields(SpaceCoords coords)
	{
		for(StarField starField : STAR_FIELDS)
		{
			starField.setStarBuffer(coords);
		}
	}
}
