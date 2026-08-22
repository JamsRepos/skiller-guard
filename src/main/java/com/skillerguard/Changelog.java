package com.skillerguard;

import java.util.List;

/**
 * Player-facing notes for the current plugin version. Keep this in lockstep with
 * {@code version} in {@code build.gradle} and {@code runelite-plugin.properties}.
 */
final class Changelog
{
	static final String VERSION = "1.1.0";

	static final List<String> NOTES = List.of(
		"House lecterns no longer let you make tablets (Magic XP). Removing the furniture still works.",
		"House dartboards and other ranging games no longer offer Play (Ranged XP).",
		"Harpoon is hidden when you would fish barehanded (Strength XP). Cage, Net, and fishing with a harpoon still work.",
		"More skiller-reachable Prayer and Strength spots are blocked, including the Forthos burner, Varlamore bowl, Blast Furnace pump, and barbarian Use-rod.",
		"Observatory Quest now warns — it can still give Hitpoints XP."
	);

	private Changelog()
	{
	}

	static boolean isUnseen(String seenVersion)
	{
		return seenVersion == null || seenVersion.isEmpty() || !VERSION.equals(seenVersion);
	}
}
