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
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ObjectComposition;
import net.runelite.api.widgets.Widget;

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
		SafetyRule hide = findHide(entry);
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
			if (findHide(entry) == null)
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
			SafetyRule warn = findWarn(entry);
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
		return SafetyIds.isWalkOrCancel(entry.getType()) || SafetyIds.isExamine(entry.getType());
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

	private SafetyRule findHide(MenuEntry entry)
	{
		if (entry == null)
		{
			return null;
		}
		if (config.hideCombatTraining())
		{
			switch (SafetyCatalog.spellbookOp(entry))
			{
				case HIDE:
					return SafetyCatalog.SPELLBOOK_HIDE;
				case KEEP:
					return null;
				default:
					break;
			}
			if (SafetyCatalog.isSpellTargetAction(entry.getType())
				&& SafetyCatalog.spellbookOp(client.getSelectedWidget()) == SafetyCatalog.SpellbookOp.HIDE)
			{
				return SafetyCatalog.SPELLBOOK_HIDE;
			}
			if (SafetyCatalog.isTabletCraftXpOp(entry.getWidget()))
			{
				return SafetyCatalog.COMBAT_LECTERN;
			}
		}
		SafetyRule hide = SafetyCatalog.hideRule(
			entry, selectedItemId(), resolvedObjectId(entry), hasHarpoon());
		if (hide != null && packEnabled(hide) && shouldHide(hide))
		{
			return hide;
		}
		return null;
	}

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.TELETABS_CRAFT_IF)
		{
			return;
		}
		hideTabletCraftButtons(activation.isActive() && config.hideCombatTraining());
	}

	public void hideTabletCraftButtons(boolean hide)
	{
		for (int componentId : SafetyCatalog.tabletCraftWidgets())
		{
			Widget widget = client.getWidget(componentId);
			if (widget != null)
			{
				widget.setHidden(hide);
			}
		}
	}

	private boolean hasHarpoon()
	{
		return containsHarpoon(client.getItemContainer(InventoryID.INV))
			|| containsHarpoon(client.getItemContainer(InventoryID.WORN));
	}

	private static boolean containsHarpoon(ItemContainer container)
	{
		if (container == null)
		{
			return false;
		}
		for (Item item : container.getItems())
		{
			if (item != null && SafetyIds.isHarpoon(item.getId()))
			{
				return true;
			}
		}
		return false;
	}

	private int selectedItemId()
	{
		Widget widget = client.getSelectedWidget();
		if (widget == null)
		{
			return -1;
		}
		return widget.getItemId();
	}

	/**
	 * POH furniture is often a hotspot whose real altar id is the impostor.
	 */
	private int resolvedObjectId(MenuEntry entry)
	{
		MenuAction type = entry.getType();
		if (!SafetyIds.isObjectOp(type)
			&& type != MenuAction.ITEM_USE_ON_GAME_OBJECT
			&& type != MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
		{
			return -1;
		}
		int id = entry.getIdentifier();
		try
		{
			ObjectComposition def = client.getObjectDefinition(id);
			if (def != null && def.getImpostorIds() != null)
			{
				ObjectComposition impostor = def.getImpostor();
				if (impostor != null)
				{
					return impostor.getId();
				}
			}
		}
		catch (Exception ignored)
		{
			// Impostor lookup needs varbits that are not always present.
		}
		return id;
	}

	private SafetyRule findWarn(MenuEntry entry)
	{
		SafetyRule warn = SafetyCatalog.warnRule(entry);
		if (warn == null || !packEnabled(warn) || shouldHide(warn))
		{
			return null;
		}
		return warn;
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
		hideTabletCraftButtons(false);
	}
}
