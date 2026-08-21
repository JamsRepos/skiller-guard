package com.skillerguard;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Skiller Guard",
	description = "Always-on skiller safety: hide risky menu options without juggling RuneLite profiles",
	tags = {"skiller", "skillers", "menu", "hide", "prayer", "safety", "level 3"}
)
public class SkillerGuardPlugin extends Plugin
{
	@Inject
	private SkillerGuardConfig config;

	@Override
	protected void startUp()
	{
		log.debug("Skiller Guard started");
	}

	@Override
	protected void shutDown()
	{
		log.debug("Skiller Guard stopped");
	}

	@Provides
	SkillerGuardConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillerGuardConfig.class);
	}
}
