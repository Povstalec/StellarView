package net.povstalec.stellarview.common.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.povstalec.stellarview.StellarView;
import org.lwjgl.glfw.GLFW;

public class KeyBindings
{
	private static final String MODID = StellarView.MODID;
	
	public static final String KEY_CATEGORY_OPEN_CONFIG = "key.category." + MODID + ".config_screen";
	public static final String KEY_OPEN_CONFIG = "key." + MODID + ".open_config_screen";
	
	public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(KEY_OPEN_CONFIG, 
			KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 
			GLFW.GLFW_KEY_G, KEY_CATEGORY_OPEN_CONFIG);
}
