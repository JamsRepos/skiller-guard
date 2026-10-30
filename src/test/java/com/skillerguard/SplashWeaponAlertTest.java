package com.skillerguard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.skillerguard.DangerSettingsService.Warning;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

public class SplashWeaponAlertTest
{
	private static final int[] SPLASH_WEAPON_IDS = {11707, 20779, 24537, 27580, 27586};
	private static final int NOTHING_WIELDED = -1;
	private static final int ACCEPT_AID_OFF = 0;
	private static final int ACCEPT_AID_ON = 1;

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
	public void goldCarrotSwordIsNotASplashWeapon()
	{
		// The '24-carat' sword looks the part, but its -10 strength bonus lets it hit and give XP.
		assertFalse(SafetyIds.isSplashWeapon(ItemID.EASTER20_CARROT_SWORD_GOLD));
	}

	@Test
	public void npcAttackIsMutedOnlyWhileWieldingASplashWeapon()
	{
		for (int id : SPLASH_WEAPON_IDS)
		{
			assertFalse("wielding " + id + " mutes", DangerSettingsService.shouldWarnNpcAttack(true, id));
		}
		assertTrue(DangerSettingsService.shouldWarnNpcAttack(true, NOTHING_WIELDED));
		assertTrue(DangerSettingsService.shouldWarnNpcAttack(true, ItemID.BRONZE_SWORD));
		assertTrue(DangerSettingsService.shouldWarnNpcAttack(true, ItemID.EASTER20_CARROT_SWORD_GOLD));
		assertFalse(DangerSettingsService.shouldWarnNpcAttack(false, NOTHING_WIELDED));
	}

	@Test
	public void acceptAidWarnsForEverySplashWeapon()
	{
		for (int id : SPLASH_WEAPON_IDS)
		{
			assertTrue("wielding " + id + " warns",
				DangerSettingsService.shouldWarnAcceptAid(false, true, id, ACCEPT_AID_ON));
		}
	}

	@Test
	public void acceptAidNeedsASplashWeaponWielded()
	{
		assertFalse(DangerSettingsService.shouldWarnAcceptAid(true, true, NOTHING_WIELDED, ACCEPT_AID_ON));
		assertFalse(DangerSettingsService.shouldWarnAcceptAid(true, true, ItemID.BRONZE_SWORD, ACCEPT_AID_ON));
	}

	@Test
	public void acceptAidOffDoesNotWarn()
	{
		assertFalse(DangerSettingsService.shouldWarnAcceptAid(true, true, ItemID.GOBLINRAID_WARHAMMER, ACCEPT_AID_OFF));
	}

	@Test
	public void acceptAidWarnsWithEitherKindOfAttackOption()
	{
		int hammer = ItemID.GOBLINRAID_WARHAMMER;
		assertTrue(DangerSettingsService.shouldWarnAcceptAid(true, false, hammer, ACCEPT_AID_ON));
		assertTrue(DangerSettingsService.shouldWarnAcceptAid(false, true, hammer, ACCEPT_AID_ON));
		assertFalse(DangerSettingsService.shouldWarnAcceptAid(false, false, hammer, ACCEPT_AID_ON));
	}

	@Test
	public void gamevalConstantIsTheAcceptAidVarbit()
	{
		assertEquals(4180, VarbitID.OPTION_ACCEPTAID);
	}

	@Test
	public void swappingOneWarningForAnotherIsANewWarning()
	{
		Set<Warning> npc = EnumSet.of(Warning.NPC_ATTACK);
		Set<Warning> aid = EnumSet.of(Warning.ACCEPT_AID);
		assertTrue("unequip", DangerSettingsService.hasNewWarning(aid, npc));
		assertTrue("equip", DangerSettingsService.hasNewWarning(npc, aid));
		assertTrue("another warning already showing", DangerSettingsService.hasNewWarning(
			EnumSet.of(Warning.AUTO_RETALIATE), EnumSet.of(Warning.AUTO_RETALIATE, Warning.NPC_ATTACK)));
		assertTrue("from nothing", DangerSettingsService.hasNewWarning(EnumSet.noneOf(Warning.class), npc));
	}

	@Test
	public void unchangedOrClearedWarningsAreNotNew()
	{
		Set<Warning> both = EnumSet.of(Warning.AUTO_RETALIATE, Warning.NPC_ATTACK);
		assertFalse(DangerSettingsService.hasNewWarning(both, both));
		assertFalse(DangerSettingsService.hasNewWarning(both, EnumSet.of(Warning.NPC_ATTACK)));
		assertFalse(DangerSettingsService.hasNewWarning(both, EnumSet.noneOf(Warning.class)));
	}

	@Test
	public void everyStateFlagMapsToItsWarning()
	{
		assertEquals(EnumSet.noneOf(Warning.class),
			DangerSettingsService.activeWarnings(false, false, false, false, false));
		assertEquals(EnumSet.of(Warning.AUTO_RETALIATE),
			DangerSettingsService.activeWarnings(true, false, false, false, false));
		assertEquals(EnumSet.of(Warning.NPC_ATTACK),
			DangerSettingsService.activeWarnings(false, true, false, false, false));
		assertEquals(EnumSet.of(Warning.PLAYER_ATTACK),
			DangerSettingsService.activeWarnings(false, false, true, false, false));
		assertEquals(EnumSet.of(Warning.ACCEPT_AID),
			DangerSettingsService.activeWarnings(false, false, false, true, false));
		assertEquals(EnumSet.of(Warning.LAMP_COMBAT_ONLY),
			DangerSettingsService.activeWarnings(false, false, false, false, true));
	}
}
