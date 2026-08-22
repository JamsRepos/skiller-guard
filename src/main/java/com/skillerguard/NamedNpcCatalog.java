package com.skillerguard;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.client.util.Text;

/**
 * Named NPCs that get overhead {@code [SG]} labels. Exact names only — not Man/Woman.
 */
public final class NamedNpcCatalog
{
	private static final Map<String, String> LABELS = build();

	private NamedNpcCatalog()
	{
	}

	public static String labelFor(String npcName)
	{
		if (npcName == null)
		{
			return null;
		}
		return LABELS.get(Text.removeTags(npcName).trim().toLowerCase(Locale.ROOT));
	}

	static Map<String, String> labels()
	{
		return LABELS;
	}

	private static Map<String, String> build()
	{
		Map<String, String> map = new LinkedHashMap<>();
		map.put("tamayu", "[SG] Atk+Str XP");
		map.put("juna", "[SG] lowest skill");
		map.put("historian minas", "[SG] lamp XP");
		map.put("otto godblessed", "[SG] Str/Pray XP");
		map.put("father aereck", "[SG] Pray XP");
		map.put("information clerk", "[SG] lamp XP");
		return Collections.unmodifiableMap(map);
	}
}
