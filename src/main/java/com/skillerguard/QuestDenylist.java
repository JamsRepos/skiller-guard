package com.skillerguard;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Curated combat-XP quest names from the Skill pure page and Quest experience rewards.
 * Matching ignores a leading "The " and is case-insensitive.
 */
public final class QuestDenylist
{
	private static final Map<String, String> REASONS = build();

	private QuestDenylist()
	{
	}

	public static String reasonForTitle(String title)
	{
		if (title == null || title.isEmpty())
		{
			return null;
		}
		return REASONS.get(normalize(title));
	}

	public static String normalize(String title)
	{
		String t = title.trim().toLowerCase(Locale.ROOT);
		if (t.startsWith("the "))
		{
			t = t.substring(4);
		}
		return t;
	}

	static Map<String, String> reasons()
	{
		return REASONS;
	}

	private static void put(Map<String, String> map, String name, String reason)
	{
		map.merge(normalize(name), reason, (a, b) -> a.contains(b) ? a : a + "; " + b);
	}

	private static Map<String, String> build()
	{
		Map<String, String> map = new LinkedHashMap<>();

		put(map, "In Search of the Myreque", "Atk+Str+Def+HP XP");
		put(map, "Mountain Daughter", "Atk+Pray XP if finished");
		put(map, "Fairytale I - Growing Pains", "Atk+Mag XP");
		put(map, "In Aid of the Myreque", "Atk+Str+Def XP");
		put(map, "Tai Bwo Wannai Trio", "Atk+Str XP if you talk to Tamayu");
		put(map, "The Fremennik Trials", "Atk+Str+Def+HP+Mag XP");
		put(map, "Death Plateau", "Atk XP");
		put(map, "Underground Pass", "Atk XP");
		put(map, "Heroes' Quest", "Atk+Str+Def+HP+Rng XP");
		put(map, "Vampyre Slayer", "Atk XP");
		put(map, "Tree Gnome Village", "Atk XP");
		put(map, "Fight Arena", "Atk XP");
		put(map, "Waterfall Quest", "Atk+Str XP");
		put(map, "The Grand Tree", "Atk+Mag XP");
		put(map, "Knight Waves Training Ground", "Atk+Str+Def+HP XP");
		put(map, "A Soul's Bane", "Def+HP XP");
		put(map, "Nature Spirit", "Def+HP XP");
		put(map, "What Lies Below", "Def XP");
		put(map, "Between a Rock...", "Def XP");
		put(map, "Olaf's Quest", "Def XP");
		put(map, "Holy Grail", "Def+Pray XP");
		put(map, "Dragon Slayer I", "Def+Str XP");
		put(map, "King's Ransom", "Def+Mag XP");
		put(map, "Witch's House", "HP XP");
		put(map, "Dream Mentor", "HP+Mag XP");
		put(map, "Mourning's End Part I", "HP XP");
		put(map, "Royal Trouble", "HP XP");
		put(map, "Grim Tales", "HP XP");
		put(map, "Recipe for Disaster", "HP XP in later subquests");
		put(map, "Witch's Potion", "Mag XP");
		put(map, "Imp Catcher", "Mag XP");
		put(map, "Spirits of the Elid", "Mag+Pray XP");
		put(map, "The Giant Dwarf", "Mag XP");
		put(map, "Horror from the Deep", "Mag+Rng+Str XP");
		put(map, "Lunar Diplomacy", "Mag XP");
		put(map, "The Path of Glouphrie", "Mag+Str XP");
		put(map, "Enakhra's Lament", "Mag XP");
		put(map, "The Eyes of Glouphrie", "Mag XP");
		put(map, "Swan Song", "Mag+Pray XP");
		put(map, "Watchtower", "Mag XP if finished");
		put(map, "Desert Treasure I", "Mag XP");
		put(map, "Rag and Bone Man I", "Pray XP");
		put(map, "Making History", "Pray XP");
		put(map, "Recruitment Drive", "Pray XP");
		put(map, "The Restless Ghost", "Pray XP");
		put(map, "Priest in Peril", "Pray XP");
		put(map, "Ghosts Ahoy", "Pray XP");
		put(map, "Another Slice of H.A.M.", "Pray XP");
		put(map, "Rag and Bone Man II", "Pray XP");
		put(map, "The Great Brain Robbery", "Pray XP");
		put(map, "Rum Deal", "Pray XP");
		put(map, "Big Chompy Bird Hunting", "Rng XP");
		put(map, "Death to the Dorgeshuun", "Rng XP");
		put(map, "Zogre Flesh Eaters", "Rng XP");
		put(map, "Temple of Ikov", "Rng XP");
		put(map, "Scorpion Catcher", "Str XP");
		put(map, "Troll Romance", "Str XP");
		put(map, "Roving Elves", "Str XP");
		put(map, "Haunted Mine", "Str XP");
		put(map, "Cold War", "40 Atk XP if you exit the icelord cage");
		put(map, "Tears of Guthix", "Awards XP in your lowest skill");

		return Collections.unmodifiableMap(map);
	}
}
