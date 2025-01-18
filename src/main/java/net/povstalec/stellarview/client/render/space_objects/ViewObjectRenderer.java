package net.povstalec.stellarview.client.render.space_objects;

import net.povstalec.stellarview.api.common.space_objects.ViewObject;

public abstract class ViewObjectRenderer<T extends ViewObject> extends OrbitingObjectRenderer<T>
{
	public ViewObjectRenderer(T viewObject)
	{
		super(viewObject);
	}
}
