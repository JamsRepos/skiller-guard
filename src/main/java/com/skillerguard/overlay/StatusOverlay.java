package com.skillerguard.overlay;

import com.skillerguard.GuardActivation;
import com.skillerguard.GuardState;
import com.skillerguard.SkillerGuardConfig;
import com.skillerguard.SkillerGuardPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class StatusOverlay extends OverlayPanel
{
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final GuardState state;

	@Inject
	StatusOverlay(SkillerGuardPlugin plugin, SkillerGuardConfig config, GuardActivation activation, GuardState state)
	{
		super(plugin);
		this.config = config;
		this.activation = activation;
		this.state = state;
		setPosition(OverlayPosition.TOP_LEFT);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Jam's Skiller Guard");
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showStatusOverlay() || !config.enabled())
		{
			return null;
		}

		boolean active = activation.isActive();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Jam's Skiller Guard")
			.color(active ? Color.GREEN : Color.GRAY)
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(active ? "ACTIVE" : "idle")
			.leftColor(active ? Color.GREEN : Color.GRAY)
			.right(config.activationMode().toString())
			.build());

		if (state.getLastWarning() != null)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(state.getLastWarning())
				.leftColor(Color.ORANGE)
				.build());
		}
		if (state.isPassivePrayerGear())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Bonecrusher/ash gear equipped")
				.leftColor(Color.ORANGE)
				.build());
		}

		return super.render(graphics);
	}
}
