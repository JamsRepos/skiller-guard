package com.skillerguard;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.util.Text;

@Singleton
public class QuestWarnService
{
	private static final Duration BANNER_HOLD = Duration.ofSeconds(25);
	private static final Duration LOGIN_SILENCE = Duration.ofSeconds(5);
	private static final Color CHAT_RED = new Color(255, 40, 40);

	private final Client client;
	private final ClientThread clientThread;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;
	private final ChatMessageManager chatMessageManager;
	private final DangerSettingsService dangerSettingsService;
	private final Set<String> warned = new HashSet<>();
	private final Set<String> helperSeen = new HashSet<>();
	private boolean helperScanQueued;
	private boolean helperBaselinePending;
	private Instant ignoreAutoUntil = Instant.EPOCH;

	@Inject
	QuestWarnService(
		Client client,
		ClientThread clientThread,
		SkillerGuardConfig config,
		GuardActivation activation,
		GuardState state,
		ChatMessageManager chatMessageManager,
		DangerSettingsService dangerSettingsService)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.config = config;
		this.activation = activation;
		this.state = state;
		this.chatMessageManager = chatMessageManager;
		this.dangerSettingsService = dangerSettingsService;
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOGGED_IN || gameState == GameState.HOPPING)
		{
			ignoreAutoUntil = Instant.now().plus(LOGIN_SILENCE);
			helperBaselinePending = true;
			helperSeen.clear();
		}
	}

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (!activation.isActive() || !config.questWarnings() || isLoginSilence())
		{
			return;
		}
		int group = event.getGroupId();
		if (group != InterfaceID.QUESTJOURNAL
			&& group != InterfaceID.QUESTJOURNAL_OVERVIEW
			&& group != InterfaceID.QUESTSCROLL
			&& group != InterfaceID.QUESTDISPLAY
			&& group != InterfaceID.QUESTLIST)
		{
			return;
		}
		clientThread.invokeLater(this::checkOpenJournal);
	}

	public void onGameTick()
	{
		if (!activation.isActive() || !config.questWarnings() || isLoginSilence())
		{
			return;
		}
		checkOpenJournal();
		if (config.warnInProgressQuests())
		{
			checkInProgressQuests();
		}
		checkQuestHelperSidebar();
	}

	public void onMenuOpened(MenuOpened event)
	{
		if (!activation.isActive() || !config.questWarnings())
		{
			return;
		}
		for (MenuEntry entry : event.getMenuEntries())
		{
			if (isQuestHelperStartOption(entry.getOption()))
			{
				warnTitle(entry.getTarget());
			}
		}
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!activation.isActive() || !config.questWarnings())
		{
			return;
		}
		String option = MenuMatcher.strip(event.getMenuOption());
		String target = MenuMatcher.strip(event.getMenuTarget());
		if (option.equalsIgnoreCase("Read journal:") || isQuestHelperStartOption(option))
		{
			warnTitle(target);
		}
	}

	public void onChatMessage(ChatMessage event)
	{
		if (!activation.isActive() || !config.questWarnings())
		{
			return;
		}
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		String message = Text.removeTags(event.getMessage());
		String prefix = "You've started a new quest: ";
		int at = message.indexOf(prefix);
		if (at < 0)
		{
			return;
		}
		warnTitle(message.substring(at + prefix.length()).trim());
	}

	public void checkOpenJournal()
	{
		if (!activation.isActive() || !config.questWarnings())
		{
			return;
		}
		warnTitle(widgetText(InterfaceID.Questjournal.TITLE));
		warnTitle(widgetText(InterfaceID.QuestjournalOverview.TITLE));
		warnTitle(widgetText(InterfaceID.Questdisplay.TITLE));
		warnTitle(widgetText(InterfaceID.Questscroll.QUEST_TITLE));
	}

	private void checkInProgressQuests()
	{
		for (Quest quest : Quest.values())
		{
			if (quest.getState(client) != QuestState.IN_PROGRESS)
			{
				continue;
			}
			if (QuestDenylist.reasonFor(quest) == null)
			{
				continue;
			}
			warnTitle(quest.getName());
		}
	}

	static boolean isQuestAlreadyStarted(Client client, String title)
	{
		String key = QuestDenylist.normalize(plainText(title));
		if (key.isEmpty())
		{
			return false;
		}
		for (Quest quest : Quest.values())
		{
			if (!QuestDenylist.normalize(quest.getName()).equals(key))
			{
				continue;
			}
			QuestState state = quest.getState(client);
			return state == QuestState.IN_PROGRESS || state == QuestState.FINISHED;
		}
		return false;
	}

	private boolean isQuestAlreadyStarted(String title)
	{
		return isQuestAlreadyStarted(client, title);
	}

	private void checkQuestHelperSidebar()
	{
		if (helperScanQueued)
		{
			return;
		}
		helperScanQueued = true;
		SwingUtilities.invokeLater(() ->
		{
			try
			{
				List<String> titles = findOpenQuestHelperQuests();
				clientThread.invoke(() -> applyQuestHelperTitles(titles));
			}
			finally
			{
				helperScanQueued = false;
			}
		});
	}

	private void applyQuestHelperTitles(List<String> titles)
	{
		boolean baseline = helperBaselinePending;
		for (String title : titles)
		{
			String key = QuestDenylist.normalize(plainText(title));
			if (key.isEmpty())
			{
				continue;
			}
			if (baseline)
			{
				helperSeen.add(key);
				continue;
			}
			if (!helperSeen.add(key))
			{
				continue;
			}
			if (!config.warnInProgressQuests() && isQuestAlreadyStarted(title))
			{
				continue;
			}
			warnTitle(title);
		}
		helperBaselinePending = false;
	}

	private boolean isLoginSilence()
	{
		return Instant.now().isBefore(ignoreAutoUntil);
	}

	private void warnTitle(String title)
	{
		if (title == null || title.isEmpty())
		{
			return;
		}
		String cleaned = plainText(title);
		if (cleaned.isEmpty()
			|| cleaned.equalsIgnoreCase("Quest Journal")
			|| cleaned.equalsIgnoreCase("Quest List")
			|| cleaned.equalsIgnoreCase("Quests")
			|| cleaned.equalsIgnoreCase("Quest Helper"))
		{
			return;
		}
		String key = QuestDenylist.normalize(cleaned);
		if (warned.contains(key))
		{
			return;
		}
		String reason = QuestDenylist.reasonForTitle(cleaned);
		if (reason == null)
		{
			return;
		}
		warned.add(key);
		String message = cleaned + " — " + reason;
		state.warn(message);
		state.questAlert(message, Instant.now().plus(BANNER_HOLD));
		dangerSettingsService.playConfiguredSound();

		String header = "[SG] DANGEROUS QUEST: " + cleaned;
		String body = "This quest gives combat or Prayer XP (" + reason
			+ "). Close the journal or Quest Helper — finishing it can ruin a skiller.";
		chat(header);
		chat(body);
	}

	private void chat(String message)
	{
		String formatted = new ChatMessageBuilder()
			.append(CHAT_RED, message)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(formatted)
			.build());
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(formatted)
			.build());
	}

	private String widgetText(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		if (widget == null || widget.isHidden() || widget.getText() == null)
		{
			return null;
		}
		return widget.getText();
	}

	static boolean isQuestHelperStartOption(String option)
	{
		String o = MenuMatcher.strip(option).toLowerCase(Locale.ROOT);
		if (o.startsWith("stop"))
		{
			return false;
		}
		return o.contains("quest helper") || o.equals("start helper");
	}

	static String plainText(String title)
	{
		if (title == null)
		{
			return "";
		}
		return Text.removeTags(title)
			.replaceAll("(?i)</?html>", "")
			.replaceAll("(?i)<br\\s*/?>", " ")
			.replace('\n', ' ')
			.trim();
	}

	static List<String> findOpenQuestHelperQuests()
	{
		ScanResult scan = new ScanResult();
		for (Frame frame : Frame.getFrames())
		{
			scanComponent(frame, scan);
		}
		if (!scan.helperOpen)
		{
			return List.of();
		}
		return scan.titles;
	}

	private static void scanComponent(Component component, ScanResult scan)
	{
		if (component == null || !component.isVisible() || component instanceof Canvas)
		{
			return;
		}
		String text = textOf(component);
		if (text != null)
		{
			String cleaned = plainText(text);
			if (cleaned.equalsIgnoreCase("Open RuneScape Wiki"))
			{
				scan.helperOpen = true;
			}
			else if (QuestDenylist.reasonForTitle(cleaned) != null)
			{
				scan.titles.add(cleaned);
			}
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				scanComponent(child, scan);
			}
		}
	}

	private static String textOf(Component component)
	{
		if (component instanceof JLabel)
		{
			return ((JLabel) component).getText();
		}
		if (component instanceof AbstractButton)
		{
			return ((AbstractButton) component).getText();
		}
		if (component instanceof JTextArea)
		{
			return ((JTextArea) component).getText();
		}
		return null;
	}

	private static final class ScanResult
	{
		private boolean helperOpen;
		private final List<String> titles = new ArrayList<>();
	}

	void reset()
	{
		warned.clear();
		helperSeen.clear();
		helperBaselinePending = true;
		ignoreAutoUntil = Instant.EPOCH;
	}
}
