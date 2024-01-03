package net.povstalec.stellarview;

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
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.client.screens.config.ConfigScreen;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.KeyBindings;

@Mod(StellarView.MODID)
public class StellarView
{
	public static final String MODID = "stellarview";
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static StellarViewOverworldEffects overworld;

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
    		
        	event.register(StellarViewOverworldEffects.OVERWORLD_EFFECTS, overworld);
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
    
    public static void updateGalaxies()
    {
    	overworld.setStarFieldOffset(OverworldConfig.milky_way_x.get(), OverworldConfig.milky_way_y.get(), OverworldConfig.milky_way_z.get());
    	overworld.setSkyRotation((float) Math.toRadians(OverworldConfig.milky_way_alpha.get()), (float) Math.toRadians(OverworldConfig.milky_way_beta.get()), (float) Math.toRadians(OverworldConfig.milky_way_gamma.get()));
    }
}
