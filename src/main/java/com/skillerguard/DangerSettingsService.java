package com.skillerguard;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Menu;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Preferences;
import net.runelite.api.SoundEffectVolume;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

@Singleton
public class DangerSettingsService
{
	enum Warning
	{
		AUTO_RETALIATE,
		NPC_ATTACK,
		PLAYER_ATTACK,
		ACCEPT_AID,
		LAMP_COMBAT_ONLY
	}

	/** Hidden is normally the last of the four Attack-option choices. */
	static final int ATTACK_OPTION_HIDDEN = 3;
	private static final Duration LOGIN_SILENCE = Duration.ofSeconds(5);

	/**
	 * Last Man Standing arena regions (Deserted Island, Wild Varrock). LMS has no dedicated
	 * varbit; RuneLite's own Discord Rich Presence plugin identifies these matches the same way.
	 */
	private static final Set<Integer> LMS_REGIONS = Set.of(
		13658, 13659, 13660, 13914, 13915, 13916,
		13918, 13919, 13920, 14174, 14175, 14176, 14430, 14431, 14432
	);

	/** Emir's Arena (PvP Arena); fights use preset stats, so no real combat XP is gained. */
	private static final Set<Integer> EMIRS_ARENA_REGIONS = Set.of(13362, 13363);

	private final Client client;
	private final ClientThread clientThread;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;
	private Set<Warning> knownWarnings = EnumSet.noneOf(Warning.class);
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
			state.setAcceptAidOn(false);
			notifyAlerts();
			return;
		}
		observeCurrentMenu();
		state.setAutoRetaliateOn(isAutoRetaliateOn());
		boolean playerAttackOn = isPlayerAttackOptionsOn();
		boolean npcAttackOn = isNpcAttackOptionsOn();
		int weaponId = wornWeaponId();
		state.setPlayerAttackOptionsOn(playerAttackOn);
		state.setNpcAttackOptionsOn(shouldWarnNpcAttack(npcAttackOn, weaponId));
		state.setAcceptAidOn(shouldWarnAcceptAid(
			playerAttackOn, npcAttackOn, weaponId, client.getVarbitValue(VarbitID.OPTION_ACCEPTAID)));
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
			knownWarnings = EnumSet.allOf(Warning.class);
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
			state.setNpcAttackOptionsOn(shouldWarnNpcAttack(sawAttack, wornWeaponId()));
			notifyAlerts();
		}
	}

	void notifyAlerts()
	{
		Set<Warning> warnings = activeWarnings(
			state.isAutoRetaliateOn(),
			state.isNpcAttackOptionsOn(),
			state.isPlayerAttackOptionsOn(),
			state.isAcceptAidOn(),
			state.isLampCombatOnly());
		if (warnings.isEmpty() || !activation.isActive() || !config.warnDangerousSettings()
			|| !config.dangerAlertMode().playSound()
			|| client.getGameState() != GameState.LOGGED_IN)
		{
			knownWarnings = EnumSet.noneOf(Warning.class);
			return;
		}

		Instant now = Instant.now();
		if (now.isBefore(ignoreSoundUntil))
		{
			knownWarnings = warnings;
			return;
		}
		boolean newWarning = hasNewWarning(knownWarnings, warnings);
		int interval = config.dangerSoundInterval();
		boolean remind = interval > 0
			&& lastSound != Instant.EPOCH
			&& Duration.between(lastSound, now).compareTo(Duration.ofSeconds(interval)) >= 0;
		knownWarnings = warnings;
		if (!newWarning && !remind)
		{
			return;
		}
		lastSound = now;
		playConfiguredSound();
	}

	static Set<Warning> activeWarnings(
		boolean autoRetaliate, boolean npcAttack, boolean playerAttack, boolean acceptAid, boolean lampCombatOnly)
	{
		Set<Warning> warnings = EnumSet.noneOf(Warning.class);
		if (autoRetaliate)
		{
			warnings.add(Warning.AUTO_RETALIATE);
		}
		if (npcAttack)
		{
			warnings.add(Warning.NPC_ATTACK);
		}
		if (playerAttack)
		{
			warnings.add(Warning.PLAYER_ATTACK);
		}
		if (acceptAid)
		{
			warnings.add(Warning.ACCEPT_AID);
		}
		if (lampCombatOnly)
		{
			warnings.add(Warning.LAMP_COMBAT_ONLY);
		}
		return warnings;
	}

	/** Equipping or unequipping a splash weapon swaps one warning for another in the same tick. */
	static boolean hasNewWarning(Set<Warning> known, Set<Warning> current)
	{
		return !known.containsAll(current);
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

	/**
	 * Official PvP / Deadman worlds, the Wilderness (which also covers Bounty Hunter, fought
	 * within the Wilderness since its 2023 rework), Last Man Standing and Emir's Arena, and the
	 * arenas of XP-toggle minigames always expose Attack on players.
	 */
	static boolean shouldWarnPlayerAttack(Collection<WorldType> worldTypes, boolean inWilderness, int regionId)
	{
		if (inWilderness
			|| LMS_REGIONS.contains(regionId)
			|| EMIRS_ARENA_REGIONS.contains(regionId)
			|| XpToggleMinigame.isArenaRegion(regionId))
		{
			return false;
		}
		return worldTypes == null
			|| !(WorldType.isPvpWorld(worldTypes) || worldTypes.contains(WorldType.PVP_ARENA));
	}

	/** A wielded splash weapon makes NPC Attack options harmless, so only that warning is muted. */
	static boolean shouldWarnNpcAttack(boolean optionsOn, int weaponId)
	{
		return optionsOn && !SafetyIds.isSplashWeapon(weaponId);
	}

	/**
	 * A Soulflame horn's Entice makes the next melee attack always hit, which defeats a splash
	 * weapon. Outside a few bosses and raids it only reaches players with Accept Aid on.
	 */
	static boolean shouldWarnAcceptAid(boolean playerAttackOn, boolean npcAttackOn, int weaponId, int acceptAidVarbit)
	{
		return (playerAttackOn || npcAttackOn) && acceptAidVarbit == 1 && SafetyIds.isSplashWeapon(weaponId);
	}

	private int wornWeaponId()
	{
		ItemContainer worn = client.getItemContainer(InventoryID.WORN);
		if (worn == null)
		{
			return -1;
		}
		Item weapon = worn.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		return weapon == null ? -1 : weapon.getId();
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
		if (!shouldWarnPlayerAttack(client.getWorldType(), isInWilderness(), currentRegionId()))
		{
			return false;
		}
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

	private boolean isInWilderness()
	{
		return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1;
	}

	private int currentRegionId()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return -1;
		}
		WorldPoint point = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation());
		return point == null ? -1 : point.getRegionID();
	}
}
