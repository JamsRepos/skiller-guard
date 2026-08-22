package com.skillerguard;

import lombok.Value;

@Value
public class SafetyRule
{
	SafetyCategory category;
	SafetyAction action;
	String optionPattern;
	String targetPattern;
	String reason;
	boolean npcTarget;
	boolean alwaysHide;

	public static SafetyRule hide(SafetyCategory category, String option, String target, String reason)
	{
		return new SafetyRule(category, SafetyAction.HIDE, option, target, reason, false, false);
	}

	public static SafetyRule hideNpc(String option, String target, String reason)
	{
		return new SafetyRule(SafetyCategory.NPC_MISCLICK, SafetyAction.HIDE, option, target, reason, true, false);
	}

	public static SafetyRule warn(SafetyCategory category, String option, String target, String reason)
	{
		return new SafetyRule(category, SafetyAction.WARN, option, target, reason, false, false);
	}

	public static SafetyRule alwaysHide(SafetyCategory category, String option, String target, String reason)
	{
		return new SafetyRule(category, SafetyAction.HIDE, option, target, reason, false, true);
	}

	public boolean matches(String option, String target)
	{
		if (!MenuMatcher.matches(optionPattern, option))
		{
			return false;
		}
		if (npcTarget)
		{
			return MenuMatcher.matchesNpcTarget(targetPattern, target);
		}
		return MenuMatcher.matches(targetPattern, target);
	}
}
