package com.skillerguard;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Skill;

public final class CombatSkills
{
	public static final Set<Skill> ALL = Collections.unmodifiableSet(EnumSet.of(
		Skill.ATTACK,
		Skill.STRENGTH,
		Skill.DEFENCE,
		Skill.HITPOINTS,
		Skill.RANGED,
		Skill.MAGIC,
		Skill.PRAYER
	));

	public static final List<Skill> LAMP_LOCK_ORDER = Collections.unmodifiableList(Arrays.asList(
		Skill.ATTACK,
		Skill.STRENGTH,
		Skill.DEFENCE,
		Skill.HITPOINTS,
		Skill.RANGED,
		Skill.MAGIC,
		Skill.PRAYER
	));

	private CombatSkills()
	{
	}

	public static boolean isCombat(Skill skill)
	{
		return ALL.contains(skill);
	}
}
