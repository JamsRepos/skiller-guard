package com.skillerguard;

import com.google.inject.Provides;
import com.skillerguard.overlay.DangerSettingsOverlay;
import com.skillerguard.overlay.NpcWarnOverlay;
import com.skillerguard.overlay.StatusOverlay;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Skiller Guard",
	description = "Protects skillers from accidental combat and Prayer XP: hides risky clicks, locks lamps, and warns if XP still slips through",
	tags = {"skiller", "skillers", "menu", "hide", "prayer", "safety", "level 3", "lamp"}
)
public class SkillerGuardPlugin extends Plugin
{
	@Inject
	private SkillerGuardConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private StatusOverlay statusOverlay;

	@Inject
	private DangerSettingsOverlay dangerSettingsOverlay;

	@Inject
	private NpcWarnOverlay npcWarnOverlay;

	@Inject
	private MenuSafetyService menuSafetyService;

	@Inject
	private LampLockoutService lampLockoutService;

	@Inject
	private QuestWarnService questWarnService;

	@Inject
	private CombatXpAlert combatXpAlert;

	@Inject
	private DangerSettingsService dangerSettingsService;

	@Inject
	private WorldWarnTracker worldWarnTracker;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Client client;

	@Override
	protected void startUp()
	{
		overlayManager.add(statusOverlay);
		overlayManager.add(dangerSettingsOverlay);
		overlayManager.add(npcWarnOverlay);
		clientThread.invoke(() ->
		{
			try
			{
				worldWarnTracker.scanOnce();
				combatXpAlert.refreshGear();
				dangerSettingsService.refresh();
			}
			catch (Exception ex)
			{
				log.debug("Skiller Guard startup scan failed", ex);
			}
		});
		lampLockoutService.refresh();
		log.info("Skiller Guard started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(statusOverlay);
		overlayManager.remove(dangerSettingsOverlay);
		overlayManager.remove(npcWarnOverlay);
		lampLockoutService.restore();
		menuSafetyService.reset();
		questWarnService.reset();
		combatXpAlert.reset();
		worldWarnTracker.clear();
		log.info("Skiller Guard stopped");
	}

	@Subscribe
	public void onPostMenuSort(PostMenuSort event)
	{
		if (client.getMenu() != null)
		{
			dangerSettingsService.observeMenu(client.getMenu().getMenuEntries());
		}
		menuSafetyService.onPostMenuSort(event);
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		dangerSettingsService.observeMenu(event.getMenuEntries());
		menuSafetyService.onMenuOpened(event);
		questWarnService.onMenuOpened(event);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		lampLockoutService.onMenuOptionClicked(event);
		if (!event.isConsumed())
		{
			menuSafetyService.onMenuOptionClicked(event);
		}
		questWarnService.onMenuOptionClicked(event);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		lampLockoutService.onWidgetLoaded(event);
		questWarnService.onWidgetLoaded(event);
		int group = event.getGroupId();
		if (group == InterfaceID.SETTINGS
			|| group == InterfaceID.SETTINGS_SIDE
			|| group == InterfaceID.COMBAT_INTERFACE
			|| group == InterfaceID.XPREWARD)
		{
			dangerSettingsService.refresh();
			dangerSettingsService.notifyAlerts();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		combatXpAlert.onStatChanged(event);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		combatXpAlert.onItemContainerChanged(event);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		combatXpAlert.onGameStateChanged(event);
		worldWarnTracker.onGameStateChanged(event);
		questWarnService.onGameStateChanged(event);
		dangerSettingsService.onGameStateChanged(event);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		dangerSettingsService.onClientTick();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		questWarnService.onGameTick();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		questWarnService.onChatMessage(event);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varp = event.getVarpId();
		if (varp == VarPlayerID.OPTION_NODEF
			|| varp == VarPlayerID.OPTION_ATTACKPRIORITY
			|| varp == VarPlayerID.OPTION_ATTACKPRIORITY_NPC)
		{
			dangerSettingsService.refresh();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SkillerGuardConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		dangerSettingsService.refresh();
		lampLockoutService.refresh();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		worldWarnTracker.onNpcSpawned(event);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		worldWarnTracker.onNpcDespawned(event);
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		worldWarnTracker.onNpcChanged(event);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		worldWarnTracker.onGameObjectSpawned(event);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		worldWarnTracker.onGameObjectDespawned(event);
	}

	@Provides
	SkillerGuardConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillerGuardConfig.class);
	}
}
