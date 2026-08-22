package com.skillerguard;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.api.ChatMessageType;

@Singleton
public class MenuSafetyService
{
	private static final Duration WARN_COOLDOWN = Duration.ofSeconds(8);

	private final Client client;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;
	private final ChatMessageManager chatMessageManager;
	private final Map<String, Instant> lastWarn = new HashMap<>();

	@Inject
	MenuSafetyService(
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

	public void onPostMenuSort(PostMenuSort event)
	{
		if (!activation.isActive() || client.isMenuOpen())
		{
			return;
		}
		filterMenu();
	}

	public void onMenuOpened(MenuOpened event)
	{
		if (!activation.isActive())
		{
			return;
		}
		MenuEntry[] filtered = filter(event.getMenuEntries());
		if (filtered.length != event.getMenuEntries().length)
		{
			event.setMenuEntries(filtered);
		}
		addRightClickWarnings(filtered);
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!activation.isActive())
		{
			return;
		}
		MenuEntry entry = event.getMenuEntry();
		if (entry != null && isPlayerEntry(entry))
		{
			return;
		}
		SafetyRule hide = findHide(event.getMenuOption(), event.getMenuTarget());
		if (hide != null)
		{
			event.consume();
			chat("Blocked: " + hide.getReason());
			state.warn(hide.getReason());
		}
	}

	private void filterMenu()
	{
		Menu menu = client.getMenu();
		if (menu == null)
		{
			return;
		}
		MenuEntry[] current = menu.getMenuEntries();
		MenuEntry[] filtered = filter(current);
		if (filtered.length != current.length)
		{
			menu.setMenuEntries(filtered);
		}
	}

	MenuEntry[] filter(MenuEntry[] entries)
	{
		List<MenuEntry> kept = new ArrayList<>(entries.length);
		List<MenuEntry> protectedEntries = new ArrayList<>();
		for (MenuEntry entry : entries)
		{
			if (isProtected(entry))
			{
				protectedEntries.add(entry);
				kept.add(entry);
				continue;
			}
			if (findHide(entry.getOption(), entry.getTarget()) == null)
			{
				kept.add(entry);
			}
		}
		if (kept.isEmpty())
		{
			return protectedEntries.toArray(new MenuEntry[0]);
		}
		return kept.toArray(new MenuEntry[0]);
	}

	/**
	 * XP-trap warnings go on the right-click menu instead of chat, so hovering does not spam.
	 */
	private void addRightClickWarnings(MenuEntry[] entries)
	{
		Set<String> reasons = new LinkedHashSet<>();
		for (MenuEntry entry : entries)
		{
			if (isProtected(entry))
			{
				continue;
			}
			SafetyRule warn = findWarn(entry.getOption(), entry.getTarget());
			if (warn != null)
			{
				reasons.add(warn.getReason());
			}
		}
		for (String reason : reasons)
		{
			String message = reason;
			client.createMenuEntry(-1)
				.setOption("[SG] " + message)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					rateLimitedChat("Careful: " + message, message);
					state.warn(message);
				});
		}
	}

	private boolean isProtected(MenuEntry entry)
	{
		if (isPlayerEntry(entry))
		{
			return true;
		}
		return MenuMatcher.isProtectedOption(entry.getOption());
	}

	private boolean isPlayerEntry(MenuEntry entry)
	{
		if (entry.getPlayer() != null)
		{
			return true;
		}
		MenuAction type = entry.getType();
		return type == MenuAction.PLAYER_FIRST_OPTION
			|| type == MenuAction.PLAYER_SECOND_OPTION
			|| type == MenuAction.PLAYER_THIRD_OPTION
			|| type == MenuAction.PLAYER_FOURTH_OPTION
			|| type == MenuAction.PLAYER_FIFTH_OPTION
			|| type == MenuAction.PLAYER_SIXTH_OPTION
			|| type == MenuAction.PLAYER_SEVENTH_OPTION
			|| type == MenuAction.PLAYER_EIGHTH_OPTION
			|| type == MenuAction.WIDGET_TARGET_ON_PLAYER
			|| type == MenuAction.ITEM_USE_ON_PLAYER;
	}

	private SafetyRule findHide(String option, String target)
	{
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			if (!packEnabled(rule) || !shouldHide(rule) || !rule.matches(option, target))
			{
				continue;
			}
			if ("Cast".equalsIgnoreCase(MenuMatcher.strip(option)) && SafetyCatalog.isZeroXpSpell(target))
			{
				continue;
			}
			return rule;
		}
		return null;
	}

	private SafetyRule findWarn(String option, String target)
	{
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			if (!packEnabled(rule) || !rule.matches(option, target))
			{
				continue;
			}
			if (shouldHide(rule))
			{
				continue;
			}
			if (rule.getAction() == SafetyAction.WARN || rule.getCategory() == SafetyCategory.XP_TRAP)
			{
				return rule;
			}
		}
		return null;
	}

	private boolean shouldHide(SafetyRule rule)
	{
		if (rule.isAlwaysHide())
		{
			return true;
		}
		if (rule.getAction() == SafetyAction.HIDE)
		{
			return true;
		}
		return rule.getCategory() == SafetyCategory.XP_TRAP
			&& config.xpTrapMode() == XpTrapMode.HIDE_AND_WARN;
	}

	private boolean packEnabled(SafetyRule rule)
	{
		switch (rule.getCategory())
		{
			case PRAYER:
				return config.hidePrayerXp();
			case COMBAT_TRAINING:
				return config.hideCombatTraining();
			case NPC_MISCLICK:
				return config.hideNpcMisclicks();
			case XP_TRAP:
				return config.xpTrapProtection();
			default:
				return false;
		}
	}

	private void rateLimitedChat(String message, String key)
	{
		Instant now = Instant.now();
		Instant last = lastWarn.get(key);
		if (last != null && Duration.between(last, now).compareTo(WARN_COOLDOWN) < 0)
		{
			return;
		}
		lastWarn.put(key, now);
		chat(message);
	}

	private void chat(String message)
	{
		String formatted = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("[SG] " + message)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(formatted)
			.build());
	}

	void reset()
	{
		lastWarn.clear();
	}
}
