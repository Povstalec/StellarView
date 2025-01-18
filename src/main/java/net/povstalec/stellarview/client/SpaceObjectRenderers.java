package net.povstalec.stellarview.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;

import javax.annotation.Nullable;
import java.util.HashMap;

public class SpaceObjectRenderers
{
	private static final HashMap<Class<? extends SpaceObject>, SpaceObjectConstructor> RENDERERS = new HashMap<>();
	
	public static <T extends SpaceObject, U extends SpaceObjectRenderer<T>> void register(Class<T> objectClass, SpaceObjectConstructor<T, U> constructor)
	{
		if(RENDERERS.containsKey(objectClass))
			throw new IllegalStateException("Duplicate registration for " + objectClass.getName());
		
		RENDERERS.put(objectClass, constructor);
	}
	
	@Nullable
	public static SpaceObjectRenderer constructObjectRenderer(SpaceObject object)
	{
		if(RENDERERS.containsKey(object.getClass()))
			return RENDERERS.get(object.getClass()).create(object);
		
		return null;
	}
	
	
	
	public interface SpaceObjectConstructor<T extends SpaceObject, U extends SpaceObjectRenderer>
	{
		U create(T object);
	}
}
