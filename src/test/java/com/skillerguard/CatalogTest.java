package com.skillerguard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CatalogTest
{
	@Test
	public void buryAndScatterCoverAllRemains()
	{
		boolean bury = false;
		boolean scatter = false;
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			if (rule.matches("Bury", "Dragon bones") && rule.getAction() == SafetyAction.HIDE)
			{
				bury = true;
			}
			if (rule.matches("Scatter", "Abyssal ashes") && rule.getAction() == SafetyAction.HIDE)
			{
				scatter = true;
			}
		}
		assertTrue(bury);
		assertTrue(scatter);
	}

	@Test
	public void sarcophagusAndMemoirsAreHidden()
	{
		boolean sarc = false;
		boolean memorial = false;
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			if (rule.matches("Open", "Sarcophagus"))
			{
				sarc = true;
				assertEquals(SafetyAction.HIDE, rule.getAction());
			}
			if (rule.isAlwaysHide() && rule.matches("Inspect", "Old Memorial"))
			{
				memorial = true;
			}
		}
		assertTrue(sarc);
		assertTrue(memorial);
	}

	@Test
	public void walkHereDoesNotMatchPrayerHides()
	{
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			assertFalse("Walk here matched " + rule, rule.matches("Walk here", "Bones"));
			assertFalse(rule.matches("Cancel", "Bones"));
		}
	}

	@Test
	public void reminisceIsNotHidden()
	{
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			assertFalse(rule.matches("Reminisce", "Kharedst's memoirs"));
		}
	}

	@Test
	public void everyRuleHasAReason()
	{
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			assertNotNull(rule.getReason());
			assertFalse(rule.getReason().isEmpty());
		}
	}

	@Test
	public void questNormalizeStripsThe()
	{
		assertEquals(QuestDenylist.normalize("The Restless Ghost"),
			QuestDenylist.normalize("Restless Ghost"));
		assertNotNull(QuestDenylist.reasonForTitle("The Restless Ghost"));
		assertNotNull(QuestDenylist.reasonForTitle("Waterfall Quest"));
		assertNull(QuestDenylist.reasonForTitle("Cook's Assistant"));
	}

	@Test
	public void overheadNamedNpcsOnly()
	{
		assertEquals("[SG] Atk+Str XP", NamedNpcCatalog.labelFor("Tamayu"));
		assertEquals("[SG] lowest skill", NamedNpcCatalog.labelFor("Juna"));
		assertNull(NamedNpcCatalog.labelFor("Man"));
		assertNull(NamedNpcCatalog.labelFor("Woman"));
	}

	@Test
	public void autoLevel3Bounds()
	{
		assertTrue(GuardActivation.isLevel3(10, 1, 1, 1, 1, 1, 1));
		assertTrue(GuardActivation.isLevel3(10, 3, 3, 3, 3, 3, 3));
		assertFalse(GuardActivation.isLevel3(11, 1, 1, 1, 1, 1, 1));
		assertFalse(GuardActivation.isLevel3(10, 4, 1, 1, 1, 1, 1));
		assertFalse(GuardActivation.isLevel3(99, 99, 99, 99, 99, 99, 99));
	}

	@Test
	public void hiddenAttackOptionIsTheSafeChoice()
	{
		assertFalse(DangerSettingsService.attackOptionsOn(DangerSettingsService.ATTACK_OPTION_HIDDEN));
		assertTrue(DangerSettingsService.attackOptionsOn(0));
		assertTrue(DangerSettingsService.attackOptionsOn(1));
		assertTrue(DangerSettingsService.attackOptionsOn(2));
	}

	@Test
	public void dangerAlertModeCanBeVisualSoundOrBoth()
	{
		assertTrue(DangerAlertMode.BANNERS.showBanners());
		assertFalse(DangerAlertMode.BANNERS.playSound());
		assertFalse(DangerAlertMode.SOUND.showBanners());
		assertTrue(DangerAlertMode.SOUND.playSound());
		assertTrue(DangerAlertMode.BANNERS_AND_SOUND.showBanners());
		assertTrue(DangerAlertMode.BANNERS_AND_SOUND.playSound());
	}

	@Test
	public void combatTrainingHidesSpellbookButNotHomeTeleport()
	{
		boolean windWave = false;
		boolean alch = false;
		boolean dummy = false;
		boolean cannon = false;
		for (SafetyRule rule : SafetyCatalog.rules())
		{
			if (rule.getCategory() != SafetyCategory.COMBAT_TRAINING || rule.getAction() != SafetyAction.HIDE)
			{
				continue;
			}
			if (rule.matches("Cast", "Wind Wave"))
			{
				windWave = true;
			}
			if (rule.matches("Cast", "High Level Alchemy"))
			{
				alch = true;
			}
			if (rule.matches("Attack", "Undead combat dummy"))
			{
				dummy = true;
			}
			if (rule.matches("Fire", "Dwarf multicannon"))
			{
				cannon = true;
			}
		}
		assertTrue(windWave);
		assertTrue(alch);
		assertTrue(dummy);
		assertTrue(cannon);
		assertTrue(SafetyCatalog.isZeroXpSpell("Home Teleport"));
		assertTrue(SafetyCatalog.isZeroXpSpell("Lumbridge Home Teleport"));
		assertFalse(SafetyCatalog.isZeroXpSpell("Wind Strike"));
	}

	@Test
	public void lampLocksOnlyCombatSkills()
	{
		assertEquals(7, CombatSkills.LAMP_LOCK_ORDER.size());
		assertTrue(CombatSkills.isCombat(net.runelite.api.Skill.PRAYER));
		assertFalse(CombatSkills.isCombat(net.runelite.api.Skill.RUNECRAFT));
	}

	@Test
	public void questHelperStartOptionsAreDetected()
	{
		assertTrue(QuestWarnService.isQuestHelperStartOption("Start Quest Helper"));
		assertTrue(QuestWarnService.isQuestHelperStartOption("<col=ff9040>Start Quest Helper"));
		assertTrue(QuestWarnService.isQuestHelperStartOption("Quest Helper"));
		assertFalse(QuestWarnService.isQuestHelperStartOption("Stop Quest Helper"));
		assertFalse(QuestWarnService.isQuestHelperStartOption("Read journal:"));
	}

	@Test
	public void questHelperSidebarFindsTheOpenQuestNotTheList()
	{
		javax.swing.JFrame frame = new javax.swing.JFrame();
		try
		{
			javax.swing.JPanel listOnly = new javax.swing.JPanel();
			listOnly.add(new javax.swing.JLabel("Waterfall Quest"));
			frame.setContentPane(listOnly);
			frame.pack();
			frame.setVisible(true);
			assertTrue(QuestWarnService.findOpenQuestHelperQuests().isEmpty());

			javax.swing.JPanel helperOpen = new javax.swing.JPanel();
			helperOpen.add(new javax.swing.JLabel("Waterfall Quest"));
			helperOpen.add(new javax.swing.JButton("Open RuneScape Wiki"));
			frame.setContentPane(helperOpen);
			frame.pack();
			assertTrue(QuestWarnService.findOpenQuestHelperQuests().contains("Waterfall Quest"));
		}
		finally
		{
			frame.dispose();
		}
	}
}
