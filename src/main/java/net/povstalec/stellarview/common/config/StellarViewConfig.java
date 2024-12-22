package net.povstalec.stellarview.common.config;

import java.io.File;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.stellarview.StellarView;

@Mod.EventBusSubscriber
public class StellarViewConfig
{
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec CLIENT_CONFIG;
	
	static
	{
		CLIENT_BUILDER.push("Stellar View Client Config");
		
		generalClientConfig(CLIENT_BUILDER);

		CLIENT_BUILDER.pop();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	public static void loadConfig(ForgeConfigSpec config, String path)
	{
		StellarView.LOGGER.info("Loading Config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
		StellarView.LOGGER.info("Built config: " + path);
		file.load();
		StellarView.LOGGER.info("Loaded Config: " + path);
		config.setConfig(file);
	}
	
	private static void generalClientConfig(ForgeConfigSpec.Builder client)
	{
		CLIENT_BUILDER.push("General Config");
		GeneralConfig.init(client);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.push("Overworld Config");
		OverworldConfig.init(client);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.push("Nether Config");
		NetherConfig.init(client);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.push("End Config");
		EndConfig.init(client);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.push("Twilight Forest Config");
		TwilightForestConfig.init(client);
		CLIENT_BUILDER.pop();
		
		CLIENT_BUILDER.push("Aether Config");
		AetherConfig.init(client);
		CLIENT_BUILDER.pop();
	}
}
