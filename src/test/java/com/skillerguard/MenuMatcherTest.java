package com.skillerguard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MenuMatcherTest
{
	@Test
	public void buryBonesMatches()
	{
		SafetyRule bury = SafetyRule.hide(SafetyCategory.PRAYER, "Bury", "*", "prayer");
		assertTrue(bury.matches("Bury", "Bones"));
		assertTrue(bury.matches("<col=ff9040>Bury</col>", "Dragon bones"));
	}

	@Test
	public void boneCrossbowIsNotBuryOrTakeBones()
	{
		SafetyRule bury = SafetyRule.hide(SafetyCategory.PRAYER, "Bury", "*", "prayer");
		SafetyRule takeBones = SafetyRule.hide(SafetyCategory.PRAYER, "Take", "*bones*", "prayer");
		assertFalse(bury.matches("Use", "Bone crossbow"));
		assertFalse(takeBones.matches("Take", "Bone crossbow"));
		assertFalse(takeBones.matches("Wield", "Bone crossbow"));
	}

	@Test
	public void walkHereAndCancelNeverProtectedByMistakeInMatcherHelper()
	{
		assertTrue(MenuMatcher.isProtectedOption("Walk here"));
		assertTrue(MenuMatcher.isProtectedOption("Cancel"));
		assertFalse(MenuMatcher.isProtectedOption("Bury"));
	}

	@Test
	public void useBonesOnAltarMatchesButTalismanDoesNot()
	{
		SafetyRule bonesOnAltar = SafetyRule.hide(SafetyCategory.PRAYER, "Use", "*bones* -> *altar*", "prayer");
		assertTrue(bonesOnAltar.matches("Use", "Bones -> Chaos altar"));
		assertFalse(bonesOnAltar.matches("Use", "Air talisman -> Air altar"));
	}

	@Test
	public void manGlobDoesNotMatchManager()
	{
		assertTrue(MenuMatcher.matchesNpcTarget("man*", "Man"));
		assertTrue(MenuMatcher.matchesNpcTarget("man*", "Man (bearded)"));
		assertFalse(MenuMatcher.matchesNpcTarget("man*", "Manager"));
		assertFalse(MenuMatcher.matchesNpcTarget("man*", "Human"));
	}

	@Test
	public void scatterAnyAshesIncludingGotr()
	{
		SafetyRule scatter = SafetyRule.hide(SafetyCategory.PRAYER, "Scatter", "*", "prayer");
		assertTrue(scatter.matches("Scatter", "Abyssal ashes"));
		assertTrue(scatter.matches("Scatter", "Fiendish ashes"));
	}
}
