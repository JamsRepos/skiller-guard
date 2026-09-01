package com.skillerguard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Player-facing notes per plugin version. {@link #VERSION} must match
 * {@code version} in {@code build.gradle} and {@code runelite-plugin.properties}.
 * Append a {@link Release} on each bump so skipped Hub versions still get their notes.
 */
final class Changelog
{
	static final String VERSION = "1.1.4";

	static final List<Release> RELEASES = List.of(
		new Release("1.1.0",
			"House lecterns no longer let you make tablets (Magic XP). Removing the furniture still works.",
			"House dartboards and other ranging games no longer offer Play (Ranged XP).",
			"Harpoon is hidden when you would fish barehanded (Strength XP). Cage, Net, and fishing with a harpoon still work.",
			"More skiller-reachable Prayer and Strength spots are blocked, including the Forthos burner, Varlamore bowl, Blast Furnace pump, and barbarian Use-rod.",
			"Observatory Quest now warns — it can still give Hitpoints XP."
		),
		new Release("1.1.1",
			"Observatory Quest is a disclaimer for now: the 2018 skip may be level-1 skill or a level-3 account. Proceed at your own risk."
		),
		new Release("1.1.2",
			"If you missed a version, update notes now list each one oldest first."
		),
		new Release("1.1.3",
			"Player Attack options are no longer warned on PvP or Deadman worlds."
		),
		new Release("1.1.4",
			"Renamed from Skiller Guard to Jam's Skiller Guard in the plugin panel and Hub."
		)
	);

	private Changelog()
	{
	}

	static boolean isUnseen(String seenVersion)
	{
		return !unseenSince(seenVersion).isEmpty();
	}

	static List<Release> unseenSince(String seenVersion)
	{
		String seen = seenVersion == null ? "" : seenVersion;
		List<Release> unseen = new ArrayList<>();
		for (Release release : RELEASES)
		{
			if (compareVersions(release.version, seen) > 0)
			{
				unseen.add(release);
			}
		}
		return Collections.unmodifiableList(unseen);
	}

	static int compareVersions(String left, String right)
	{
		int[] a = parseVersion(left);
		int[] b = parseVersion(right);
		int n = Math.max(a.length, b.length);
		for (int i = 0; i < n; i++)
		{
			int av = i < a.length ? a[i] : 0;
			int bv = i < b.length ? b[i] : 0;
			if (av != bv)
			{
				return Integer.compare(av, bv);
			}
		}
		return 0;
	}

	private static int[] parseVersion(String version)
	{
		if (version == null || version.isEmpty())
		{
			return new int[0];
		}
		String[] parts = version.split("\\.");
		int[] values = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
		{
			try
			{
				values[i] = Integer.parseInt(parts[i]);
			}
			catch (NumberFormatException ex)
			{
				values[i] = 0;
			}
		}
		return values;
	}

	static final class Release
	{
		final String version;
		final List<String> notes;

		Release(String version, String... notes)
		{
			this.version = version;
			this.notes = List.of(notes);
		}
	}
}
