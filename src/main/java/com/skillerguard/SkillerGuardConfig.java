package com.skillerguard;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("skiller-guard")
public interface SkillerGuardConfig extends Config
{
	@ConfigItem(
		keyName = "enabled",
		name = "Enable Skiller Guard",
		description = "Master toggle for skiller menu protection",
		position = 0
	)
	default boolean enabled()
	{
		return true;
	}
}
