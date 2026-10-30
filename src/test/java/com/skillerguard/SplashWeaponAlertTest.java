package com.skillerguard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import org.junit.Before;
import org.junit.Test;

public class SplashWeaponAlertTest
{
	private static final int[] SPLASH_WEAPON_IDS = {11707, 20779, 24537, 27580, 27586};

	private final Client client = mock(Client.class);
	private final SkillerGuardConfig config = mock(SkillerGuardConfig.class);
	private final ItemContainer worn = mock(ItemContainer.class);
	private final GuardState state = new GuardState();
	private DangerSettingsService service;

	@Before
	public void setUp()
	{
		when(config.enabled()).thenReturn(true);
		when(config.activationMode()).thenReturn(ActivationMode.ALWAYS);
		when(config.warnDangerousSettings()).thenReturn(true);
		when(config.dangerAlertMode()).thenReturn(DangerAlertMode.BANNERS);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getItemContainer(InventoryID.WORN)).thenReturn(worn);
		// Player Attack Hidden tells the service which value means Hidden; NPC Attack is on.
		when(client.getVarpValue(VarPlayerID.OPTION_ATTACKPRIORITY)).thenReturn(3);
		when(client.getVarpValue(VarPlayerID.OPTION_ATTACKPRIORITY_NPC)).thenReturn(0);
		service = new DangerSettingsService(
			client, mock(ClientThread.class), config, new GuardActivation(client, config), state);
	}

	private void wield(int itemId)
	{
		Item weapon = itemId < 0 ? null : new Item(itemId, 1);
		when(worn.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(weapon);
	}

	private static MenuEntry[] attackOnCombatNpc()
	{
		NPC npc = mock(NPC.class);
		when(npc.getCombatLevel()).thenReturn(2);
		MenuEntry entry = mock(MenuEntry.class);
		when(entry.getNpc()).thenReturn(npc);
		when(entry.getOption()).thenReturn("Attack");
		return new MenuEntry[]{entry};
	}

	@Test
	public void gamevalConstantsAreTheItemsTheListNames()
	{
		assertEquals(11707, ItemID.GOBLINRAID_WARHAMMER);
		assertEquals(20779, ItemID.HW16_REWARD_KNIFE);
		assertEquals(24537, ItemID.EASTER20_CARROT_SWORD);
		assertEquals(27580, ItemID.XMAS22_NUTCRACKER_STAFF01);
		assertEquals(27586, ItemID.XMAS22_NUTCRACKER_STAFF02);
		assertEquals(SPLASH_WEAPON_IDS.length, SafetyIds.SPLASH_WEAPONS.size());
	}

	@Test
	public void refreshMutesEachSplashWeaponAndUnequipRestoresTheWarning()
	{
		for (int id : SPLASH_WEAPON_IDS)
		{
			wield(-1);
			service.refresh();
			assertTrue("empty hand warns before " + id, state.isNpcAttackOptionsOn());

			wield(id);
			service.refresh();
			assertFalse("wielding " + id + " mutes", state.isNpcAttackOptionsOn());

			wield(-1);
			service.refresh();
			assertTrue("unequipping " + id + " restores", state.isNpcAttackOptionsOn());
		}
	}

	@Test
	public void ordinaryWeaponStillWarns()
	{
		wield(ItemID.BRONZE_SWORD);
		service.refresh();
		assertTrue(state.isNpcAttackOptionsOn());
	}

	@Test
	public void noWornContainerStillWarns()
	{
		when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
		service.refresh();
		assertTrue(state.isNpcAttackOptionsOn());
	}

	@Test
	public void attackSeenInAMenuDoesNotBreakTheMute()
	{
		wield(ItemID.GOBLINRAID_WARHAMMER);
		service.observeMenu(attackOnCombatNpc());
		assertFalse(state.isNpcAttackOptionsOn());

		wield(-1);
		service.observeMenu(attackOnCombatNpc());
		assertTrue(state.isNpcAttackOptionsOn());
	}

	@Test
	public void splashWeaponLeavesAutoRetaliateWarningOn()
	{
		wield(ItemID.GOBLINRAID_WARHAMMER);
		when(client.getVarpValue(VarPlayerID.OPTION_NODEF)).thenReturn(0);
		service.refresh();
		assertTrue(state.isAutoRetaliateOn());
	}

	@Test
	public void splashWeaponLeavesPlayerAttackWarningOn()
	{
		wield(ItemID.GOBLINRAID_WARHAMMER);
		when(client.getPlayerOptions()).thenReturn(new String[]{"Attack"});
		service.refresh();
		assertTrue(state.isPlayerAttackOptionsOn());
	}
}
