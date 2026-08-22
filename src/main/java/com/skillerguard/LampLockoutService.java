package com.skillerguard;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

@Singleton
public class LampLockoutService
{
	private static final Map<Skill, Integer> SKILL_WIDGETS = new EnumMap<>(Skill.class);

	static
	{
		SKILL_WIDGETS.put(Skill.ATTACK, InterfaceID.Xpreward.ATTACK);
		SKILL_WIDGETS.put(Skill.STRENGTH, InterfaceID.Xpreward.STRENGTH);
		SKILL_WIDGETS.put(Skill.DEFENCE, InterfaceID.Xpreward.DEFENCE);
		SKILL_WIDGETS.put(Skill.HITPOINTS, InterfaceID.Xpreward.HITPOINTS);
		SKILL_WIDGETS.put(Skill.RANGED, InterfaceID.Xpreward.RANGED);
		SKILL_WIDGETS.put(Skill.MAGIC, InterfaceID.Xpreward.MAGIC);
		SKILL_WIDGETS.put(Skill.PRAYER, InterfaceID.Xpreward.PRAYER);
	}

	private final Client client;
	private final ClientThread clientThread;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;

	@Inject
	LampLockoutService(
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

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.XPREWARD)
		{
			return;
		}
		clientThread.invoke(this::updateInterface);
	}

	public void refresh()
	{
		clientThread.invoke(this::updateInterface);
	}

	public void restore()
	{
		clientThread.invoke(() -> hideCombatSkills(false));
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!lampOpen() || !shouldLock())
		{
			return;
		}
		String option = Text.removeTags(event.getMenuOption());
		for (Skill skill : CombatSkills.LAMP_LOCK_ORDER)
		{
			if (skill.getName().equalsIgnoreCase(option))
			{
				event.consume();
				state.warn("Combat skills are locked on this lamp");
				return;
			}
		}
		MenuEntry entry = event.getMenuEntry();
		if (entry != null)
		{
			Widget widget = entry.getWidget();
			if (widget != null && SKILL_WIDGETS.containsValue(widget.getId()))
			{
				event.consume();
				state.warn("Combat skills are locked on this lamp");
			}
		}
	}

	private void updateInterface()
	{
		if (!lampOpen())
		{
			state.setLampCombatOnly(false);
			return;
		}
		boolean lock = shouldLock();
		hideCombatSkills(lock);
		state.setLampCombatOnly(lock && onlyCombatSkillsVisible());
	}

	private boolean shouldLock()
	{
		return activation.isActive() && config.lampLockout();
	}

	private boolean lampOpen()
	{
		Widget root = client.getWidget(InterfaceID.Xpreward.UNIVERSE);
		return root != null && !root.isHidden();
	}

	private void hideCombatSkills(boolean hide)
	{
		for (int componentId : SKILL_WIDGETS.values())
		{
			Widget widget = client.getWidget(componentId);
			if (widget != null)
			{
				widget.setHidden(hide);
			}
		}
	}

	private boolean onlyCombatSkillsVisible()
	{
		Widget root = client.getWidget(InterfaceID.Xpreward.UNIVERSE);
		if (root == null)
		{
			return false;
		}
		Widget[] children = root.getStaticChildren();
		if (children == null)
		{
			return false;
		}
		int visibleSkills = 0;
		int visibleCombat = 0;
		for (Widget child : children)
		{
			String[] actions = child.getActions();
			if (actions == null || actions.length == 0 || actions[0] == null)
			{
				continue;
			}
			Skill skill = skillNamed(actions[0]);
			if (skill == null)
			{
				continue;
			}
			visibleSkills++;
			if (CombatSkills.isCombat(skill))
			{
				visibleCombat++;
			}
		}
		return visibleSkills > 0 && visibleSkills == visibleCombat;
	}

	private static Skill skillNamed(String action)
	{
		for (Skill skill : Skill.values())
		{
			if (skill.getName().equalsIgnoreCase(action))
			{
				return skill;
			}
		}
		return null;
	}
}
