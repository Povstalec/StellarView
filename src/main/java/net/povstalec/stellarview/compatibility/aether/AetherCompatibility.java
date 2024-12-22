package net.povstalec.stellarview.compatibility.aether;

import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

public class AetherCompatibility
{
	public static StellarViewAetherEffects aether;
	
	public static void registerAetherEffects(RegisterDimensionSpecialEffectsEvent event)
	{
		aether = new StellarViewAetherEffects();
		event.register(StellarViewAetherEffects.AETHER_EFFECTS, aether);
	}
}
