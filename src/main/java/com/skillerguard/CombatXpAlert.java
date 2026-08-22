package com.skillerguard;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.api.ChatMessageType;

@Singleton
public class CombatXpAlert
{
	private static final Duration LOGIN_DEBOUNCE = Duration.ofSeconds(3);
	private static final Duration BREACH_HOLD = Duration.ofSeconds(20);

	private final Client client;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;
	private final ChatMessageManager chatMessageManager;
	private Instant ignoreXpUntil = Instant.EPOCH;
	private final Map<Skill, Integer> lastXp = new EnumMap<>(Skill.class);

	@Inject
	CombatXpAlert(
		Client client,
		SkillerGuardConfig config,
		GuardActivation activation,
		GuardState state,
		ChatMessageManager chatMessageManager)
	{
		this.client = client;
		this.config = config;
		this.activation = activation;
		this.state = state;
		this.chatMessageManager = chatMessageManager;
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN || event.getGameState() == GameState.HOPPING)
		{
			ignoreXpUntil = Instant.now().plus(LOGIN_DEBOUNCE);
			lastXp.clear();
		}
	}

	public void onStatChanged(StatChanged event)
	{
		Skill skill = event.getSkill();
		if (!CombatSkills.isCombat(skill))
		{
			return;
		}
		int xp = event.getXp();
		Integer previous = lastXp.put(skill, xp);
		if (!activation.isActive() || !config.warnOnCombatXp())
		{
			return;
		}
		if (Instant.now().isBefore(ignoreXpUntil) || previous == null || xp <= previous)
		{
			return;
		}
		String message = skill.getName() + " XP gained while Guard is active (" + (xp - previous) + ")";
		state.breach(message, Instant.now().plus(BREACH_HOLD));
		client.playSoundEffect(SoundEffectID.GE_ADD_OFFER_DINGALING);
		String formatted = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("[SG] BREACH: " + message)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(formatted)
			.build());
	}

	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.WORN)
		{
			return;
		}
		state.setPassivePrayerGear(wearingPassivePrayerXp());
	}

	public void refreshGear()
	{
		state.setPassivePrayerGear(wearingPassivePrayerXp());
	}

	private boolean wearingPassivePrayerXp()
	{
		ItemContainer worn = client.getItemContainer(InventoryID.WORN);
		if (worn == null)
		{
			return false;
		}
		for (Item item : worn.getItems())
		{
			if (item == null)
			{
				continue;
			}
			int id = item.getId();
			if (id == ItemID.BONECRUSHER
				|| id == ItemID.BONECRUSHER_NECKLACE
				|| id == ItemID.ASH_SANCTIFIER
				|| id == ItemID.SOUL_WARS_ECTOPLASMATOR)
			{
				return true;
			}
		}
		return false;
	}

	void reset()
	{
		ignoreXpUntil = Instant.EPOCH;
		lastXp.clear();
	}
}
