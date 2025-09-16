package net.povstalec.stellarview;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.povstalec.stellarview.api.common.space_objects.distinct.Luna;
import net.povstalec.stellarview.api.common.space_objects.distinct.Sol;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.*;
import net.povstalec.stellarview.client.SpaceObjectRenderers;
import net.povstalec.stellarview.client.render.space_objects.distinct.LunaRenderer;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.*;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.config.StellarViewConfigSpec;

@Environment(EnvType.CLIENT)
public class StellarViewClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
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
		
		registerConfig(StellarViewConfig.CLIENT_CONFIG, StellarView.MODID + "-client.toml");
	}
	
	private static void registerConfig(StellarViewConfigSpec config, String fileName)
	{
		config.register(fileName);
	}
}
