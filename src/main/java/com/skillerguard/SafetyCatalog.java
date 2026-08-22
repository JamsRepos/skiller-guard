package com.skillerguard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SafetyCatalog
{
	private static final List<SafetyRule> RULES = build();

	private SafetyCatalog()
	{
	}

	public static List<SafetyRule> rules()
	{
		return RULES;
	}

	private static List<SafetyRule> build()
	{
		List<SafetyRule> rules = new ArrayList<>();

		// Prayer: hide bury/scatter on every remains. Option-name only so Bone crossbow stays usable.
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Bury", "*", "Burying bones grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Scatter", "*", "Scattering ashes grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Take", "*bones*", "Ground bones grant Prayer XP if buried"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Take", "*ashes*", "Ground ashes grant Prayer XP if scattered"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Pray-at", "*shrine*", "Praying at a shrine can grant Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Use", "* -> *shrine*", "Offering at a shrine grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Use", "* -> Chaos altar", "Offering at the Chaos altar grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Use", "*bones* -> *altar*", "Offering bones at an altar grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Use", "*ashes* -> *altar*", "Offering ashes at an altar grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "*offer*", "*altar*", "Offering at an altar grants Prayer XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Open", "Sarcophagus", "Pyramid Plunder sarcophagus grants 20 Strength XP"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Eat", "*nightshade*", "Eating nightshade is a skiller footgun"));
		rules.add(SafetyRule.hide(SafetyCategory.PRAYER, "Worship", "Ectofuntus", "Worshipping the Ectofuntus grants Prayer XP"));

		// Combat training: almost every spell gives Magic XP. Home Teleport is excluded in MenuSafetyService.
		rules.add(SafetyRule.hide(SafetyCategory.COMBAT_TRAINING, "Attack", "*dummy*", "Attacking a dummy grants combat XP"));
		rules.add(SafetyRule.hide(SafetyCategory.COMBAT_TRAINING, "Hit", "*dummy*", "Hitting a dummy grants combat XP"));
		rules.add(SafetyRule.hide(SafetyCategory.COMBAT_TRAINING, "Fire", "*cannon*", "Firing a dwarf cannon grants Ranged XP"));
		rules.add(SafetyRule.hide(SafetyCategory.COMBAT_TRAINING, "Cast", "*", "Casting a spell grants Magic XP"));

		// NPC misclicks (opt-in hide pack). Whole-word globs so Manager is not hidden.
		for (String npc : new String[]{"man*", "woman*", "pirate*", "mugger*", "rat*"})
		{
			rules.add(SafetyRule.hideNpc("Talk-to", npc, "Talk-to on this NPC is a common skiller misclick"));
			rules.add(SafetyRule.hideNpc("Trick-or-treat", npc, "Trick-or-treat on this NPC is a common skiller misclick"));
			rules.add(SafetyRule.hideNpc("Pickpocket", npc, "Pickpocket on this NPC is a common skiller misclick"));
			rules.add(SafetyRule.hideNpc("Attack", npc, "Attacking this NPC grants combat XP"));
		}
		rules.add(SafetyRule.hideNpc("Talk-to", "Tamayu", "Talking to Tamayu after Tai Bwo Wannai Trio grants Attack and Strength XP"));
		rules.add(SafetyRule.hideNpc("Talk-to", "Information clerk", "This clerk can award XP lamps"));
		rules.add(SafetyRule.hideNpc("Talk-to", "Evil Bob", "Evil Bob random-event footgun"));

		// Memoirs charge: always hide (10 Magic XP / charge). Reminisce stays.
		rules.add(SafetyRule.alwaysHide(SafetyCategory.XP_TRAP, "Inspect", "Old Memorial",
			"Charging Kharedst's memoirs here grants 10 Magic XP per charge"));
		rules.add(SafetyRule.alwaysHide(SafetyCategory.XP_TRAP, "Use", "*memoirs* -> Old Memorial",
			"Charging Kharedst's memoirs grants 10 Magic XP per charge"));
		rules.add(SafetyRule.alwaysHide(SafetyCategory.XP_TRAP, "Use", "*Book of the dead* -> Old Memorial",
			"Charging the Book of the dead grants 10 Magic XP per charge"));
		rules.add(SafetyRule.alwaysHide(SafetyCategory.XP_TRAP, "Charge", "*memoirs*",
			"Charging Kharedst's memoirs grants 10 Magic XP per charge"));
		rules.add(SafetyRule.alwaysHide(SafetyCategory.XP_TRAP, "Charge", "*Book of the dead*",
			"Charging the Book of the dead grants 10 Magic XP per charge"));

		// Dual-use XP traps: warn by default
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Reanimate", "*", "Reanimating ensouled heads grants Magic/combat XP"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Cast", "*Reanimate*", "Reanimation spells grant Magic XP"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Talk-to", "Juna", "Tears of Guthix awards XP in your lowest skill"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Talk-to", "Tamayu", "Talking to Tamayu can grant Attack and Strength XP"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Talk-to", "Otto Godblessed", "Barbarian training can grant Strength or Prayer XP"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Talk-to", "Father Aereck", "The Restless Ghost / Father Aereck can grant Prayer XP"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Talk-to", "Historian Minas", "This NPC can award an XP lamp"));
		rules.add(SafetyRule.warn(SafetyCategory.XP_TRAP, "Talk-to", "Information clerk", "This clerk can award XP lamps"));

		return Collections.unmodifiableList(rules);
	}

	/**
	 * Home Teleport (any spellbook) gives no Magic XP and must stay clickable.
	 */
	public static boolean isZeroXpSpell(String target)
	{
		String t = MenuMatcher.strip(target).toLowerCase(java.util.Locale.ROOT);
		int arrow = t.indexOf("->");
		if (arrow >= 0)
		{
			t = t.substring(0, arrow).trim();
		}
		return t.equals("home teleport") || t.equals("lumbridge home teleport");
	}
}
