package net.povstalec.stellarview.client.render;

import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;

public final class ViewCenters
{
	private static Minecraft minecraft = Minecraft.getInstance();
	
	private static final HashMap<ResourceLocation, ViewCenter> VIEW_CENTER_MAP = new HashMap<>();
	
	public static void clear()
	{
		VIEW_CENTER_MAP.clear();
	}
	
	public static void addViewCenter(ResourceLocation location, ViewCenter viewCenter)
	{
		if(!VIEW_CENTER_MAP.containsKey(location))
			VIEW_CENTER_MAP.put(location, viewCenter);
		else
			StellarView.LOGGER.error("View Center " + location.toString() + " already exists");
	}
	
	public static boolean isViewCenterPresent(ResourceLocation location)
	{
		return VIEW_CENTER_MAP.containsKey(location);
	}
	
	@Nullable
	public static ViewCenter getViewCenter(ResourceLocation location)
	{
		return VIEW_CENTER_MAP.get(location);
	}
	
	public static boolean renderViewCenterSky(ResourceLocation location, ClientLevel level, int ticks, float partialTicks, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		if(!isViewCenterPresent(location))
			return false; // False because we're not replacing any rendering
		
		return getViewCenter(location).renderSky(level, ticks, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
	}
	
	public static boolean renderViewCenterSky(ClientLevel level, int ticks, float partialTicks, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		return renderViewCenterSky(level.dimension().location(), level, ticks, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
	}
	
	//TODO Maybe more rendering stuff like clouds
}
