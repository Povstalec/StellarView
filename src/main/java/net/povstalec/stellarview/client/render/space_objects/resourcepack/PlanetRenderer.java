package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import net.povstalec.stellarview.client.render.space_objects.ViewObjectRenderer;
import net.povstalec.stellarview.api.common.space_objects.resourcepack.Planet;

public class PlanetRenderer<T extends Planet> extends ViewObjectRenderer<T>
{
	public PlanetRenderer(T planet)
	{
		super(planet);
	}
}
