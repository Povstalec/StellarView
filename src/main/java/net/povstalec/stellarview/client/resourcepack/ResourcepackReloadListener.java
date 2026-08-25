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
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.client.events.StellarViewEvents;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.distinct.Luna;
import net.povstalec.stellarview.api.common.space_objects.distinct.Sol;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.*;
import net.povstalec.stellarview.client.SpaceObjectRenderers;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.StellarViewEffects;
import net.povstalec.stellarview.client.render.ViewCenters;
import net.povstalec.stellarview.client.render.level.StellarViewEndEffects;
import net.povstalec.stellarview.client.render.level.StellarViewNetherEffects;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.effects.MeteorEffect;
import net.povstalec.stellarview.common.util.DustCloudInfo;
import net.povstalec.stellarview.common.util.StarInfo;

import java.util.HashMap;
import java.util.Map;

public class ResourcepackReloadListener
{
	public static final ResourceLocation REMOVE = StellarView.stellarViewLocation("remove");
	
	public static final String PATH = StellarView.MODID;
	
	public static final String VIEW_CENTERS = "view_centers";
	
	public static final String CELESTIALS = "celestials";

	public static final String PLANET = "planet";
	public static final String MOON = "moon";
	public static final String STAR = "star";
	public static final String BLACK_HOLE = "black_hole";
	public static final String STAR_FIELD = "star_field";
	public static final String CONSTELLATION = "constellation";
	public static final String NEBULA = "nebula";
	
	public static final String EFFECTS = "effects";
	public static final String STAR_INFO = "star_info";
	public static final String DUST_CLOUD_INFO = "dust_cloud_info";
	
	private static final ResourceLocation MILKY_WAY_LOCATION = StellarView.stellarViewLocation("star_field/milky_way/milky_way");
	private static final ResourceLocation SOL_LOCATION = StellarView.stellarViewLocation("star/milky_way/sol");
	private static final ResourceLocation LUNA_LOCATION = StellarView.stellarViewLocation("moon/milky_way/sol/earth/luna");
	
	private static HashMap<ResourceLocation, ViewCenter> viewCenters;
	private static HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects;
	
	private static HashMap<ResourceLocation, StarInfo> starTypes;
	private static HashMap<ResourceLocation, DustCloudInfo> dustCloudTypes;
	
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
			
			viewCenters = new HashMap<>();
			spaceObjects = new HashMap<>();
			
			starTypes = new HashMap<>();
			dustCloudTypes = new HashMap<>();
    		for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : jsonMap.entrySet())
			{
				ResourceLocation location = jsonEntry.getKey();
				JsonElement element = jsonEntry.getValue();
				
				// Add View Center
				if(canShortenPath(location, VIEW_CENTERS))
					addViewCenter(viewCenters, shortenPath(location, VIEW_CENTERS), element);
				else if(canShortenPath(location, EFFECTS))
				{
					location = shortenPath(location, EFFECTS);
					
					if(canShortenPath(location, STAR_INFO))
						addStarType(starTypes, shortenPath(location, STAR_INFO), element);
					
					else if(canShortenPath(location, DUST_CLOUD_INFO))
						addDustCloudType(dustCloudTypes, shortenPath(location, DUST_CLOUD_INFO), element);
				}
				else if(canShortenPath(location, CELESTIALS))
				{
					SpaceObject spaceObject = null;
					location = shortenPath(location, CELESTIALS);
					
					if(canShortenPath(location, PLANET))
						spaceObject = makePlanet(location, element);
					
					else if(canShortenPath(location, MOON))
						spaceObject = makeMoon(location, element);
					
					else if(canShortenPath(location, STAR))
						spaceObject = makeStar(location, element);
					
					else if(canShortenPath(location, STAR_FIELD))
						spaceObject = makeStarField(location, element);
					
					else if(canShortenPath(location, CONSTELLATION))
						spaceObject = makeConstellation(location, element);
					
					else if(canShortenPath(location, BLACK_HOLE))
						spaceObject = makeBlackHole(location, element);
					
					else if(canShortenPath(location, NEBULA))
						spaceObject = makeNebula(location, element);
					
					if(spaceObject != null && !shouldIgnore(spaceObject))
					{
						SpaceObjectRenderer<?> renderer = SpaceObjectRenderers.constructObjectRenderer(spaceObject);
						
						if(renderer != null)
							spaceObjects.put(location, renderer);
					}
				}
			}
			
			if(!StellarViewEvents.onEffectsReload(starTypes, dustCloudTypes))
			{
				StellarViewEffects.reset();
				StellarViewEffects.setupEffects(starTypes, dustCloudTypes);
			}
			
			if(!StellarViewEvents.onSpaceRendererReload(spaceObjects))
			{
				SpaceRenderer.clear();
				setSpaceObjects(spaceObjects);
				SpaceRenderer.setupSynodicOrbits();
			}
			
			if(!StellarViewEvents.onViewCenterReload(spaceObjects, viewCenters))
			{
				ViewCenters.clear();
				setViewCenters(spaceObjects, viewCenters);
			}
		}
		
		//============================================================================================
		//****************************************View Centers****************************************
		//============================================================================================
		
		public static void addViewCenter(HashMap<ResourceLocation, ViewCenter> viewCenters, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "view_center");
				ViewCenter viewCenter;
				
				if(StellarViewOverworldEffects.OVERWORLD_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.Overworld.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Overworld View Center {}", msg));
				else if(StellarViewNetherEffects.NETHER_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.Nether.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Nether View Center {}", msg));
				else if(StellarViewEndEffects.END_EFFECTS.equals(location))
					viewCenter = DefaultViewCenters.End.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse End View Center {}", msg));
				else
					viewCenter = ViewCenter.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse View Center {}", msg));
				
				viewCenters.put(location, viewCenter);
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
		}
		
		public static void setViewCenters(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects, HashMap<ResourceLocation, ViewCenter> viewCenters)
		{
			for(Map.Entry<ResourceLocation, ViewCenter> viewCenterEntry : viewCenters.entrySet())
			{
				// Set the View Center's Space Object if it exists, if it doesn't, don't add it to View Center Map
				if(viewCenterEntry.getValue().setViewObjectRenderer(spaceObjects))
					ViewCenters.addViewCenter(viewCenterEntry.getKey(), viewCenterEntry.getValue());
			}
		}
		
		//============================================================================================
		//******************************************Effects*******************************************
		//============================================================================================
		
		public static void addStarType(HashMap<ResourceLocation, StarInfo> starTypes, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "star_info");
				StarInfo starInfo;
				
				starInfo = StarInfo.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star Info {}", msg));
				
				starTypes.put(location, starInfo);
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
		}
		
		public static void addDustCloudType(HashMap<ResourceLocation, DustCloudInfo> dustCloudTypes, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "dust_cloud_info");
				DustCloudInfo dustCloudInfo;
				
				dustCloudInfo = DustCloudInfo.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Dust Cloud Info {}", msg));
				
				dustCloudTypes.put(location, dustCloudInfo);
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
		}
		
		public static void addMeteorType(HashMap<ResourceLocation, MeteorEffect.MeteorType> meteorTypes, ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "meteor_type");
				MeteorEffect.MeteorType meteorType;
				
				meteorType = MeteorEffect.MeteorType.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Meteor Type {}", msg));
				
				meteorTypes.put(location, meteorType);
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
		}
		
		//============================================================================================
		//*****************************************Celestials*****************************************
		//============================================================================================
		
		public static boolean shouldIgnore(SpaceObject spaceObject)
		{
			// Don't add Space Objects which are marked as removed
			if(spaceObject.getParentLocation() != null)
				return REMOVE.equals(spaceObject.getParentLocation());
			
			return false;
		}
		
		public static Star makeStar(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "star");
				if(SOL_LOCATION.equals(location))
					return Sol.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Sol {}", msg));
				else
					return Star.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static BlackHole makeBlackHole(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "black_hole");
				
				return BlackHole.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Black Hole {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static Planet makePlanet(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "planet");
				
				return Planet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Planet {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static Moon makeMoon(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "moon");
				if(LUNA_LOCATION.equals(location))
					return Luna.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Luna {}", msg));
				else
					return Moon.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Moon {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static StarField makeStarField(ResourceLocation location, JsonElement element)
		{
			try
			{
				
				JsonObject json = GsonHelper.convertToJsonObject(element, "star_field");
				
				return StarField.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Star Field {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static Constellation makeConstellation(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "constellation");
				
				return Constellation.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Constellation {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static Nebula makeNebula(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, "nebula");
				
				return Nebula.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> StellarView.LOGGER.error("Failed to parse Nebula {}", msg));
			}
			catch(Exception e)
			{
				StellarView.LOGGER.error("Could not load {}", location.toString(), e);
			}
			
			return null;
		}
		
		public static void setSpaceObjects(HashMap<ResourceLocation, SpaceObjectRenderer<?>> spaceObjects)
		{
			for(Map.Entry<ResourceLocation, SpaceObjectRenderer<?>> spaceObjectEntry : spaceObjects.entrySet())
			{
				SpaceObjectRenderer<?> spaceObject = spaceObjectEntry.getValue();
				
				if(spaceObject.renderedObject() instanceof Sol sol)
					SpaceRenderer.addSol(sol);
				
				// Assign parents
				if(spaceObject.renderedObject().getParentLocation() != null)
				{
					for(Map.Entry<ResourceLocation, SpaceObjectRenderer<?>> parentEntry : spaceObjects.entrySet())
					{
						if(parentEntry.getKey().equals(spaceObject.renderedObject().getParentLocation()))
						{
							parentEntry.getValue().addChild(spaceObject);
							break;
						}
					}
					
					if(spaceObject.renderedObject().getParent().isEmpty())
						StellarView.LOGGER.error("Failed to find parent for {}", spaceObject);
				}
				else
					SpaceRenderer.addSpaceObjectRenderer(spaceObjectEntry.getValue());
			}
			
			// Setup objects
			for(Map.Entry<ResourceLocation, SpaceObjectRenderer<?>> spaceObjectEntry : spaceObjects.entrySet())
			{
				spaceObjectEntry.getValue().setupSpaceObject(spaceObjectEntry.getKey(), null);
			}
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
