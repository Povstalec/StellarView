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
import net.povstalec.stellarview.client.render.level.StellarViewAlphaEffects;
import net.povstalec.stellarview.client.render.level.StellarViewBetaEffects;
import net.povstalec.stellarview.client.render.level.StellarViewGammaEffects;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.client.screens.config.ConfigScreen;
import net.povstalec.stellarview.common.config.AlphaConfig;
import net.povstalec.stellarview.common.config.BetaConfig;
import net.povstalec.stellarview.common.config.GammaConfig;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.KeyBindings;

@Mod(StellarView.MODID)
public class StellarView
{
	public static final String MODID = "stellarview";
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static StellarViewOverworldEffects overworld;
    public static StellarViewAlphaEffects alpha;
    public static StellarViewBetaEffects beta;
    public static StellarViewGammaEffects gamma;

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
    		alpha = new StellarViewAlphaEffects();
    		beta = new StellarViewBetaEffects();
    		gamma = new StellarViewGammaEffects();
    		
        	event.register(StellarViewOverworldEffects.OVERWORLD_EFFECTS, overworld);
        	event.register(StellarViewAlphaEffects.ALPHA_EFFECTS, alpha);
        	event.register(StellarViewBetaEffects.BETA_EFFECTS, beta);
        	event.register(StellarViewGammaEffects.GAMMA_EFFECTS, gamma);
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
    	overworld.milkyWay(OverworldConfig.milky_way_x.get(), OverworldConfig.milky_way_y.get(), OverworldConfig.milky_way_z.get(),
				Math.toRadians(OverworldConfig.milky_way_alpha.get()), Math.toRadians(OverworldConfig.milky_way_beta.get()), Math.toRadians(OverworldConfig.milky_way_gamma.get()));
    	alpha.milkyWay(AlphaConfig.milky_way_x.get(), AlphaConfig.milky_way_y.get(), AlphaConfig.milky_way_z.get(),
				Math.toRadians(AlphaConfig.milky_way_alpha.get()), Math.toRadians(AlphaConfig.milky_way_beta.get()), Math.toRadians(AlphaConfig.milky_way_gamma.get()));
    	beta.milkyWay(BetaConfig.milky_way_x.get(), BetaConfig.milky_way_y.get(), BetaConfig.milky_way_z.get(),
				Math.toRadians(BetaConfig.milky_way_alpha.get()), Math.toRadians(BetaConfig.milky_way_beta.get()), Math.toRadians(BetaConfig.milky_way_gamma.get()));
    	gamma.milkyWay(GammaConfig.milky_way_x.get(), GammaConfig.milky_way_y.get(), GammaConfig.milky_way_z.get(),
				Math.toRadians(GammaConfig.milky_way_alpha.get()), Math.toRadians(GammaConfig.milky_way_beta.get()), Math.toRadians(GammaConfig.milky_way_gamma.get()));
    }
}
