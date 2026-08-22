package com.skillerguard;

import java.util.Set;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;

public final class SafetyCatalog
{
	public static final SafetyRule SPELLBOOK_HIDE = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "*", "*", "Casting a spell grants Magic XP");
	static final SafetyRule PRAYER_REMAINS = SafetyRule.hide(
		SafetyCategory.PRAYER, "Bury", "*", "Burying or scattering remains grants Prayer XP");
	static final SafetyRule PRAYER_ALTAR = SafetyRule.hide(
		SafetyCategory.PRAYER, "Offer", "*", "Offering at an altar grants Prayer XP");
	static final SafetyRule PRAYER_LIBATION = SafetyRule.hide(
		SafetyCategory.PRAYER, "Sacrifice", "*", "Sacrificing blessed bone shards grants Prayer XP");
	static final SafetyRule PRAYER_BONE_BURNER = SafetyRule.hide(
		SafetyCategory.PRAYER, "Offer", "*", "The sacred bone burner grants Prayer XP");
	static final SafetyRule PRAYER_CAMDOZAAL = SafetyRule.hide(
		SafetyCategory.PRAYER, "Offer-fish", "*", "Offering fish at the Camdozaal altar grants Prayer XP");
	static final SafetyRule PRAYER_EGG_SHRINE = SafetyRule.hide(
		SafetyCategory.PRAYER, "Offer", "*", "Offering a bird's egg at the shrine grants Prayer XP");
	static final SafetyRule PRAYER_SARCOPHAGUS = SafetyRule.hide(
		SafetyCategory.PRAYER, "Open", "*", "Pyramid Plunder sarcophagus grants 20 Strength XP");
	static final SafetyRule PRAYER_NIGHTSHADE = SafetyRule.hide(
		SafetyCategory.PRAYER, "Eat", "*", "Eating nightshade is a skiller footgun");
	static final SafetyRule COMBAT_DUMMY = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Attack", "*", "Attacking a dummy grants combat XP");
	static final SafetyRule COMBAT_CANNON = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Fire", "*", "Firing a dwarf cannon grants Ranged XP");
	static final SafetyRule COMBAT_PUMP = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Operate", "*", "The Blast Furnace pump grants Strength XP");
	static final SafetyRule COMBAT_BARB_FISH = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Use-rod", "*", "Barbarian fishing always grants Strength XP");
	static final SafetyRule COMBAT_BAREHAND = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Harpoon", "*", "Barehanded harpooning grants Strength XP");
	static final SafetyRule COMBAT_LECTERN = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Study", "*", "Making tablets at a lectern grants Magic XP");
	static final SafetyRule COMBAT_DARTBOARD = SafetyRule.hide(
		SafetyCategory.COMBAT_TRAINING, "Play", "*", "House ranging games grant Ranged XP");
	static final SafetyRule NPC_MISCLICK = SafetyRule.hideNpc(
		"Talk-to", "*", "This NPC is a common skiller misclick");
	static final SafetyRule MEMOIRS_CHARGE = SafetyRule.alwaysHide(
		SafetyCategory.XP_TRAP, "Charge", "*", "Charging Kharedst's memoirs grants 10 Magic XP per charge");
	static final SafetyRule XP_TRAP_NPC = SafetyRule.warn(
		SafetyCategory.XP_TRAP, "Talk-to", "*", "This NPC can grant combat or Prayer XP");
	static final SafetyRule CLERK_PRAYER = SafetyRule.warn(
		SafetyCategory.XP_TRAP, "Talk-to", "*", "The 151 kudos museum reward includes Prayer XP");

	private static final Set<Integer> TABLET_CRAFT_WIDGETS = Set.of(
		InterfaceID.TeletabsCraftIf.TAB_1,
		InterfaceID.TeletabsCraftIf.TAB_2,
		InterfaceID.TeletabsCraftIf.TAB_3,
		InterfaceID.TeletabsCraftIf.TAB_4,
		InterfaceID.TeletabsCraftIf.TAB_5,
		InterfaceID.TeletabsCraftIf.TAB_6,
		InterfaceID.TeletabsCraftIf.TAB_7,
		InterfaceID.TeletabsCraftIf.TAB_8,
		InterfaceID.TeletabsCraftIf.TAB_9,
		InterfaceID.TeletabsCraftIf.TAB_10,
		InterfaceID.TeletabsCraftIf.TAB_11,
		InterfaceID.TeletabsCraftIf.TAB_12,
		InterfaceID.TeletabsCraftIf.TAB_13,
		InterfaceID.TeletabsCraftIf.TAB_14,
		InterfaceID.TeletabsCraftIf.TAB_15,
		InterfaceID.TeletabsCraftIf.TAB_16,
		InterfaceID.TeletabsCraftIf.TAB_17,
		InterfaceID.TeletabsCraftIf.TAB_18,
		InterfaceID.TeletabsCraftIf.TAB_19,
		InterfaceID.TeletabsCraftIf.TAB_20,
		InterfaceID.TeletabsCraftIf.TAB_21,
		InterfaceID.TeletabsCraftIf.MAKE_1,
		InterfaceID.TeletabsCraftIf.MAKE_5,
		InterfaceID.TeletabsCraftIf.MAKE_10,
		InterfaceID.TeletabsCraftIf.MAKE_X,
		InterfaceID.TeletabsCraftIf.MAKE_ALL,
		InterfaceID.TeletabsCraftIf.MAKE_SOME,
		InterfaceID.TeletabsCraftIf.MAKEX,
		InterfaceID.TeletabsCraftIf.CONFIRM
	);

	private static final Set<Integer> ZERO_XP_SPELL_WIDGETS = Set.of(
		InterfaceID.MagicSpellbook.TELEPORT_HOME_STANDARD,
		InterfaceID.MagicSpellbook.TELEPORT_HOME_ZAROS,
		InterfaceID.MagicSpellbook.TELEPORT_HOME_LUNAR,
		InterfaceID.MagicSpellbook.TELEPORT_HOME_ARCEUUS,
		InterfaceID.MagicSpellbook.LEAGUE_HOME_TELEPORT,
		InterfaceID.MagicSpellbook.TELEPORT_MINIGAME_STANDARD,
		InterfaceID.MagicSpellbook.TELEPORT_MINIGAME_ANCIENT,
		InterfaceID.MagicSpellbook.TELEPORT_MINIGAME_ARCEUUS,
		InterfaceID.MagicSpellbook.TELEPORT_MINIGAME_LUNAR
	);

	private SafetyCatalog()
	{
	}

	static SafetyRule hideRule(MenuEntry entry)
	{
		return hideRule(entry, -1, -1);
	}

	static SafetyRule hideRule(MenuEntry entry, int selectedItemId, int resolvedObjectId)
	{
		if (entry == null)
		{
			return null;
		}
		int item = itemId(entry);
		if (item <= 0)
		{
			item = selectedItemId;
		}
		int object = resolvedObjectId > 0 ? resolvedObjectId : objectId(entry);
		return hideRule(entry.getType(), npcId(entry), item, object, entry.getOption(), true);
	}

	static SafetyRule hideRule(MenuEntry entry, int selectedItemId, int resolvedObjectId, boolean hasHarpoon)
	{
		if (entry == null)
		{
			return null;
		}
		int item = itemId(entry);
		if (item <= 0)
		{
			item = selectedItemId;
		}
		int object = resolvedObjectId > 0 ? resolvedObjectId : objectId(entry);
		return hideRule(entry.getType(), npcId(entry), item, object, entry.getOption(), hasHarpoon);
	}

	static SafetyRule hideRule(MenuAction type, int npcId, int itemId, int objectId, String option)
	{
		return hideRule(type, npcId, itemId, objectId, option, true);
	}

	static SafetyRule hideRule(MenuAction type, int npcId, int itemId, int objectId, String option, boolean hasHarpoon)
	{
		if (type == null || SafetyIds.isWalkOrCancel(type) || SafetyIds.isExamine(type))
		{
			return null;
		}
		if (SafetyIds.DUMMY_NPCS.contains(npcId) && SafetyIds.isNpcOp(type))
		{
			return COMBAT_DUMMY;
		}
		if (SafetyIds.DUMMY_OBJECTS.contains(objectId) && SafetyIds.isObjectOp(type))
		{
			return COMBAT_DUMMY;
		}
		if (SafetyIds.CANNONS.contains(objectId)
			&& (op(option, "Fire") || SafetyIds.isUseOnObject(type)))
		{
			return COMBAT_CANNON;
		}
		if (SafetyIds.BLAST_FURNACE_PUMPS.contains(objectId) && SafetyIds.isObjectOp(type))
		{
			return COMBAT_PUMP;
		}
		if (SafetyIds.BARBARIAN_FISHING_SPOTS.contains(npcId) && SafetyIds.isNpcOp(type))
		{
			return COMBAT_BARB_FISH;
		}
		if (SafetyIds.HARPOON_FISHING_SPOTS.contains(npcId) && SafetyIds.isNpcOp(type)
			&& op(option, "Harpoon") && !hasHarpoon)
		{
			return COMBAT_BAREHAND;
		}
		if (SafetyIds.POH_LECTERNS.contains(objectId)
			&& (SafetyIds.isUseOnObject(type) || (SafetyIds.isObjectOp(type) && op(option, "Study"))))
		{
			return COMBAT_LECTERN;
		}
		if (SafetyIds.POH_RANGING_GAMES.contains(objectId) && SafetyIds.isObjectOp(type)
			&& !SafetyIds.isConstructionOp(option))
		{
			return COMBAT_DARTBOARD;
		}
		if (SafetyIds.MISCLICK_NPCS.contains(npcId) && SafetyIds.isNpcOp(type))
		{
			return NPC_MISCLICK;
		}
		if (SafetyIds.TAMAYU.contains(npcId) && SafetyIds.isNpcOp(type))
		{
			return NPC_MISCLICK;
		}
		if (SafetyIds.EVIL_BOB.contains(npcId) && SafetyIds.isNpcOp(type))
		{
			return NPC_MISCLICK;
		}
		if (SafetyIds.INFORMATION_CLERK.contains(npcId) && SafetyIds.isNpcOp(type))
		{
			return NPC_MISCLICK;
		}
		if (objectId == SafetyIds.OLD_MEMORIAL
			&& (SafetyIds.isObjectOp(type) || type == MenuAction.ITEM_USE_ON_GAME_OBJECT
			|| type == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
			&& !op(option, "Reminisce"))
		{
			return MEMOIRS_CHARGE;
		}
		if (SafetyIds.MEMOIRS.contains(itemId) && op(option, "Charge"))
		{
			return MEMOIRS_CHARGE;
		}
		if (SafetyIds.BONES.contains(itemId) && (op(option, "Bury") || op(option, "Take")))
		{
			return PRAYER_REMAINS;
		}
		if (SafetyIds.ASHES.contains(itemId) && (op(option, "Scatter") || op(option, "Take")))
		{
			return PRAYER_REMAINS;
		}
		if (SafetyIds.NIGHTSHADE.contains(itemId) && op(option, "Eat"))
		{
			return PRAYER_NIGHTSHADE;
		}
		if (SafetyIds.SHARD_BREAKDOWN.contains(itemId) && op(option, "Break-down"))
		{
			return PRAYER_LIBATION;
		}
		if (SafetyIds.PRAYER_ALTARS.contains(objectId) && SafetyIds.isUseOnObject(type))
		{
			return PRAYER_ALTAR;
		}
		if (SafetyIds.PRAYER_ALTARS.contains(objectId) && SafetyIds.isObjectOp(type)
			&& (op(option, "Offer") || optionContains(option, "offer")))
		{
			return PRAYER_ALTAR;
		}
		if (SafetyIds.LIBATION_BOWLS.contains(objectId)
			&& (SafetyIds.isUseOnObject(type) || (SafetyIds.isObjectOp(type) && !isCheckOption(option))))
		{
			return PRAYER_LIBATION;
		}
		if (SafetyIds.EXPOSED_ALTARS.contains(objectId)
			&& (SafetyIds.isUseOnObject(type) || op(option, "Bless")))
		{
			return PRAYER_LIBATION;
		}
		if (SafetyIds.SACRED_BONE_BURNERS.contains(objectId)
			&& (SafetyIds.isUseOnObject(type) || (SafetyIds.isObjectOp(type) && !isCheckOption(option))))
		{
			return PRAYER_BONE_BURNER;
		}
		if (SafetyIds.CAMDOZAAL_ALTARS.contains(objectId)
			&& (SafetyIds.isUseOnObject(type) || optionContains(option, "offer")))
		{
			return PRAYER_CAMDOZAAL;
		}
		if (SafetyIds.BIRD_EGG_SHRINES.contains(objectId)
			&& (SafetyIds.isUseOnObject(type) || optionContains(option, "offer")))
		{
			return PRAYER_EGG_SHRINE;
		}
		if (SafetyIds.PYRAMID_SARCOPHAGUS.contains(objectId) && SafetyIds.isObjectOp(type) && op(option, "Open"))
		{
			return PRAYER_SARCOPHAGUS;
		}
		return null;
	}

	static SafetyRule warnRule(MenuEntry entry)
	{
		if (entry == null)
		{
			return null;
		}
		return warnRule(entry.getType(), npcId(entry), itemId(entry), entry.getOption());
	}

	static SafetyRule warnRule(MenuAction type, int npcId, int itemId, String option)
	{
		if (type == null || SafetyIds.isWalkOrCancel(type) || SafetyIds.isExamine(type))
		{
			return null;
		}
		if (SafetyIds.isNpcOp(type) && SafetyIds.INFORMATION_CLERK.contains(npcId))
		{
			return CLERK_PRAYER;
		}
		if (SafetyIds.isNpcOp(type) && (SafetyIds.TAMAYU.contains(npcId)
			|| SafetyIds.JUNA.contains(npcId)
			|| SafetyIds.OTTO.contains(npcId)
			|| SafetyIds.ELNOCK.contains(npcId)
			|| SafetyIds.FATHER_AERECK.contains(npcId)))
		{
			return XP_TRAP_NPC;
		}
		return null;
	}

	enum SpellbookOp
	{
		HIDE,
		KEEP,
		NONE
	}

	static SpellbookOp spellbookOp(MenuEntry entry)
	{
		if (entry == null)
		{
			return SpellbookOp.NONE;
		}
		return spellbookOp(entry.getWidget(), entry.getOption());
	}

	static SpellbookOp spellbookOp(Widget widget)
	{
		return spellbookOp(widget, "");
	}

	private static SpellbookOp spellbookOp(Widget widget, String option)
	{
		Widget current = widget;
		for (int depth = 0; depth < 6 && current != null; depth++)
		{
			int id = current.getId();
			if (WidgetUtil.componentToInterface(id) != InterfaceID.MAGIC_SPELLBOOK)
			{
				return SpellbookOp.NONE;
			}
			SpellbookOp op = spellbookOp(id, current.getParentId(), option);
			if (op != SpellbookOp.NONE)
			{
				return op;
			}
			current = current.getParent();
		}
		return SpellbookOp.NONE;
	}

	static SpellbookOp spellbookOp(int widgetId, int parentId, String option)
	{
		if (WidgetUtil.componentToInterface(widgetId) != InterfaceID.MAGIC_SPELLBOOK)
		{
			return SpellbookOp.NONE;
		}
		if (ZERO_XP_SPELL_WIDGETS.contains(widgetId))
		{
			return SpellbookOp.KEEP;
		}
		if (parentId != InterfaceID.MagicSpellbook.SPELLLAYER)
		{
			return SpellbookOp.NONE;
		}
		if (op(option, "Configure"))
		{
			return SpellbookOp.KEEP;
		}
		return SpellbookOp.HIDE;
	}

	static boolean isSpellbookXpOp(int widgetId, int parentId, String option)
	{
		return spellbookOp(widgetId, parentId, option) == SpellbookOp.HIDE;
	}

	static boolean isTabletCraftXpOp(Widget widget)
	{
		if (widget == null)
		{
			return false;
		}
		Widget current = widget;
		for (int depth = 0; depth < 6 && current != null; depth++)
		{
			if (TABLET_CRAFT_WIDGETS.contains(current.getId()))
			{
				return true;
			}
			if (WidgetUtil.componentToInterface(current.getId()) != InterfaceID.TELETABS_CRAFT_IF)
			{
				return false;
			}
			current = current.getParent();
		}
		return false;
	}

	static Iterable<Integer> tabletCraftWidgets()
	{
		return TABLET_CRAFT_WIDGETS;
	}

	static boolean isSpellTargetAction(MenuAction type)
	{
		return type == MenuAction.WIDGET_TARGET_ON_NPC
			|| type == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT
			|| type == MenuAction.WIDGET_TARGET_ON_GROUND_ITEM
			|| type == MenuAction.WIDGET_TARGET_ON_WIDGET
			|| type == MenuAction.WIDGET_USE_ON_ITEM;
	}

	private static int npcId(MenuEntry entry)
	{
		NPC npc = entry.getNpc();
		return npc == null ? -1 : npc.getId();
	}

	private static int itemId(MenuEntry entry)
	{
		int id = entry.getItemId();
		if (id > 0)
		{
			return id;
		}
		if (SafetyIds.isGroundItemOp(entry.getType()))
		{
			return entry.getIdentifier();
		}
		return -1;
	}

	private static int objectId(MenuEntry entry)
	{
		MenuAction type = entry.getType();
		if (SafetyIds.isObjectOp(type)
			|| type == MenuAction.ITEM_USE_ON_GAME_OBJECT
			|| type == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
		{
			return entry.getIdentifier();
		}
		return -1;
	}

	private static boolean op(String option, String expected)
	{
		return expected.equalsIgnoreCase(MenuMatcher.strip(option));
	}

	private static boolean optionContains(String option, String fragment)
	{
		return MenuMatcher.strip(option).toLowerCase().contains(fragment.toLowerCase());
	}

	private static boolean isCheckOption(String option)
	{
		String stripped = MenuMatcher.strip(option);
		return "Check".equalsIgnoreCase(stripped) || "Check-Faith".equalsIgnoreCase(stripped);
	}
}
