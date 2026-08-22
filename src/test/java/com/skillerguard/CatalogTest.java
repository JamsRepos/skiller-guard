package com.skillerguard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import net.runelite.api.MenuAction;
import net.runelite.api.Quest;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.WidgetUtil;
import org.junit.Test;

public class CatalogTest
{
	@Test
	public void bonesAndAshesAreHiddenByItemId()
	{
		assertEquals(SafetyCategory.PRAYER, SafetyCatalog.hideRule(
			MenuAction.ITEM_THIRD_OPTION, -1, ItemID.DRAGON_BONES, -1, "Bury").getCategory());
		assertEquals(SafetyCategory.PRAYER, SafetyCatalog.hideRule(
			MenuAction.ITEM_THIRD_OPTION, -1, ItemID.ABYSSAL_ASHES, -1, "Scatter").getCategory());
		assertEquals(SafetyCategory.PRAYER, SafetyCatalog.hideRule(
			MenuAction.GROUND_ITEM_THIRD_OPTION, -1, ItemID.BONES, -1, "Take").getCategory());
		assertNull(SafetyCatalog.hideRule(
			MenuAction.ITEM_THIRD_OPTION, -1, ItemID.DRAGON_BONES, -1, "Drop"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.ITEM_USE, -1, ItemID.BONES, -1, "Use"));
	}

	@Test
	public void boneCrossbowIsNotRemains()
	{
		assertFalse(SafetyIds.isRemains(ItemID.DTTD_BONE_CROSSBOW));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.ITEM_FIRST_OPTION, -1, ItemID.DTTD_BONE_CROSSBOW, -1, "Wield"));
	}

	@Test
	public void offeringBonesUsesAltarObjectIdNotTheWordAltar()
	{
		assertEquals(SafetyCatalog.PRAYER_ALTAR, SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, ItemID.BONES, ObjectID.CHAOSALTAR, "Use"));
		assertEquals(SafetyCatalog.PRAYER_ALTAR, SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, -1, ObjectID.POH_ALTAR_GUTHIX_7, "Use"));
		assertEquals(SafetyCatalog.PRAYER_ALTAR, SafetyCatalog.hideRule(
			MenuAction.WIDGET_TARGET_ON_GAME_OBJECT, -1, ItemID.DRAGON_BONES, ObjectID.POH_ALTAR_OCCULT, "Use"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.POH_ALTAR_GUTHIX_7, "Pray-at"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.CHAOSALTAR, "Pray-at"));
	}

	@Test
	public void skillerPrayerMethodsAreHiddenByObjectId()
	{
		assertEquals(SafetyCatalog.PRAYER_LIBATION, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.VARLAMORE_LIBATION_BOWL_FULL, "Sacrifice"));
		assertEquals(SafetyCatalog.PRAYER_LIBATION, SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, ItemID.BLESSED_BONE_SHARD, ObjectID.VARLAMORE_LIBATION_BOWL, "Use"));
		assertEquals(SafetyCatalog.PRAYER_LIBATION, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.VARLAMORE_LIBATION_BOWL_EMPTY, "Fill"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_SECOND_OPTION, -1, -1, ObjectID.VARLAMORE_LIBATION_BOWL_FULL, "Check"));
		assertEquals(SafetyCatalog.PRAYER_LIBATION, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.VARLAMORE_PRAYER_ACTIVITY_ALTAR, "Bless"));
		assertEquals(SafetyCatalog.PRAYER_BONE_BURNER, SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, ItemID.DRAGON_BONES, ObjectID.HOSDUN_BONE_BURNER, "Use"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.HOSDUN_BONE_BURNER, "Check-Faith"));
		assertEquals(SafetyCatalog.PRAYER_CAMDOZAAL, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.CAMDOZAAL_ALTAR, "Offer-fish"));
		assertEquals(SafetyCatalog.PRAYER_EGG_SHRINE, SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, ItemID.BIRD_EGG_RED, ObjectID.WCGUILD_SHRINE, "Use"));
		assertEquals(SafetyCatalog.PRAYER_REMAINS, SafetyCatalog.hideRule(
			MenuAction.ITEM_THIRD_OPTION, -1, ItemID.DORGESH_CONSTRUCTION_BONE, -1, "Bury"));
		assertEquals(SafetyCatalog.PRAYER_LIBATION, SafetyCatalog.hideRule(
			MenuAction.ITEM_FIRST_OPTION, -1, ItemID.BLESSED_DRAGON_BONES, -1, "Break-down"));
		assertEquals(SafetyCatalog.PRAYER_LIBATION, SafetyCatalog.hideRule(
			MenuAction.ITEM_FIRST_OPTION, -1, ItemID.VARLAMORE_BONE_STATUETTE01, -1, "Break-down"));
		assertEquals(SafetyCatalog.PRAYER_REMAINS, SafetyCatalog.hideRule(
			MenuAction.ITEM_THIRD_OPTION, -1, ItemID.NEWBIEBONES, -1, "Bury"));
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.CIVITAS_COMBAT_DUMMY, "Attack"));
	}

	@Test
	public void sarcophagusAndMemorialAreObjectIds()
	{
		assertEquals(SafetyCatalog.PRAYER_SARCOPHAGUS, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.NTK_SARCOPHAGUS, "Open"));
		assertTrue(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.KOURENDWOODLAND_STATUE, "Inspect").isAlwaysHide());
		assertNull(SafetyCatalog.hideRule(
			MenuAction.ITEM_FIRST_OPTION, -1, ItemID.VEOS_KHAREDSTS_MEMOIRS, -1, "Reminisce"));
	}

	@Test
	public void dummyAndCannonUseNpcAndObjectIds()
	{
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.POH_COMBAT_DUMMY_NPC, -1, -1, "Attack"));
		assertEquals(SafetyCatalog.COMBAT_CANNON, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.DWARF_MULTICANNON1, "Fire"));
		assertEquals(SafetyCatalog.COMBAT_CANNON, SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, -1, ObjectID.DWARF_MULTICANNON1, "Use"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_SECOND_OPTION, -1, -1, ObjectID.DWARF_MULTICANNON1, "Pick-up"));
	}

	@Test
	public void skillerCombatMethodsAreHiddenByObjectId()
	{
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.FAI_VARROCK_SWORD_DUMMY, "Attack"));
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.SWORDDUMMY, "Attack"));
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.SLUG2_SWORDDUMMY, "Hit"));
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.KR_CAM_SWORDDUMMY, "Hit"));
		assertEquals(SafetyCatalog.COMBAT_DUMMY, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.BIOHAZARDDUMMY, "Hit"));
		assertEquals(SafetyCatalog.COMBAT_PUMP, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.BLAST_FURNACE_PUMP, "Operate"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.BLAST_FURNACE_PEDALS, "Operate"));
		assertEquals(SafetyCatalog.COMBAT_BARB_FISH, SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID._0_39_54_BRUT_FISHING_SPOT, -1, -1, "Use-rod"));
		assertEquals(SafetyCatalog.COMBAT_BARB_FISH, SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID._0_19_55_BRUT_FISHING_SPOT, -1, -1, "Use-rod"));
		assertEquals(SafetyCatalog.COMBAT_BAREHAND, SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.RAREFISH, -1, -1, "Harpoon", false));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.RAREFISH, -1, -1, "Harpoon", true));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.RAREFISH, -1, -1, "Cage", false));
		assertEquals(SafetyCatalog.COMBAT_LECTERN, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.POH_LECTERN_7, "Study"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.POH_LECTERN_7, "Remove"));
		assertEquals(SafetyCatalog.COMBAT_DARTBOARD, SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.POH_DARTBOARD1, "Play"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.POH_DARTBOARD1, "Remove"));
	}

	@Test
	public void combatGatedMethodsAreLeftAlone()
	{
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.WARGUILD_DUMMY_ACC, "Hit"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.WAR_STRENGTH_SHOT22, "Throw"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.RANGING_TARGET, "Fire-at"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.ARCEUUS_LECTERN, "Study"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.II_MAGIC_WHEAT_M, "Push-through"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.MAGICTRAINING_COIN_COLLECTOR, "Deposit"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.WILDERNESS_GWD_BOULDER, -1, -1, "Move"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, ItemID.BONES, ObjectID.AHOY_ECTOFUNTUS, "Use"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.GAME_OBJECT_FIRST_OPTION, -1, -1, ObjectID.TEMPLE_PYRE_BONES_MAGIC, "Light"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.ITEM_USE_ON_GAME_OBJECT, -1, ItemID.VAMPIRE_DUST, ObjectID.HALLOWED_TREASURE_PRAYER_READY, "Use"));
		assertNull(SafetyCatalog.warnRule(
			MenuAction.CC_OP, -1, ItemID.BONECRUSHER, "Activity"));
	}

	@Test
	public void misclickNpcsAreIdsNotNameGlobs()
	{
		assertEquals(SafetyCatalog.NPC_MISCLICK, SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.MAN, -1, -1, "Talk-to"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.EXAMINE_NPC, NpcID.MAN, -1, -1, "Examine"));
		assertNull(NamedNpcCatalog.labelFor(NpcID.MAN));
	}

	@Test
	public void overheadNamedNpcsUseNpcIds()
	{
		assertEquals("[SG] Atk+Str XP", NamedNpcCatalog.labelFor(NpcID.TBWT_TAMAYU));
		assertEquals("[SG] lowest skill", NamedNpcCatalog.labelFor(NpcID.TOG_JUNA_DUMMY));
		assertNull(NamedNpcCatalog.labelFor(NpcID.MAN));
		assertNull(NamedNpcCatalog.labelFor(NpcID.WOMAN));
		assertNull(NamedNpcCatalog.labelFor(NpcID.VM_TIMELINE_HISTORIAN));
		assertEquals("[SG] Pray XP", NamedNpcCatalog.labelFor(NpcID.VM_INFO_BOOTH_LADY));
		assertEquals("[SG] Str XP wheat", NamedNpcCatalog.labelFor(NpcID.II_ELNOCK));
		assertEquals("[SG] Mag XP", NamedObjectCatalog.labelFor(ObjectID.POH_LECTERN_7));
		assertEquals("[SG] Rng XP", NamedObjectCatalog.labelFor(ObjectID.POH_DARTBOARD1));
		assertEquals("[SG] Mag XP", NamedObjectCatalog.labelFor(ObjectID.KOURENDWOODLAND_STATUE));
		assertEquals(SafetyCatalog.XP_TRAP_NPC, SafetyCatalog.warnRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.II_ELNOCK, -1, "Talk-to"));
		assertNull(SafetyCatalog.hideRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.VM_TIMELINE_HISTORIAN, -1, -1, "Talk-to"));
		assertNull(SafetyCatalog.warnRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.VM_TIMELINE_HISTORIAN, -1, "Talk-to"));
		assertEquals(SafetyCatalog.CLERK_PRAYER, SafetyCatalog.warnRule(
			MenuAction.NPC_FIRST_OPTION, NpcID.VM_INFO_BOOTH_LADY, -1, "Talk-to"));
	}

	@Test
	public void walkAndCancelAreMenuActions()
	{
		assertTrue(SafetyIds.isWalkOrCancel(MenuAction.WALK));
		assertTrue(SafetyIds.isWalkOrCancel(MenuAction.CANCEL));
		assertNull(SafetyCatalog.hideRule(MenuAction.WALK, NpcID.MAN, ItemID.BONES, -1, "Walk here"));
		assertNull(SafetyCatalog.hideRule(MenuAction.CANCEL, -1, ItemID.BONES, -1, "Cancel"));
	}

	@Test
	public void questDenylistUsesQuestEnum()
	{
		assertEquals(QuestDenylist.normalize("The Restless Ghost"),
			QuestDenylist.normalize("Restless Ghost"));
		assertNotNull(QuestDenylist.reasonFor(Quest.THE_RESTLESS_GHOST));
		assertNotNull(QuestDenylist.reasonFor(Quest.PRIEST_IN_PERIL));
		assertEquals(QuestDenylist.OBSERVATORY_REASON, QuestDenylist.reasonFor(Quest.OBSERVATORY_QUEST));
		assertTrue(QuestDenylist.isDisclaimerTitle("Observatory Quest"));
		assertTrue(QuestDenylist.isDisclaimerTitle("The Observatory Quest"));
		assertFalse(QuestDenylist.isDisclaimerTitle("Waterfall Quest"));
		assertNotNull(QuestDenylist.reasonFor(Quest.HIS_FAITHFUL_SERVANTS));
		assertEquals("Mag XP", QuestDenylist.reasonFor(Quest.RECIPE_FOR_DISASTER__LUMBRIDGE_GUIDE));
		assertNotNull(QuestDenylist.reasonFor(Quest.BARBARIAN_TRAINING));
		assertNotNull(QuestDenylist.reasonFor(Quest.WATERFALL_QUEST));
		assertNotNull(QuestDenylist.reasonForTitle("Waterfall Quest"));
		assertNull(QuestDenylist.reasonFor(Quest.COOKS_ASSISTANT));
		assertNull(QuestDenylist.reasonForTitle("Cook's Assistant"));
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
	public void spellbookWidgetHidesDestinationsAndBoatWithoutMatchingNames()
	{
		int layer = InterfaceID.MagicSpellbook.SPELLLAYER;
		int varrock = InterfaceID.MagicSpellbook.VARROCK_TELEPORT;
		int camelot = InterfaceID.MagicSpellbook.CAMELOT_TELEPORT;
		int kourend = InterfaceID.MagicSpellbook.KOUREND_TELEPORT;
		int summonBoat = InterfaceID.MagicSpellbook.TELEPORT_BOAT_TO_ME;
		int teleportToBoat = InterfaceID.MagicSpellbook.TELEPORT_ME_TO_BOAT;
		int home = InterfaceID.MagicSpellbook.TELEPORT_HOME_STANDARD;
		int minigame = InterfaceID.MagicSpellbook.TELEPORT_MINIGAME_STANDARD;
		int filter = InterfaceID.MagicSpellbook.FILTERBUTTON;
		int inventory = WidgetUtil.packComponentId(InterfaceID.INVENTORY, 0);

		assertTrue(SafetyCatalog.isSpellbookXpOp(varrock, layer, "Grand Exchange"));
		assertTrue(SafetyCatalog.isSpellbookXpOp(camelot, layer, "Seers' Village"));
		assertTrue(SafetyCatalog.isSpellbookXpOp(kourend, layer, "Hosidius"));
		assertTrue(SafetyCatalog.isSpellbookXpOp(summonBoat, layer, "Summon last boat"));
		assertTrue(SafetyCatalog.isSpellbookXpOp(summonBoat, layer, "Summon Boat"));
		assertTrue(SafetyCatalog.isSpellbookXpOp(teleportToBoat, layer, "Last Boat"));
		assertTrue(SafetyCatalog.isSpellbookXpOp(varrock, layer, "Cast"));

		assertEquals(SafetyCatalog.SpellbookOp.KEEP, SafetyCatalog.spellbookOp(home, layer, "Cast"));
		assertEquals(SafetyCatalog.SpellbookOp.KEEP, SafetyCatalog.spellbookOp(minigame, layer, "Cast"));
		assertEquals(SafetyCatalog.SpellbookOp.KEEP, SafetyCatalog.spellbookOp(
			InterfaceID.MagicSpellbook.TELEPORT_MINIGAME_ANCIENT, layer, "Cast"));
		assertFalse(SafetyCatalog.isSpellbookXpOp(varrock, layer, "Configure"));
		assertFalse(SafetyCatalog.isSpellbookXpOp(filter, InterfaceID.MagicSpellbook.BOTTOM, "Filters"));
		assertFalse(SafetyCatalog.isSpellbookXpOp(inventory, 0, "Break"));
		assertTrue(SafetyCatalog.isSpellTargetAction(MenuAction.WIDGET_TARGET_ON_NPC));
		assertTrue(SafetyCatalog.isSpellTargetAction(MenuAction.WIDGET_USE_ON_ITEM));
		assertFalse(SafetyCatalog.isSpellTargetAction(MenuAction.WIDGET_TARGET_ON_PLAYER));
		assertFalse(SafetyCatalog.isSpellTargetAction(MenuAction.NPC_FIRST_OPTION));
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

	@Test
	public void changelogIsOncePerVersion() throws IOException
	{
		assertFalse(Changelog.RELEASES.isEmpty());
		Changelog.Release latest = Changelog.RELEASES.get(Changelog.RELEASES.size() - 1);
		assertEquals(Changelog.VERSION, latest.version);
		assertFalse(latest.notes.isEmpty());
		for (int i = 1; i < Changelog.RELEASES.size(); i++)
		{
			assertTrue(Changelog.compareVersions(
				Changelog.RELEASES.get(i).version,
				Changelog.RELEASES.get(i - 1).version) > 0);
		}
		assertEquals(Changelog.RELEASES.size(), Changelog.unseenSince("").size());
		if (Changelog.RELEASES.size() > 1)
		{
			List<Changelog.Release> afterFirst =
				Changelog.unseenSince(Changelog.RELEASES.get(0).version);
			assertEquals(Changelog.RELEASES.size() - 1, afterFirst.size());
			assertEquals(Changelog.VERSION, afterFirst.get(afterFirst.size() - 1).version);
		}
		assertTrue(Changelog.unseenSince(Changelog.VERSION).isEmpty());
		assertTrue(Changelog.isUnseen(""));
		assertTrue(Changelog.isUnseen("1.0.0"));
		assertFalse(Changelog.isUnseen(Changelog.VERSION));
		assertTrue(Changelog.compareVersions("1.10.0", "1.9.0") > 0);
		assertEquals(Changelog.VERSION, pluginPropertyVersion());
		assertEquals(Changelog.VERSION, gradleVersion());
	}

	private static String pluginPropertyVersion() throws IOException
	{
		Properties properties = new Properties();
		properties.load(Files.newBufferedReader(Path.of("runelite-plugin.properties"), StandardCharsets.UTF_8));
		return properties.getProperty("version");
	}

	private static String gradleVersion() throws IOException
	{
		for (String line : Files.readAllLines(Path.of("build.gradle"), StandardCharsets.UTF_8))
		{
			String trimmed = line.trim();
			if (trimmed.startsWith("version = '") && trimmed.contains("Changelog.VERSION"))
			{
				int start = trimmed.indexOf('\'') + 1;
				int end = trimmed.indexOf('\'', start);
				return trimmed.substring(start, end);
			}
		}
		throw new AssertionError("build.gradle is missing version = '...' // Keep Changelog.VERSION");
	}
}
