package net.povstalec.stellarview.client.resourcepack;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.client.events.StellarViewEvents;
import net.povstalec.stellarview.api.common.space_objects.distinct.Sol;
import net.povstalec.stellarview.client.SpaceObjectRenderers;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.ViewCenters;
import net.povstalec.stellarview.client.render.level.StellarViewEndEffects;
import net.povstalec.stellarview.client.render.level.StellarViewNetherEffects;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.BlackHole;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Moon;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Nebula;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Planet;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Star;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.StarField;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
	
	private static final ResourceLocation SOL_LOCATION = ResourceLocation.fromNamespaceAndPath(StellarView.MODID, "star/milky_way/sol");
	
	private static HashMap<ResourceLocation, ViewCenter> viewCenters = new HashMap<>();
	private static HashMap<ResourceLocation, SpaceObjectRenderer> spaceObjects = new HashMap<>();
	
	@EventBusSubscriber(modid = StellarView.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ReloadListener extends SimpleJsonResourceReloadListener
	{
		public ReloadListener()
		{
			super(new GsonBuilder().create(), PATH);
		}
		
		@Override
		protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler)
		{
			if(StellarViewEvents.onReload(jsonMap, manager, filler))
				return;
			
    		SpaceRenderer.clear();
    		ViewCenters.clear();
    		
			for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : jsonMap.entrySet())
			{
				ResourceLocation location = jsonEntry.getKey();
				JsonElement element = jsonEntry.getValue();
				
				SpaceObject spaceObject = null;
				if(canShortenPath(location, VIEW_CENTERS))
					addViewCenter(viewCenters, shortenPath(location, VIEW_CENTERS), element);
				else if(canShortenPath(location, CELESTIALS))
				{
					location = shortenPath(location, CELESTIALS);
					
					if(canShortenPath(location, PLANET))
						spaceObject = makePlanet(location, element);
					
					else if(canShortenPath(location, MOON))
						spaceObject = makeMoon(location, element);
					
					else if(canShortenPath(location, STAR))
						spaceObject = makeStar(location, element);
					
					else if(canShortenPath(location, STAR_FIELD))
						spaceObject = makeStarField(location, element);
					
					else if(canShortenPath(location, BLACK_HOLE))
						spaceObject = makeBlackHole(location, element);
					
					else if(canShortenPath(location, NEBULA))
						spaceObject = makeNebula(location, element);
				}
				
				if(spaceObject != null)
				{
					SpaceObjectRenderer renderer = SpaceObjectRenderers.constructObjectRenderer(spaceObject);
					
					if(renderer != null)
						spaceObjects.put(location, renderer);
				}
			}

			setSpaceObjects(spaceObjects);
			SpaceRenderer.setupSynodicOrbits();
			setViewCenters(spaceObjects, viewCenters);
		}
		
		private static void addViewCenter(HashMap<ResourceLocation, ViewCenter> viewCenters, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "view_center");
				ViewCenter viewCenter;
				
				if(StellarViewOverworldEffects.OVERWORLD_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.Overworld.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse View Center"));
				else if(StellarViewNetherEffects.NETHER_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.Nether.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse View Center"));
				else if(StellarViewEndEffects.END_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.End.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse View Center"));
				else
					viewCenter = ViewCenter.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse View Center"));
				
				viewCenters.put(location, viewCenter);
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
		}
		
		private static void setViewCenters(HashMap<ResourceLocation, SpaceObjectRenderer> spaceObjects, HashMap<ResourceLocation, ViewCenter> viewCenters)
		{
			for(Map.Entry<ResourceLocation, ViewCenter> viewCenterEntry : viewCenters.entrySet())
			{
				// Set the View Center's Space Object if it exists, if it doesn't don't add it to View Center Map
				if(viewCenterEntry.getValue().setViewObjectRenderer(spaceObjects))
					ViewCenters.addViewCenter(viewCenterEntry.getKey(), viewCenterEntry.getValue());
			}
		}
		
		private static Star makeStar(ResourceLocation location, JsonElement element)
		{
			try
			{
				if(SOL_LOCATION.equals(location))
				{
					JsonObject json = GsonHelper.convertToJsonObject(element, "star");
					Sol sol = Sol.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Sol"));
					SpaceRenderer.addSol(sol);
					
					return sol;
				}
				else
				{
					JsonObject json = GsonHelper.convertToJsonObject(element, "star");
					Star star = Star.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Star"));
					
					return star;
				}
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
			
			return null;
		}
		
		private static BlackHole makeBlackHole(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "black_hole");
				BlackHole blackHole = BlackHole.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Black Hole"));
				
				return blackHole;
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
			
			return null;
		}
		
		private static Planet makePlanet(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "planet");
				Planet planet = Planet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Planet"));

				return planet;
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
			
			return null;
		}
		
		private static Moon makeMoon(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "moon");
				Moon moon = Moon.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Moon"));

				return moon;
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
			
			return null;
		}
		
		private static StarField  makeStarField(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "star_field");
				StarField starField = StarField.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Star Field"));

				return starField;
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
			
			return null;
		}
		
		private static Nebula makeNebula(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "nebula");
				Nebula nebula = Nebula.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(loggedExceptionProvider("Failed to parse Nebula"));
				
				return nebula;
			}
			catch(RuntimeException e)
			{
				StellarView.LOGGER.error("Could not load " + location.toString() + " " + e);
			}
			
			return null;
		}
		
		private static void setSpaceObjects(HashMap<ResourceLocation, SpaceObjectRenderer> spaceObjects)
		{
			for(Map.Entry<ResourceLocation, SpaceObjectRenderer> spaceObjectEntry : spaceObjects.entrySet())
			{
				SpaceObjectRenderer spaceObject = spaceObjectEntry.getValue();

				// Set name
				spaceObject.renderedObject().setResourceLocation(spaceObjectEntry.getKey());
				
				// Handle parents
				if(spaceObject.renderedObject().getParentLocation().isPresent())
				{
					for(Map.Entry<ResourceLocation, SpaceObjectRenderer> parentEntry : spaceObjects.entrySet())
					{
						if(parentEntry.getKey().equals(spaceObject.renderedObject().getParentLocation().get()))
						{
							parentEntry.getValue().addChild(spaceObject);
							break;
						}
					}
					
					if(!spaceObject.renderedObject().getParent().isPresent())
						StellarView.LOGGER.error("Failed to find parent for " + spaceObject.toString());
				}
				else
					SpaceRenderer.addSpaceObjectRenderer(spaceObjectEntry.getValue());
			}
		}
		
		
		@SubscribeEvent
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

	public static Function<String, IllegalStateException> loggedExceptionProvider(String loggedMessage) {
		return (msg) -> {
			final var e = new IllegalStateException(msg);
			StellarView.LOGGER.error("{}", loggedMessage, e);
			return e;
		};
	}
}
