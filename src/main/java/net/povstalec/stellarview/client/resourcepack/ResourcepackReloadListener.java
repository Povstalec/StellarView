package net.povstalec.stellarview.client.resourcepack;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.stellarview.StellarView;

public class ResourcepackReloadListener
{
	public static final String PATH = "stellarview";
	
	public static final String VIEW_CENTERS = "view_centers";
	public static final String CELESTIALS = "celestials";

	public static final String PLANET = "planet";
	public static final String STAR = "star";
	public static final String SUPERNOVA = "star/supernova";
	
	public static final String GLOBULAR_CLUSTER = "star_field/globular_cluster";
	public static final String SPIRAL_GALAXY = "star_field/spiral_galaxy";
	public static final String ELLIPTICAL_GALAXY = "star_field/elliptical_galaxy";
	
	@Mod.EventBusSubscriber(modid = StellarView.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ReloadListener extends SimpleJsonResourceReloadListener
	{
		public ReloadListener()
		{
			super(new GsonBuilder().create(), PATH);
		}
		
		@Override
		protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler)
		{
    		Space.clear();
    		ViewCenters.clear();
    		
			HashMap<ResourceLocation, ViewCenter> viewCenters = new HashMap<>();
			HashMap<ResourceLocation, SpaceObject> spaceObjects = new HashMap<>();
    		
			for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : jsonMap.entrySet())
			{
				ResourceLocation location = jsonEntry.getKey();
				JsonElement element = jsonEntry.getValue();
				
				if(canShortenPath(location, VIEW_CENTERS))
					addViewCenter(viewCenters, shortenPath(location, VIEW_CENTERS), element);
				else if(canShortenPath(location, CELESTIALS))
				{
					location = shortenPath(location, CELESTIALS);
					
					if(canShortenPath(location, PLANET))
						addPlanet(spaceObjects, location, element);
					
					else if(canShortenPath(location, SUPERNOVA))
						addSupernova(spaceObjects, location, element);
					
					else if(canShortenPath(location, STAR))
						addStar(spaceObjects, location, element);
					
					else if(canShortenPath(location, GLOBULAR_CLUSTER))
						addGlobularCluster(spaceObjects, location, element);
					
					else if(canShortenPath(location, SPIRAL_GALAXY))
						addSpiralGalaxy(spaceObjects, location, element);
					
					else if(canShortenPath(location, ELLIPTICAL_GALAXY))
						addEllipticalGalaxy(spaceObjects, location, element);
				}
			}

			setSpaceObjects(spaceObjects);
			setViewCenters(spaceObjects, viewCenters);
		}
		
		private static void addViewCenter(HashMap<ResourceLocation, ViewCenter> viewCenters, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "view_center");
				ViewCenter viewCenter = ViewCenter.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse View Center", msg));
				
				viewCenters.put(location, viewCenter);
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void setViewCenters(HashMap<ResourceLocation, SpaceObject> spaceObjects, HashMap<ResourceLocation, ViewCenter> viewCenters)
		{
			for(Map.Entry<ResourceLocation, ViewCenter> viewCenterEntry : viewCenters.entrySet())
			{
				// Set the View Center's Space Object if it exists, if it doesn't don't add it to View Center Map
				if(viewCenterEntry.getValue().setViewCenterObject(spaceObjects))
					ViewCenters.addViewCenter(viewCenterEntry.getKey(), viewCenterEntry.getValue());
			}
		}
		
		private static void addStar(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "star");
				Star star = Star.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star", msg));
				
				spaceObjects.put(location, star);
				StellarView.LOGGER.error("Parsed " + location.toString() + " as Star");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void addSupernova(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "supernova");
				Supernova supernova = Supernova.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Supernova", msg));
				
				spaceObjects.put(location, supernova);
				StellarView.LOGGER.error("Parsed " + location.toString() + " as Supernova");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void addPlanet(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "planet");
				Planet planet = Planet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Planet", msg));

				spaceObjects.put(location, planet);
				StellarView.LOGGER.error("Parsed " + location.toString() + " as Planet");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void addGlobularCluster(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "globular_cluster");
				StarField.GlobularCluster globularCluster = StarField.GlobularCluster.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Globular Cluster", msg));

				spaceObjects.put(location, globularCluster);
				Space.addStarField(globularCluster);
				StellarView.LOGGER.error("Parsed " + location.toString() + " as Globular Cluster");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void addSpiralGalaxy(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "spiral_galaxy");
				Galaxy.SpiralGalaxy spiralGalaxy = Galaxy.SpiralGalaxy.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Spiral Galaxy", msg));

				spaceObjects.put(location, spiralGalaxy);
				Space.addStarField(spiralGalaxy);
				StellarView.LOGGER.error("Parsed " + location.toString() + " as Spiral Galaxy");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void addEllipticalGalaxy(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "elliptical_galaxy");
				Galaxy.EllipticalGalaxy ellipticalGalaxy = Galaxy.EllipticalGalaxy.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Elliptical Galaxy", msg));

				spaceObjects.put(location, ellipticalGalaxy);
				Space.addStarField(ellipticalGalaxy);
				StellarView.LOGGER.error("Parsed " + location.toString() + " as Elliptical Galaxy");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString());
			}
		}
		
		private static void setSpaceObjects(HashMap<ResourceLocation, SpaceObject> spaceObjects)
		{
			for(Map.Entry<ResourceLocation, SpaceObject> spaceObjectEntry : spaceObjects.entrySet())
			{
				SpaceObject spaceObject = spaceObjectEntry.getValue();

				// Set name
				spaceObject.location = spaceObjectEntry.getKey();
				
				// Handle parents
				if(spaceObject.getParentKey().isPresent())
				{
					for(Map.Entry<ResourceLocation, SpaceObject> parentEntry : spaceObjects.entrySet())
					{
						if(parentEntry.getKey().equals(spaceObject.getParentKey().get().location()))
						{
							parentEntry.getValue().addChild(spaceObject);
							break;
						}
					}
					
					if(spaceObject.parent == null)
						StellarView.LOGGER.error("Failed to find parent for " + spaceObject.toString());
				}
				else
					Space.addSpaceObject(spaceObjectEntry.getValue());
			}
		}
		
		
		
		public static void registerReloadListener(RegisterClientReloadListenersEvent event)
		{
			event.registerReloadListener(new ReloadListener());
		}
		
		private static boolean canShortenPath(ResourceLocation location, String shortenBy)
		{
			return location.getPath().startsWith(shortenBy + "/") && location.getPath().length() > shortenBy.length(); // If it starts with the string and isn't empty after getting shortened
		}
		
		private static ResourceLocation shortenPath(ResourceLocation location, String shortenBy)
		{
			return location.withPath(location.getPath().substring(shortenBy.length() + 1)); // Magical 1 because there's also the / symbol
		}
	}
}
