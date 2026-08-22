package com.skillerguard;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.api.Quest;

/**
 * Combat-XP quests keyed by {@link Quest}. Journal widgets and Quest Helper still
 * expose titles, which are matched to {@link Quest#getName()}.
 */
public final class QuestDenylist
{
	static final String OBSERVATORY_REASON =
		"Unsure if 2018 skip is level-1 skill or level-3 account — proceed at your own risk";

	private static final Map<Quest, String> BY_QUEST = buildQuests();
	private static final Map<String, String> BY_TITLE = buildTitles();

	private QuestDenylist()
	{
	}

	public static String reasonFor(Quest quest)
	{
		return quest == null ? null : BY_QUEST.get(quest);
	}

	public static boolean isDisclaimerTitle(String title)
	{
		return title != null && !title.isEmpty()
			&& normalize(title).equals(normalize(Quest.OBSERVATORY_QUEST.getName()));
	}

	public static String reasonForTitle(String title)
	{
		if (title == null || title.isEmpty())
		{
			return null;
		}
		return BY_TITLE.get(normalize(title));
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
		return BY_TITLE;
	}

	private static void put(Map<Quest, String> map, Quest quest, String reason)
	{
		map.merge(quest, reason, (a, b) -> a.contains(b) ? a : a + "; " + b);
	}

	private static Map<Quest, String> buildQuests()
	{
		Map<Quest, String> map = new EnumMap<>(Quest.class);
		put(map, Quest.IN_SEARCH_OF_THE_MYREQUE, "Atk+Str+Def+HP XP");
		put(map, Quest.MOUNTAIN_DAUGHTER, "Atk+Pray XP if finished");
		put(map, Quest.FAIRYTALE_I__GROWING_PAINS, "Atk+Mag XP");
		put(map, Quest.IN_AID_OF_THE_MYREQUE, "Atk+Str+Def XP");
		put(map, Quest.TAI_BWO_WANNAI_TRIO, "Atk+Str XP if you talk to Tamayu");
		put(map, Quest.THE_FREMENNIK_TRIALS, "Atk+Str+Def+HP+Mag XP");
		put(map, Quest.DEATH_PLATEAU, "Atk XP");
		put(map, Quest.UNDERGROUND_PASS, "Atk XP");
		put(map, Quest.HEROES_QUEST, "Atk+Str+Def+HP+Rng XP");
		put(map, Quest.VAMPYRE_SLAYER, "Atk XP");
		put(map, Quest.TREE_GNOME_VILLAGE, "Atk XP");
		put(map, Quest.FIGHT_ARENA, "Atk XP");
		put(map, Quest.WATERFALL_QUEST, "Atk+Str XP");
		put(map, Quest.THE_GRAND_TREE, "Atk+Mag XP");
		put(map, Quest.A_SOULS_BANE, "Def+HP XP");
		put(map, Quest.NATURE_SPIRIT, "Def+HP XP");
		put(map, Quest.WHAT_LIES_BELOW, "Def XP");
		put(map, Quest.BETWEEN_A_ROCK, "Def XP");
		put(map, Quest.OLAFS_QUEST, "Def XP");
		put(map, Quest.HOLY_GRAIL, "Def+Pray XP");
		put(map, Quest.DRAGON_SLAYER_I, "Def+Str XP");
		put(map, Quest.KINGS_RANSOM, "Def+Mag XP");
		put(map, Quest.WITCHS_HOUSE, "HP XP");
		put(map, Quest.DREAM_MENTOR, "HP+Mag XP");
		put(map, Quest.MOURNINGS_END_PART_I, "HP XP");
		put(map, Quest.ROYAL_TROUBLE, "HP XP");
		put(map, Quest.GRIM_TALES, "HP XP");
		put(map, Quest.RECIPE_FOR_DISASTER, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__MOUNTAIN_DWARF, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__PIRATE_PETE, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__LUMBRIDGE_GUIDE, "Mag XP");
		put(map, Quest.RECIPE_FOR_DISASTER__EVIL_DAVE, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__SKRACH_UGLOGWEE, "Rng XP");
		put(map, Quest.RECIPE_FOR_DISASTER__SIR_AMIK_VARZE, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI, "HP XP in later subquests");
		put(map, Quest.RECIPE_FOR_DISASTER__CULINAROMANCER, "HP XP in later subquests");
		put(map, Quest.WITCHS_POTION, "Mag XP");
		put(map, Quest.IMP_CATCHER, "Mag XP");
		put(map, Quest.SPIRITS_OF_THE_ELID, "Mag+Pray XP");
		put(map, Quest.THE_GIANT_DWARF, "Mag XP");
		put(map, Quest.HORROR_FROM_THE_DEEP, "Mag+Rng+Str XP");
		put(map, Quest.LUNAR_DIPLOMACY, "Mag XP");
		put(map, Quest.THE_PATH_OF_GLOUPHRIE, "Mag+Str XP");
		put(map, Quest.ENAKHRAS_LAMENT, "Mag XP");
		put(map, Quest.THE_EYES_OF_GLOUPHRIE, "Mag XP");
		put(map, Quest.SWAN_SONG, "Mag+Pray XP");
		put(map, Quest.WATCHTOWER, "Mag XP if finished");
		put(map, Quest.DESERT_TREASURE_I, "Mag XP");
		put(map, Quest.RAG_AND_BONE_MAN_I, "Pray XP");
		put(map, Quest.MAKING_HISTORY, "Pray XP");
		put(map, Quest.RECRUITMENT_DRIVE, "Pray XP");
		put(map, Quest.THE_RESTLESS_GHOST, "Pray XP");
		put(map, Quest.PRIEST_IN_PERIL, "Pray XP");
		put(map, Quest.HIS_FAITHFUL_SERVANTS, "Pray XP");
		put(map, Quest.HOPESPEARS_WILL, "Pray XP");
		put(map, Quest.GHOSTS_AHOY, "Pray XP");
		put(map, Quest.ANOTHER_SLICE_OF_HAM, "Pray XP");
		put(map, Quest.RAG_AND_BONE_MAN_II, "Pray XP");
		put(map, Quest.THE_GREAT_BRAIN_ROBBERY, "Pray XP");
		put(map, Quest.RUM_DEAL, "Pray XP");
		put(map, Quest.BIG_CHOMPY_BIRD_HUNTING, "Rng XP");
		put(map, Quest.DEATH_TO_THE_DORGESHUUN, "Rng XP");
		put(map, Quest.ZOGRE_FLESH_EATERS, "Rng XP");
		put(map, Quest.TEMPLE_OF_IKOV, "Rng XP");
		put(map, Quest.SCORPION_CATCHER, "Str XP");
		put(map, Quest.TROLL_ROMANCE, "Str XP");
		put(map, Quest.ROVING_ELVES, "Str XP");
		put(map, Quest.HAUNTED_MINE, "Str XP");
		put(map, Quest.COLD_WAR, "40 Atk XP if you exit the icelord cage");
		put(map, Quest.TEARS_OF_GUTHIX, "Awards XP in your lowest skill");
		put(map, Quest.OBSERVATORY_QUEST, OBSERVATORY_REASON);
		put(map, Quest.BARBARIAN_TRAINING, "Str/Pray XP from Otto's tasks");
		return Collections.unmodifiableMap(map);
	}

	private static Map<String, String> buildTitles()
	{
		Map<String, String> map = new LinkedHashMap<>();
		for (Map.Entry<Quest, String> entry : BY_QUEST.entrySet())
		{
			map.merge(normalize(entry.getKey().getName()), entry.getValue(),
				(a, b) -> a.contains(b) ? a : a + "; " + b);
		}
		// Minigame, not a Quest enum.
		map.put(normalize("Knight Waves Training Ground"), "Atk+Str+Def+HP XP");
		return Collections.unmodifiableMap(map);
	}
}
