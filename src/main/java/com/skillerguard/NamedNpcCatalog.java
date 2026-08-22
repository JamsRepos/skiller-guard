package com.skillerguard;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Named NPCs that get overhead {@code [SG]} labels, keyed by {@link net.runelite.api.gameval.NpcID}.
 */
public final class NamedNpcCatalog
{
	private static final Map<Integer, String> LABELS = build();

	private NamedNpcCatalog()
	{
	}

	public static String labelFor(int npcId)
	{
		return LABELS.get(npcId);
	}

	static Map<Integer, String> labels()
	{
		return LABELS;
	}

	private static Map<Integer, String> build()
	{
		Map<Integer, String> map = new LinkedHashMap<>();
		for (int id : SafetyIds.TAMAYU)
		{
			map.put(id, "[SG] Atk+Str XP");
		}
		for (int id : SafetyIds.JUNA)
		{
			map.put(id, "[SG] lowest skill");
		}
		for (int id : SafetyIds.OTTO)
		{
			map.put(id, "[SG] Str/Pray XP");
		}
		for (int id : SafetyIds.ELNOCK)
		{
			map.put(id, "[SG] Str XP wheat");
		}
		for (int id : SafetyIds.FATHER_AERECK)
		{
			map.put(id, "[SG] Pray XP");
		}
		for (int id : SafetyIds.INFORMATION_CLERK)
		{
			map.put(id, "[SG] Pray XP");
		}
		return Collections.unmodifiableMap(map);
	}
}
