package net.povstalec.stellarview;

import java.util.Optional;
import java.util.function.BiFunction;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.povstalec.stellarview.client.render.level.StellarViewEndEffects;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.client.screens.config.ConfigScreen;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.KeyBindings;

@Mod(StellarView.MODID)
public class StellarView
{
	public static final String MODID = "stellarview";
	
	public static final String ENHANCED_CELESTIALS_MODID = "enhancedcelestials";
    
    private static Optional<Boolean> isEnhancedCelestialsLoaded = Optional.empty();
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static StellarViewOverworldEffects overworld;
    public static StellarViewEndEffects end;

	public StellarView()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, StellarViewConfig.CLIENT_CONFIG, MODID + "-client.toml");
		
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, 
				() -> new ConfigScreenHandler.ConfigScreenFactory(new BiFunction<Minecraft, Screen, Screen>()
				{
					@Override
					public Screen apply(Minecraft mc, Screen screen)
					{
						return new ConfigScreen(screen);
					}
				}));
		
		MinecraftForge.EVENT_BUS.register(this);
	}
    
    @Mod.EventBusSubscriber(modid = StellarView.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
    	@SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event)
        {
    		overworld = new StellarViewOverworldEffects();
    		end = new StellarViewEndEffects();
    		
        	event.register(StellarViewOverworldEffects.OVERWORLD_EFFECTS, overworld);
        	//event.register(StellarViewEndEffects.END_EFFECTS, end);
        }

    	@SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event)
        {
        	event.register(KeyBindings.OPEN_CONFIG_KEY);
        }
    }
    
    @Mod.EventBusSubscriber(modid = StellarView.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents
    {
    	@SubscribeEvent
        public static void playerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event)
        {
    		updateGalaxies();
        }
    }
    
    public static boolean isEnhancedCelestialsLoaded()
    {
    	if(isEnhancedCelestialsLoaded.isEmpty())
    		isEnhancedCelestialsLoaded = Optional.of(ModList.get().isLoaded(ENHANCED_CELESTIALS_MODID));
    	
    	return isEnhancedCelestialsLoaded.get();	
    }
    
    public static void updateGalaxies()
    {
    	overworld.setupGalaxy();
    	end.setupGalaxy();
    }
}
