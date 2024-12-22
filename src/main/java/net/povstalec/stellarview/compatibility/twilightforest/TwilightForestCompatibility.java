package net.povstalec.stellarview.compatibility.twilightforest;

import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.povstalec.stellarview.StellarView;

public class TwilightForestCompatibility
{
	public static StellarViewTwilightForestEffects twilightForest;
	
	public static void registerTwilightForestEffects(RegisterDimensionSpecialEffectsEvent event)
	{
		twilightForest = new StellarViewTwilightForestEffects();
		event.register(StellarViewTwilightForestEffects.TWILIGHT_FOREST_EFFECTS, twilightForest);
	}
}
