package com.skillerguard;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scene objects that get {@code [SG]} labels, keyed by {@link net.runelite.api.gameval.ObjectID}.
 */
public final class NamedObjectCatalog
{
	private static final Map<Integer, String> LABELS = build();

	private NamedObjectCatalog()
	{
	}

	public static String labelFor(int objectId)
	{
		return LABELS.get(objectId);
	}

	static Map<Integer, String> labels()
	{
		return LABELS;
	}

	private static Map<Integer, String> build()
	{
		Map<Integer, String> map = new LinkedHashMap<>();
		map.put(SafetyIds.OLD_MEMORIAL, "[SG] Mag XP");
		for (int id : SafetyIds.POH_LECTERNS)
		{
			map.put(id, "[SG] Mag XP");
		}
		for (int id : SafetyIds.POH_RANGING_GAMES)
		{
			map.put(id, "[SG] Rng XP");
		}
		return Collections.unmodifiableMap(map);
	}
}
