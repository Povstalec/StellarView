package net.povstalec.stellarview.client.render.space_objects;

import com.mojang.math.Vector3f;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.AxisRotation;

import javax.annotation.Nullable;

public abstract class OrbitingObjectRenderer<T extends OrbitingObject> extends TexturedObjectRenderer<T>
{
	public OrbitingObjectRenderer(T orbitingObject)
	{
		super(orbitingObject);
	}
	
	@Nullable
	public OrbitingObject.OrbitInfo orbitInfo()
	{
		return renderedObject.orbitInfo();
	}
	
	public void setupSynodicOrbit(@Nullable OrbitingObject.OrbitalPeriod parentOrbitalPeriod)
	{
		renderedObject.setupSynodicOrbit(parentOrbitalPeriod);
	}
	
	@Override
	public Vector3f getPosition(ViewCenter viewCenter, AxisRotation axisRotation, long ticks, float partialTicks)
	{
		return axisRotation.quaterniond().transform(getPosition(viewCenter, ticks, partialTicks));
	}
	
	@Override
	public Vector3f getPosition(ViewCenter viewCenter, long ticks, float partialTicks)
	{
		if(orbitInfo() != null)
		{
			if(!viewCenter.objectEquals(this) && orbitInfo().orbitClampNumber() > 0 && parent != null)
				return orbitInfo().getOrbitVector(ticks, partialTicks, parent.lastDistance);
			else
				return orbitInfo().getOrbitVector(ticks, partialTicks);
		}
		else
			return super.getPosition(viewCenter, ticks, partialTicks);
	}
}
