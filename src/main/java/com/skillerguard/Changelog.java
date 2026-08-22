package com.skillerguard;

import java.util.List;

/**
 * Player-facing notes for the current plugin version. Keep this in lockstep with
 * {@code version} in {@code build.gradle} and {@code runelite-plugin.properties}.
 */
final class Changelog
{
	static final String VERSION = "1.1.1";

	static final List<String> NOTES = List.of(
		"Observatory Quest is a disclaimer for now: the 2018 skip may be level-1 skill or a level-3 account. Proceed at your own risk."
	);

	private Changelog()
	{
	}

	static boolean isUnseen(String seenVersion)
	{
		return seenVersion == null || seenVersion.isEmpty() || !VERSION.equals(seenVersion);
	}
}
