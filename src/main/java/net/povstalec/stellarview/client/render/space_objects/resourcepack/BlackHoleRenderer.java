package net.povstalec.stellarview.client.render.space_objects.resourcepack;

import net.povstalec.stellarview.api.common.space_objects.resourcepack.BlackHole;
import net.povstalec.stellarview.client.render.space_objects.GravityLenseRenderer;

public class BlackHoleRenderer<T extends BlackHole> extends GravityLenseRenderer<T>
{
	public BlackHoleRenderer(T blackHole)
	{
		super(blackHole);
	}
}
