package net.povstalec.stellarview.compatibility.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.povstalec.stellarview.client.screens.config.ConfigScreen;

public class StellarViewModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return screen -> new ConfigScreen(screen);
	}
}
