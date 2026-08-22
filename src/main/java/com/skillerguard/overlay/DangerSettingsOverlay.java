package com.skillerguard.overlay;

import com.skillerguard.GuardActivation;
import com.skillerguard.GuardState;
import com.skillerguard.SkillerGuardConfig;
import com.skillerguard.SkillerGuardPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class DangerSettingsOverlay extends OverlayPanel
{
	private static final int PAD = 16;
	private static final Color HEADER = Color.RED;
	private static final Color BODY = Color.WHITE;

	private final Client client;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;

	@Inject
	DangerSettingsOverlay(
		SkillerGuardPlugin plugin,
		Client client,
		SkillerGuardConfig config,
		GuardActivation activation,
		GuardState state)
	{
		super(plugin);
		this.client = client;
		this.config = config;
		this.activation = activation;
		this.state = state;
		setPosition(OverlayPosition.TOP_CENTER);
		setPriority(OverlayPriority.HIGH);
		panelComponent.setGap(new Point(0, 2));
		panelComponent.setWrap(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!activation.isActive())
		{
			panelComponent.getChildren().clear();
			return null;
		}

		boolean settings = config.warnDangerousSettings() && config.dangerAlertMode().showBanners();
		List<String[]> rows = new ArrayList<>();
		if (state.hasActiveQuestWarning())
		{
			if (state.isQuestDisclaimer())
			{
				rows.add(new String[]{"[SG] QUEST DISCLAIMER", nullToEmpty(state.getQuestWarning())});
			}
			else
			{
				rows.add(new String[]{"[SG] DANGEROUS QUEST", nullToEmpty(state.getQuestWarning())
					+ ". Close the journal or Quest Helper."});
			}
		}
		if (settings && state.hasActiveBreach())
		{
			rows.add(new String[]{"[SG] COMBAT XP GAINED", nullToEmpty(state.getBreachMessage())});
		}
		if (settings && state.isAutoRetaliateOn())
		{
			rows.add(new String[]{"[SG] AUTO RETALIATE IS ON", "Turn it off on the Combat Options tab"});
		}
		if (settings && state.isNpcAttackOptionsOn())
		{
			rows.add(new String[]{"[SG] NPC ATTACK OPTIONS ARE ON", "Set NPC Attack options to Hidden in Controls"});
		}
		if (settings && state.isPlayerAttackOptionsOn())
		{
			rows.add(new String[]{"[SG] PLAYER ATTACK OPTIONS ARE ON", "Set Player Attack options to Hidden in Controls"});
		}
		if (settings && state.isLampCombatOnly())
		{
			rows.add(new String[]{"[SG] THIS LAMP IS COMBAT-ONLY", "Do not confirm — it can only go into a combat skill"});
		}
		if (rows.isEmpty())
		{
			panelComponent.getChildren().clear();
			return null;
		}

		Font headerFont = FontManager.getRunescapeBoldFont();
		Font bodyFont = FontManager.getRunescapeFont();
		Color headerColor = flash(HEADER);
		Color bodyColor = flash(BODY);

		int width = PAD;
		FontMetrics headerMetrics = graphics.getFontMetrics(headerFont);
		FontMetrics bodyMetrics = graphics.getFontMetrics(bodyFont);
		for (String[] row : rows)
		{
			width = Math.max(width, headerMetrics.stringWidth(row[0]) + PAD);
			width = Math.max(width, bodyMetrics.stringWidth(row[1]) + PAD);
		}
		panelComponent.setPreferredSize(new Dimension(width, 0));

		for (int i = 0; i < rows.size(); i++)
		{
			String[] row = rows.get(i);
			panelComponent.getChildren().add(LineComponent.builder()
				.left(row[0])
				.leftColor(headerColor)
				.leftFont(headerFont)
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left(row[1])
				.leftColor(bodyColor)
				.leftFont(bodyFont)
				.build());
			if (i < rows.size() - 1)
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(" ")
					.leftColor(new Color(0, 0, 0, 0))
					.build());
			}
		}

		return super.render(graphics);
	}

	private Color flash(Color base)
	{
		if (!config.blinkDangerBanners())
		{
			return base;
		}
		// Client cycle is ~20ms; 25 cycles is about half a second on, half a second off.
		if ((client.getGameCycle() / 25) % 2 != 0)
		{
			return new Color(base.getRed(), base.getGreen(), base.getBlue(), 50);
		}
		return base;
	}

	private static String nullToEmpty(String value)
	{
		return value == null ? "" : value;
	}
}
