package com.skillerguard;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;

@Singleton
public class GuardActivation
{
	private final Client client;
	private final SkillerGuardConfig config;

	@Inject
	GuardActivation(Client client, SkillerGuardConfig config)
	{
		this.client = client;
		this.config = config;
	}

	public boolean isActive()
	{
		if (!config.enabled() || client.getGameState() != GameState.LOGGED_IN || isOnExemptWorld())
		{
			return false;
		}
		if (config.activationMode() == ActivationMode.ALWAYS)
		{
			return true;
		}
		return isLevel3Account();
	}

	/** Whether the current world is one Guard does not run on at all. */
	public boolean isOnExemptWorld()
	{
		return isExemptWorld(client.getWorldType());
	}

	/**
	 * Seasonal worlds (Leagues, Deadman seasons) always reset, so nothing there threatens the
	 * player's real account; Guard has nothing useful to protect.
	 */
	static boolean isExemptWorld(Collection<WorldType> worldTypes)
	{
		return worldTypes != null && worldTypes.contains(WorldType.SEASONAL);
	}

	public boolean isLevel3Account()
	{
		if (client.getRealSkillLevel(Skill.HITPOINTS) > 10)
		{
			return false;
		}
		return client.getRealSkillLevel(Skill.ATTACK) <= 3
			&& client.getRealSkillLevel(Skill.STRENGTH) <= 3
			&& client.getRealSkillLevel(Skill.DEFENCE) <= 3
			&& client.getRealSkillLevel(Skill.RANGED) <= 3
			&& client.getRealSkillLevel(Skill.MAGIC) <= 3
			&& client.getRealSkillLevel(Skill.PRAYER) <= 3;
	}

	static boolean isLevel3(int hp, int attack, int strength, int defence, int ranged, int magic, int prayer)
	{
		return hp <= 10
			&& attack <= 3
			&& strength <= 3
			&& defence <= 3
			&& ranged <= 3
			&& magic <= 3
			&& prayer <= 3;
	}
}
