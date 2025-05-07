package net.povstalec.stellarview.common.config;

public class StellarViewConfig
{
	private static final StellarViewConfigSpec.Builder CLIENT_BUILDER = new StellarViewConfigSpec.Builder();
	public static final StellarViewConfigSpec CLIENT_CONFIG;
	
	static
	{
		CLIENT_BUILDER.push("Stellar View Client Config");
		
		generalClientConfig(CLIENT_BUILDER);

		CLIENT_BUILDER.pop();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	private static void generalClientConfig(StellarViewConfigSpec.Builder client)
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
