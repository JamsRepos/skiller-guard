package com.skillerguard;

import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

/** Tells the player once per login/hop why Guard has stood down on a Seasonal or Deadman world. */
@Singleton
public class WorldExemptionNotice
{
	private static final Color CHAT_ORANGE = new Color(255, 170, 0);

	private final GuardActivation activation;
	private final SkillerGuardConfig config;
	private final ChatMessageManager chatMessageManager;

	@Inject
	WorldExemptionNotice(GuardActivation activation, SkillerGuardConfig config, ChatMessageManager chatMessageManager)
	{
		this.activation = activation;
		this.config = config;
		this.chatMessageManager = chatMessageManager;
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN || !config.enabled() || !activation.isOnExemptWorld())
		{
			return;
		}
		chat("[SG] Guard is off on this world. Seasonal and Deadman worlds use a character separate"
			+ " from your main account, so there is nothing here for Guard to protect.");
	}

	private void chat(String message)
	{
		String formatted = new ChatMessageBuilder()
			.append(CHAT_ORANGE, message)
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
}
