package com.skillerguard;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(SkillerGuardConfig.GROUP)
public interface SkillerGuardConfig extends Config
{
	String GROUP = "skiller-guard";
	String SEEN_CHANGELOG_VERSION_KEY = "seenChangelogVersion";

	@ConfigSection(
		name = "General",
		description = "Turn Guard on and choose when it should protect you.",
		position = 0
	)
	String generalSection = "general";

	@ConfigSection(
		name = "Protections",
		description = "Choose which accidental combat or Prayer XP actions Guard should stop.",
		position = 1
	)
	String packsSection = "packs";

	@ConfigSection(
		name = "Warnings",
		description = "How Guard warns you about dangerous NPCs, settings, and XP traps.",
		position = 2
	)
	String warningsSection = "warnings";

	@ConfigItem(
		keyName = "enabled",
		name = "Enable Jam's Skiller Guard",
		description = "Turns the whole plugin on or off. When this is off, Guard does nothing.",
		section = generalSection,
		position = 0
	)
	default boolean enabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "activationMode",
		name = "When to protect",
		description = "Always on: protects you whenever the plugin is enabled. Level-3 only: only when Hitpoints is 10 or lower and Attack, Strength, Defence, Ranged, Magic, and Prayer are all 3 or lower.",
		section = generalSection,
		position = 1
	)
	default ActivationMode activationMode()
	{
		return ActivationMode.ALWAYS;
	}

	@ConfigItem(
		keyName = "showStatusOverlay",
		name = "Show status panel",
		description = "Shows a small debug panel with whether Guard is on, plus the last warning. Off by default.",
		section = generalSection,
		position = 2
	)
	default boolean showStatusOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hidePrayerXp",
		name = "Block Prayer XP",
		description = "Hides burying, scattering, and offering remains so you cannot get Prayer XP by accident.",
		section = packsSection,
		position = 0
	)
	default boolean hidePrayerXp()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideCombatTraining",
		name = "Block combat training",
		description = "Hides training methods that grant Attack, Strength, Defence, Ranged, or Magic XP without needing to fight a player.",
		section = packsSection,
		position = 1
	)
	default boolean hideCombatTraining()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideNpcMisclicks",
		name = "Block NPC misclicks",
		description = "Hides Talk-to and Attack on common misclick NPCs. Off by default because it can get in the way of normal talking.",
		section = packsSection,
		position = 2
	)
	default boolean hideNpcMisclicks()
	{
		return false;
	}

	@ConfigItem(
		keyName = "xpTrapProtection",
		name = "Watch for XP traps",
		description = "Warns on NPCs and items that can dump combat or Prayer XP. Charging Kharedst's memoirs is always blocked.",
		section = packsSection,
		position = 3
	)
	default boolean xpTrapProtection()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lampLockout",
		name = "Lock combat skills on lamps",
		description = "Hides Attack, Strength, Defence, Hitpoints, Ranged, Magic, and Prayer on XP lamps so you cannot put the XP into a combat skill by accident.",
		section = packsSection,
		position = 4
	)
	default boolean lampLockout()
	{
		return true;
	}

	@ConfigItem(
		keyName = "questWarnings",
		name = "Quest warnings",
		description = "Warns you when you open a combat-XP quest journal, start that quest, or open it in Quest Helper.",
		section = packsSection,
		position = 5
	)
	default boolean questWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "warnInProgressQuests",
		name = "Warn for started quests",
		description = "Also warns when you log in if a combat-XP quest is already in progress. Leave this off if you have already started one, such as Recipe for Disaster.",
		section = packsSection,
		position = 6
	)
	default boolean warnInProgressQuests()
	{
		return false;
	}

	@ConfigItem(
		keyName = "warnOnCombatXp",
		name = "Combat XP alarm",
		description = "If combat or Prayer XP still slips through, this raises a large on-screen alarm, a chat message, and a sound.",
		section = packsSection,
		position = 7
	)
	default boolean warnOnCombatXp()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpTrapMode",
		name = "XP traps",
		description = "Warn only: the option stays clickable and Guard adds a [SG] line when you right-click. Hide + warn: Guard also removes that option.",
		section = warningsSection,
		position = 0
	)
	default XpTrapMode xpTrapMode()
	{
		return XpTrapMode.WARN_ONLY;
	}

	@ConfigItem(
		keyName = "npcOverheadWarnings",
		name = "NPC labels",
		description = "Draws a short [SG] label above NPCs that can grant combat or Prayer XP if you talk to them.",
		section = warningsSection,
		position = 1
	)
	default boolean npcOverheadWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "npcOverheadColor",
		name = "NPC label colour",
		description = "Colour of the [SG] text above those NPCs.",
		section = warningsSection,
		position = 2
	)
	default Color npcOverheadColor()
	{
		return new Color(255, 170, 0);
	}

	@ConfigItem(
		keyName = "warnDangerousSettings",
		name = "Dangerous settings",
		description = "Warns while Auto Retaliate is on, or while NPC/Player Attack options are not Hidden. Player Attack is not warned on PvP or Deadman worlds. Guard cannot change those for you — turn them off in Combat Options and Controls.",
		section = warningsSection,
		position = 3
	)
	default boolean warnDangerousSettings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dangerAlertMode",
		name = "Warning type",
		description = "On-screen: a large message. Sound: an in-game warning noise. Both: message and sound.",
		section = warningsSection,
		position = 4
	)
	default DangerAlertMode dangerAlertMode()
	{
		return DangerAlertMode.BANNERS_AND_SOUND;
	}

	@ConfigItem(
		keyName = "blinkDangerBanners",
		name = "Flash warning",
		description = "Makes the on-screen warning flash so it is harder to miss. Does nothing if you chose Sound.",
		section = warningsSection,
		position = 5
	)
	default boolean blinkDangerBanners()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dangerSound",
		name = "Warning sound",
		description = "Which in-game sound to play for the dangerous-settings warning.",
		section = warningsSection,
		position = 6
	)
	default WarningSound dangerSound()
	{
		return WarningSound.TOWN_CRIER;
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
		keyName = "dangerSoundVolume",
		name = "Warning volume (%)",
		description = "How loud the warning is. 100 is full volume; go higher to make it louder than other game sounds.",
		section = warningsSection,
		position = 7
	)
	default int dangerSoundVolume()
	{
		return 80;
	}

	@Range(min = 0, max = 60)
	@ConfigItem(
		keyName = "dangerSoundInterval",
		name = "Repeat every (seconds)",
		description = "How often to play the warning while the problem is still there. 0 plays once when it first appears, then stays quiet.",
		section = warningsSection,
		position = 8
	)
	default int dangerSoundInterval()
	{
		return 12;
	}

	@ConfigItem(
		keyName = SEEN_CHANGELOG_VERSION_KEY,
		name = "Seen changelog version",
		description = "Last Jam's Skiller Guard version whose update notes were shown in chat.",
		hidden = true
	)
	default String seenChangelogVersion()
	{
		return "";
	}
}
