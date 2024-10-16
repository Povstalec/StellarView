package net.povstalec.stellarview.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.povstalec.stellarview.StellarView;

import java.io.File;

public class StellarViewConfig
{
	private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec CLIENT_CONFIG;
	
	static
	{
		CLIENT_BUILDER.push("Stellar View Client Config");
		
		generalClientConfig(CLIENT_BUILDER);

		CLIENT_BUILDER.pop();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	public static void loadConfig(ModConfigSpec config, String path)
	{
		StellarView.LOGGER.info("Loading Config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
		StellarView.LOGGER.info("Built config: " + path);
		file.load();
		StellarView.LOGGER.info("Loaded Config: " + path);
		config.correct(file);
	}
	
	private static void generalClientConfig(ModConfigSpec.Builder client)
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
	}
}
