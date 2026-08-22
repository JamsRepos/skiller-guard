package com.skillerguard;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;

@Singleton
public class ChangelogService
{
	private final Client client;
	private final SkillerGuardConfig config;
	private final ConfigManager configManager;
	private final ChatMessageManager chatMessageManager;
	private boolean announced;

	@Inject
	ChangelogService(
		Client client,
		SkillerGuardConfig config,
		ConfigManager configManager,
		ChatMessageManager chatMessageManager)
	{
		this.client = client;
		this.config = config;
		this.configManager = configManager;
		this.chatMessageManager = chatMessageManager;
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			maybeAnnounce();
		}
	}

	public void maybeAnnounce()
	{
		if (announced || !config.enabled() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		if (!Changelog.isUnseen(config.seenChangelogVersion()))
		{
			announced = true;
			return;
		}
		announced = true;
		chat("Skiller Guard " + Changelog.VERSION + " — what's new:");
		for (String note : Changelog.NOTES)
		{
			chat("• " + note);
		}
		configManager.setConfiguration(SkillerGuardConfig.GROUP, SkillerGuardConfig.SEEN_CHANGELOG_VERSION_KEY, Changelog.VERSION);
	}

	public void reset()
	{
		announced = false;
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
}
