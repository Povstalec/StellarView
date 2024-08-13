package net.povstalec.stellarview.client.render.level.misc;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public class StellarViewFogEffects
{
	public static boolean doesMobEffectBlockSky(Camera camera)
	{
		Entity entity = camera.getEntity();
		if(!(entity instanceof LivingEntity livingentity))
			return false;
		else
			return livingentity.hasEffect(MobEffects.BLINDNESS) || livingentity.hasEffect(MobEffects.DARKNESS);
	}
	
	public static boolean isFoggy(Minecraft minecraft, Camera camera)
	{
		Vec3 cameraPos = camera.getPosition();
		boolean isFoggy = minecraft.level.effects().isFoggyAt(Mth.floor(cameraPos.x()), Mth.floor(cameraPos.y())) || minecraft.gui.getBossOverlay().shouldCreateWorldFog();
		if(isFoggy)
			return true;
		
		FogType fogtype = camera.getFluidInCamera();
		return fogtype == FogType.POWDER_SNOW || fogtype == FogType.LAVA || doesMobEffectBlockSky(camera);
	}
}
