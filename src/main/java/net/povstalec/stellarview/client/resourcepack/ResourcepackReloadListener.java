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
import net.povstalec.stellarview.client.render.level.StellarViewEndEffects;
import net.povstalec.stellarview.client.render.level.StellarViewNetherEffects;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.client.resourcepack.objects.BlackHole;
import net.povstalec.stellarview.client.resourcepack.objects.Moon;
import net.povstalec.stellarview.client.resourcepack.objects.Nebula;
import net.povstalec.stellarview.client.resourcepack.objects.Planet;
import net.povstalec.stellarview.client.resourcepack.objects.SpaceObject;
import net.povstalec.stellarview.client.resourcepack.objects.Star;
import net.povstalec.stellarview.client.resourcepack.objects.StarField;
import net.povstalec.stellarview.client.resourcepack.objects.distinct.Sol;

public class ResourcepackReloadListener
{
	public static final String PATH = "stellarview";
	
	public static final String VIEW_CENTERS = "view_centers";
	public static final String CELESTIALS = "celestials";

	public static final String PLANET = "planet";
	public static final String MOON = "moon";
	
	public static final String STAR = "star";
	public static final String BLACK_HOLE = "black_hole";

	public static final String STAR_FIELD = "star_field";

	public static final String NEBULA = "nebula";
	
	private static final ResourceLocation SOL_LOCATION = new ResourceLocation(StellarView.MODID, "star/milky_way/sol");
	
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
					
					else if(canShortenPath(location, MOON))
						addMoon(spaceObjects, location, element);
					
					else if(canShortenPath(location, STAR))
						addStar(spaceObjects, location, element);
					
					else if(canShortenPath(location, STAR_FIELD))
						addStarField(spaceObjects, location, element);
					
					else if(canShortenPath(location, BLACK_HOLE))
						addBlackHole(spaceObjects, location, element);
					
					else if(canShortenPath(location, NEBULA))
						addNebula(spaceObjects, location, element);
				}
			}

			setSpaceObjects(spaceObjects);
			Space.setupSynodicOrbits();
			setViewCenters(spaceObjects, viewCenters);
		}
		
		private static void addViewCenter(HashMap<ResourceLocation, ViewCenter> viewCenters, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "view_center");
				ViewCenter viewCenter;
				
				if(StellarViewOverworldEffects.OVERWORLD_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.Overworld.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse View Center", msg));
				else if(StellarViewNetherEffects.NETHER_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.Nether.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse View Center", msg));
				else if(StellarViewEndEffects.END_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.End.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse View Center", msg));
				else
					viewCenter = ViewCenter.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse View Center", msg));
				
				viewCenters.put(location, viewCenter);
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
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
				
				if(SOL_LOCATION.equals(location))
				{
					JsonObject json = GsonHelper.convertToJsonObject(element, "star");
					Sol sol = Sol.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star", msg));
					Space.addSol(sol);
					
					spaceObjects.put(location, sol);
				}
				else
				{
					JsonObject json = GsonHelper.convertToJsonObject(element, "star");
					Star star = Star.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star", msg));
					
					spaceObjects.put(location, star);
				}
				
				StellarView.LOGGER.debug("Parsed " + location.toString() + " as Star");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void addBlackHole(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "black_hole");
				BlackHole blackHole = BlackHole.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star", msg));
				
				spaceObjects.put(location, blackHole);
				StellarView.LOGGER.debug("Parsed " + location.toString() + " as Black Hole");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void addPlanet(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "planet");
				Planet planet = Planet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Planet", msg));

				spaceObjects.put(location, planet);
				StellarView.LOGGER.debug("Parsed " + location.toString() + " as Planet");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void addMoon(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "moon");
				Moon moon = Moon.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Moon", msg));

				spaceObjects.put(location, moon);
				StellarView.LOGGER.debug("Parsed " + location.toString() + " as Moon");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void addStarField(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "star_field");
				StarField starField = StarField.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star Field", msg));

				spaceObjects.put(location, starField);
				Space.addStarField(starField);
				StellarView.LOGGER.debug("Parsed " + location.toString() + " as Star Field");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void addNebula(HashMap<ResourceLocation, SpaceObject> spaceObjects, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "nebula");
				Nebula nebula = Nebula.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Nebula", msg));

				spaceObjects.put(location, nebula);
				StellarView.LOGGER.debug("Parsed " + location.toString() + " as Nebula");
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void setSpaceObjects(HashMap<ResourceLocation, SpaceObject> spaceObjects)
		{
			for(Map.Entry<ResourceLocation, SpaceObject> spaceObjectEntry : spaceObjects.entrySet())
			{
				SpaceObject spaceObject = spaceObjectEntry.getValue();

				// Set name
				spaceObject.setResourceLocation(spaceObjectEntry.getKey());
				
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
					
					if(!spaceObject.getParent().isPresent())
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
