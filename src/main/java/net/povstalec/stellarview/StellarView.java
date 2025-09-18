package net.povstalec.stellarview;

import java.util.Optional;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.povstalec.stellarview.api.common.space_objects.distinct.Luna;
import net.povstalec.stellarview.api.common.space_objects.distinct.Sol;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.*;
import net.povstalec.stellarview.client.SpaceObjectRenderers;
import net.povstalec.stellarview.client.render.space_objects.distinct.*;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.*;
import net.povstalec.stellarview.client.screens.config.ConfigScreen;
import net.povstalec.stellarview.compatibility.aether.AetherCompatibility;
import net.povstalec.stellarview.compatibility.twilightforest.TwilightForestCompatibility;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.povstalec.stellarview.client.render.level.StellarViewEndEffects;
import net.povstalec.stellarview.client.render.level.StellarViewNetherEffects;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.client.resourcepack.ResourcepackReloadListener;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.KeyBindings;

@Mod(StellarView.MODID)
public class StellarView
{
	public static final String MODID = "stellarview";
	
	public static final String ENHANCED_CELESTIALS_MODID = "enhancedcelestials";
	public static final String LUNAR_MODID = "lunar";
	public static final String TWILIGHT_FOREST_MODID = "twilightforest";
	public static final String AETHER_MODID = "aether";
    
    private static Optional<Boolean> isEnhancedCelestialsLoaded = Optional.empty();
	private static Optional<Boolean> isLunarLoaded = Optional.empty();
	private static Optional<Boolean> isTwilightForestLoaded = Optional.empty();
	private static Optional<Boolean> isAetherLoaded = Optional.empty();
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static StellarViewOverworldEffects overworld;
    public static StellarViewNetherEffects nether;
    public static StellarViewEndEffects end;
	
	public StellarView(ModContainer modContainer, Dist dist)
	{
		modContainer.registerConfig(ModConfig.Type.CLIENT, StellarViewConfig.CLIENT_CONFIG, MODID + "-client.toml");
		
		if(dist.isClient())
			ConfigScreen.registerConfigScreen(modContainer);
	}

    @EventBusSubscriber(modid = StellarView.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{
			event.enqueueWork(() ->
			{
				SpaceObjectRenderers.register(Planet.class, PlanetRenderer<Planet>::new);
				SpaceObjectRenderers.register(Moon.class, MoonRenderer<Moon>::new);
				SpaceObjectRenderers.register(Luna.class, LunaRenderer::new);
				SpaceObjectRenderers.register(Star.class, StarRenderer<Star>::new);
				SpaceObjectRenderers.register(Sol.class, StarRenderer<Sol>::new);
				SpaceObjectRenderers.register(BlackHole.class, BlackHoleRenderer<BlackHole>::new);
				SpaceObjectRenderers.register(Nebula.class, NebulaRenderer<Nebula>::new);
				SpaceObjectRenderers.register(StarField.class, StarFieldRenderer<StarField>::new);
				SpaceObjectRenderers.register(Constellation.class, ConstellationRenderer<Constellation>::new);
			});
		}
		
    	@SubscribeEvent(priority = EventPriority.LOWEST)
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event)
		{
			overworld = new StellarViewOverworldEffects();
			nether = new StellarViewNetherEffects();
			end = new StellarViewEndEffects();
			
			event.register(StellarViewOverworldEffects.OVERWORLD_EFFECTS, overworld);
			event.register(StellarViewNetherEffects.NETHER_EFFECTS, nether);
			event.register(StellarViewEndEffects.END_EFFECTS, end);
			
			if(isTwilightForestLoaded())
				TwilightForestCompatibility.registerTwilightForestEffects(event);
			
			if(isAetherLoaded())
				AetherCompatibility.registerAetherEffects(event);
        }
    	

    	@SubscribeEvent
        public static void registerClientReloadListener(RegisterClientReloadListenersEvent event)
        {
    		ResourcepackReloadListener.ReloadListener.registerReloadListener(event);
        }

    	@SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event)
        {
        	event.register(KeyBindings.OPEN_CONFIG_KEY);
        }
    }
    
    public static boolean isEnhancedCelestialsLoaded()
    {
    	if(isEnhancedCelestialsLoaded.isEmpty())
    		isEnhancedCelestialsLoaded = Optional.of(ModList.get().isLoaded(ENHANCED_CELESTIALS_MODID));
    	
    	return isEnhancedCelestialsLoaded.get();	
    }

	public static boolean isLunarLoaded()
	{
		if(isLunarLoaded.isEmpty())
			isLunarLoaded = Optional.of(ModList.get().isLoaded(LUNAR_MODID));

		return isLunarLoaded.get();
	}
	
	public static boolean isTwilightForestLoaded()
	{
		if(isTwilightForestLoaded.isEmpty())
			isTwilightForestLoaded = Optional.of(ModList.get().isLoaded(TWILIGHT_FOREST_MODID));
		
		return isTwilightForestLoaded.get();
	}
	
	public static boolean isAetherLoaded()
	{
		if(isAetherLoaded.isEmpty())
			isAetherLoaded = Optional.of(ModList.get().isLoaded(AETHER_MODID));
		
		return isAetherLoaded.get();
	}
}	
