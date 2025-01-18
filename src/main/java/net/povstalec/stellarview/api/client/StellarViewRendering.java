package net.povstalec.stellarview.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.client.render.ViewCenters;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public class StellarViewRendering
{
	/**
	 * Check if a View Center is present
	 *
	 * @param resourceLocation The Resource Location (ID) of the View Center
	 * @return whether or not a View Center corresponding to the Resource Location was found
	 */
	public static boolean isViewCenterPresent(ResourceLocation resourceLocation)
	{
		return ViewCenters.isViewCenterPresent(resourceLocation);
	}
	
	/**
	 * Get a View Center based on its Resource Location
	 *
	 * @param resourceLocation The Resource Location (ID) of the View Center
	 * @return View Center if present, otherwise null
	 */
	@Nullable
	public static ViewCenter getViewCenter(ResourceLocation resourceLocation)
	{
		return ViewCenters.getViewCenter(resourceLocation);
	}
	
	/**
	 * Render the sky as seen from the View Center specified by the Resource Location (ID)
	 *
	 * @param location The Resource Location (ID) of the View Center
	 * @param level ClientLevel used for rendering
	 * @param ticks Current Ticks of the LevelRenderer
	 * @param partialTicks Partial Ticks, what else to say?
	 * @param modelViewMatrix Matrix4f used as a starting point for all of the linear transformations
	 * @param camera Player's Camera
	 * @param projectionMatrix Current projection matrix
	 * @param isFoggy Whether it is foggy or not
	 * @param setupFog Function to set up fog
	 * @return true if the View Center was found and rendered successfully, otherwise false
	 */
	public static boolean renderViewCenterSky(ResourceLocation location, ClientLevel level, int ticks, float partialTicks, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		return ViewCenters.renderViewCenterSky(location, level, ticks, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
	}
	
	/**
	 * Render the sky as seen from the View Center with the same Resource Location (ID) as the Dimension it's being rendered in
	 *
	 * @param level ClientLevel used for rendering
	 * @param ticks Current Ticks of the LevelRenderer
	 * @param partialTicks Partial Ticks, what else to say?
	 * @param modelViewMatrix Matrix4f used as a starting point for all of the linear transformations
	 * @param camera Player's Camera
	 * @param projectionMatrix Current projection matrix
	 * @param isFoggy Whether it is foggy or not
	 * @param setupFog Function to set up fog
	 * @return true if the View Center was found and rendered successfully, otherwise false
	 */
	public static boolean renderViewCenterSky(ClientLevel level, int ticks, float partialTicks, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
	{
		return ViewCenters.renderViewCenterSky(level.dimension().location(), level, ticks, partialTicks, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
	}
}
