package net.povstalec.stellarview.api.init;

import net.povstalec.stellarview.api.celestials.Galaxy;
import net.povstalec.stellarview.api.celestials.StarField;
import net.povstalec.stellarview.api.celestials.Supernova;

public class StarFieldInit
{
	private static final int TICKS_PER_DAY = 24000;
	
	public static final StarField VANILLA = new StarField.VanillaStarField(10842L, (short) 1500);
	
	public static final StarField MILKY_WAY = new Galaxy.SpiralGalaxy(10842L, (byte) 4, (short) 1500)
			.addGalacticObject(new Supernova(10.0F, 15 * TICKS_PER_DAY + 18000, 5 * TICKS_PER_DAY), 10, -3, 2);
	
	public static final StarField SPINDLE_GALAXY = new Galaxy.LenticularGalaxy(10842L, (short) 6000);
}
