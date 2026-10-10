package com.skillerguard;

import java.util.Set;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

/**
 * PvP minigames whose player combat grants real combat XP unless the player turns it off in-game
 * (added 25 March 2026). The game forces Attack onto players inside the arenas, so the Player
 * Attack warning is muted there; the toggle NPC or portal gets an {@code [SG] Disable XP} label.
 * Arena regions match RuneLite's Discord plugin, minus lobbies where Attack is not forced.
 */
public enum XpToggleMinigame
{
	// Castle Wars keeps its toggle server-side (no client var changes on Lanthus's Disable-XP),
	// and the Fight Pit toggle has no known varbit, so their labels always show.
	CASTLE_WARS(
		-1,
		Set.of(9520, 9620),
		Set.of(NpcID.CASTLEWARS_JUDGE, NpcID.CASTLEWARS_JUDGE_MODEL),
		Set.of()),
	SOUL_WARS(
		VarbitID.SOUL_WARS_XP_DISABLE,
		Set.of(8493, 8749, 9005),
		Set.of(NpcID.SOUL_WARS_NOMAD, NpcID.SOUL_WARS_NOMAD_1OP, NpcID.SOUL_WARS_NOMAD_2OP),
		Set.of()),
	CLAN_WARS(
		VarbitID.CLAN_WARS_XP_DISABLE,
		Set.of(
			12621, 12622, 12623, 13130, 13131, 13133, 13134, 13135, 13386, 13387, 13390,
			13641, 13642, 13643, 13644, 13645, 13646, 13647, 13899, 13900, 14155, 14156),
		Set.of(),
		Set.of(
			ObjectID.CLANWARS_FFAPORTAL,
			ObjectID.CLANWARS_CHALLENGEPORTAL_W,
			ObjectID.CLANWARS_CHALLENGEPORTAL_S,
			ObjectID.CLANWARS_CHALLENGEPORTAL_E)),
	FIGHT_PIT(
		-1,
		Set.of(9552),
		Set.of(NpcID.TZHAAR_FIGHTPIT_MASTER),
		Set.of());

	static final String LABEL = "[SG] Disable XP";

	private final int xpDisabledVarbit;
	private final Set<Integer> arenaRegions;
	private final Set<Integer> toggleNpcs;
	private final Set<Integer> toggleObjects;

	XpToggleMinigame(int xpDisabledVarbit, Set<Integer> arenaRegions, Set<Integer> toggleNpcs, Set<Integer> toggleObjects)
	{
		this.xpDisabledVarbit = xpDisabledVarbit;
		this.arenaRegions = arenaRegions;
		this.toggleNpcs = toggleNpcs;
		this.toggleObjects = toggleObjects;
	}

	/** Varbit that reads 1 once the player has turned combat XP off, or -1 if Guard cannot read it. */
	public int getXpDisabledVarbit()
	{
		return xpDisabledVarbit;
	}

	Set<Integer> getToggleNpcs()
	{
		return toggleNpcs;
	}

	Set<Integer> getToggleObjects()
	{
		return toggleObjects;
	}

	static boolean isArenaRegion(int regionId)
	{
		for (XpToggleMinigame minigame : values())
		{
			if (minigame.arenaRegions.contains(regionId))
			{
				return true;
			}
		}
		return false;
	}

	static XpToggleMinigame forNpc(int npcId)
	{
		for (XpToggleMinigame minigame : values())
		{
			if (minigame.toggleNpcs.contains(npcId))
			{
				return minigame;
			}
		}
		return null;
	}

	static XpToggleMinigame forObject(int objectId)
	{
		for (XpToggleMinigame minigame : values())
		{
			if (minigame.toggleObjects.contains(objectId))
			{
				return minigame;
			}
		}
		return null;
	}
}
