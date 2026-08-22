package com.skillerguard;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Preferences;
import net.runelite.api.SoundEffectVolume;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

@Singleton
public class DangerSettingsService
{
	/** Hidden is normally the last of the four Attack-option choices. */
	static final int ATTACK_OPTION_HIDDEN = 3;
	private static final Duration LOGIN_SILENCE = Duration.ofSeconds(5);

	private final Client client;
	private final ClientThread clientThread;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;
	private boolean wasAlerting;
	private Instant lastSound = Instant.EPOCH;
	private Instant ignoreSoundUntil = Instant.EPOCH;
	private Boolean npcAttackFromMenu;
	private Instant npcMenuAt = Instant.EPOCH;

	@Inject
	DangerSettingsService(
		Client client,
		ClientThread clientThread,
		SkillerGuardConfig config,
		GuardActivation activation,
		GuardState state)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.config = config;
		this.activation = activation;
		this.state = state;
	}

	public void refresh()
	{
		if (!activation.isActive() || !config.warnDangerousSettings())
		{
			state.setAutoRetaliateOn(false);
			state.setNpcAttackOptionsOn(false);
			state.setPlayerAttackOptionsOn(false);
			notifyAlerts();
			return;
		}
		observeCurrentMenu();
		state.setAutoRetaliateOn(isAutoRetaliateOn());
		state.setPlayerAttackOptionsOn(isPlayerAttackOptionsOn());
		state.setNpcAttackOptionsOn(isNpcAttackOptionsOn());
		notifyAlerts();
	}

	public void onGameTick()
	{
		refresh();
	}

	public void onClientTick()
	{
		refresh();
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOGGED_IN || gameState == GameState.HOPPING)
		{
			Instant now = Instant.now();
			ignoreSoundUntil = now.plus(LOGIN_SILENCE);
			lastSound = now;
			wasAlerting = true;
		}
		refresh();
	}

	/**
	 * If a combat NPC is under the cursor, Attack present means options are on;
	 * Attack missing means they are Hidden.
	 */
	public void observeMenu(MenuEntry[] entries)
	{
		if (!activation.isActive() || !config.warnDangerousSettings() || entries == null)
		{
			return;
		}
		boolean sawCombatNpc = false;
		boolean sawAttack = false;
		for (MenuEntry entry : entries)
		{
			NPC npc = entry.getNpc();
			if (npc == null || npc.getCombatLevel() <= 0)
			{
				continue;
			}
			sawCombatNpc = true;
			if ("Attack".equalsIgnoreCase(MenuMatcher.strip(entry.getOption())))
			{
				sawAttack = true;
			}
		}
		if (sawCombatNpc)
		{
			npcAttackFromMenu = sawAttack;
			npcMenuAt = Instant.now();
			state.setNpcAttackOptionsOn(sawAttack);
			notifyAlerts();
		}
	}

	void notifyAlerts()
	{
		boolean alerting = state.isAutoRetaliateOn()
			|| state.isNpcAttackOptionsOn()
			|| state.isPlayerAttackOptionsOn()
			|| state.isLampCombatOnly();
		if (!alerting || !activation.isActive() || !config.warnDangerousSettings()
			|| !config.dangerAlertMode().playSound()
			|| client.getGameState() != GameState.LOGGED_IN)
		{
			wasAlerting = false;
			return;
		}

		Instant now = Instant.now();
		if (now.isBefore(ignoreSoundUntil))
		{
			wasAlerting = true;
			return;
		}
		boolean rising = !wasAlerting;
		int interval = config.dangerSoundInterval();
		boolean remind = interval > 0
			&& lastSound != Instant.EPOCH
			&& Duration.between(lastSound, now).compareTo(Duration.ofSeconds(interval)) >= 0;
		wasAlerting = true;
		if (!rising && !remind)
		{
			return;
		}
		lastSound = now;
		playConfiguredSound();
	}

	public void playConfiguredSound()
	{
		int percent = config.dangerSoundVolume();
		if (percent <= 0)
		{
			return;
		}
		int soundId = config.dangerSound().getId();
		int scaled = Math.max(1, percent * SoundEffectVolume.HIGH / 100);
		int copies = Math.max(1, (scaled + SoundEffectVolume.HIGH - 1) / SoundEffectVolume.HIGH);
		int volume = Math.min(SoundEffectVolume.HIGH, scaled);
		clientThread.invoke(() ->
		{
			Preferences prefs = client.getPreferences();
			int previous = prefs.getSoundEffectVolume();
			try
			{
				prefs.setSoundEffectVolume(volume);
				for (int i = 0; i < copies; i++)
				{
					client.playSoundEffect(soundId, volume);
				}
			}
			finally
			{
				prefs.setSoundEffectVolume(previous);
			}
		});
	}

	static boolean attackOptionsOn(int settingValue)
	{
		return settingValue != ATTACK_OPTION_HIDDEN;
	}

	private void observeCurrentMenu()
	{
		Menu menu = client.getMenu();
		if (menu == null)
		{
			return;
		}
		observeMenu(menu.getMenuEntries());
	}

	private boolean isNpcAttackOptionsOn()
	{
		if (npcAttackFromMenu != null && npcMenuAt != Instant.EPOCH
			&& Duration.between(npcMenuAt, Instant.now()).compareTo(Duration.ofSeconds(2)) < 0)
		{
			return npcAttackFromMenu;
		}
		Boolean fromWidget = attackOptionOnFromWidget(InterfaceID.SettingsSide.ATTACK_PRIORITY_NPC);
		if (fromWidget != null)
		{
			return fromWidget;
		}
		int hidden = hiddenAttackOptionValue();
		return client.getVarpValue(VarPlayerID.OPTION_ATTACKPRIORITY_NPC) != hidden;
	}

	/**
	 * When Player Attack options are Hidden, that varp value is the Hidden choice.
	 * NPC Attack options use the same numbering.
	 */
	private int hiddenAttackOptionValue()
	{
		if (!isPlayerAttackOptionsOn())
		{
			return client.getVarpValue(VarPlayerID.OPTION_ATTACKPRIORITY);
		}
		return ATTACK_OPTION_HIDDEN;
	}

	private Boolean attackOptionOnFromWidget(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		if (widget == null || widget.isHidden() || widget.getText() == null || widget.getText().isEmpty())
		{
			return null;
		}
		String text = Text.removeTags(widget.getText()).toLowerCase(Locale.ROOT);
		if (text.contains("hidden"))
		{
			return false;
		}
		if (text.contains("right-click") || text.contains("left-click") || text.contains("combat"))
		{
			return true;
		}
		return null;
	}

	private boolean isAutoRetaliateOn()
	{
		Widget text = client.getWidget(InterfaceID.CombatInterface.RETALIATE_TEXT);
		if (text != null && !text.isHidden() && text.getText() != null)
		{
			String t = Text.removeTags(text.getText()).toLowerCase(Locale.ROOT);
			if (t.contains("auto retaliate"))
			{
				return t.contains("(on)");
			}
		}
		return client.getVarpValue(VarPlayerID.OPTION_NODEF) == 0;
	}

	private boolean isPlayerAttackOptionsOn()
	{
		String[] options = client.getPlayerOptions();
		if (options == null)
		{
			return false;
		}
		for (String option : options)
		{
			if (option != null && option.equalsIgnoreCase("Attack"))
			{
				return true;
			}
		}
		return false;
	}
}
