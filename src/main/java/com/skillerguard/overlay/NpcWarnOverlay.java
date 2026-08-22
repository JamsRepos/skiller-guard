package com.skillerguard.overlay;

import com.skillerguard.GuardActivation;
import com.skillerguard.SkillerGuardConfig;
import com.skillerguard.WorldWarnTracker;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NpcWarnOverlay extends Overlay
{
	private final Client client;
	private final SkillerGuardConfig config;
	private final GuardActivation activation;
	private final WorldWarnTracker tracker;

	@Inject
	NpcWarnOverlay(Client client, SkillerGuardConfig config, GuardActivation activation, WorldWarnTracker tracker)
	{
		this.client = client;
		this.config = config;
		this.activation = activation;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!activation.isActive() || !config.npcOverheadWarnings())
		{
			return null;
		}

		int plane = client.getPlane();
		for (WorldWarnTracker.NpcLabel label : tracker.getNpcs().values())
		{
			NPC npc = label.npc;
			if (npc == null || npc.getName() == null || npc.getWorldLocation().getPlane() != plane)
			{
				continue;
			}
			net.runelite.api.Point text = Perspective.getCanvasTextLocation(
				client, graphics, npc.getLocalLocation(), label.label, npc.getLogicalHeight() + 40);
			if (text != null)
			{
				OverlayUtil.renderTextLocation(graphics, text, label.label, config.npcOverheadColor());
			}
		}

		for (WorldWarnTracker.ObjectLabel label : tracker.getObjects().values())
		{
			TileObject object = label.object;
			if (object.getWorldLocation().getPlane() != plane)
			{
				continue;
			}
			LocalPoint lp = object.getLocalLocation();
			if (lp == null)
			{
				continue;
			}
			net.runelite.api.Point loc = Perspective.getCanvasTextLocation(client, graphics, lp, label.label, 0);
			if (loc != null)
			{
				OverlayUtil.renderTextLocation(graphics, loc, label.label, config.npcOverheadColor());
			}
		}

		return null;
	}
}
