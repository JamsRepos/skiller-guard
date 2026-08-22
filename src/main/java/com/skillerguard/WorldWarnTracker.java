package com.skillerguard;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.GameState;

@Singleton
public class WorldWarnTracker
{
	private final Client client;
	private final Map<Integer, NpcLabel> npcs = new HashMap<>();
	private final Map<Long, TileObject> memorials = new HashMap<>();

	@Inject
	WorldWarnTracker(Client client)
	{
		this.client = client;
	}

	public Map<Integer, NpcLabel> getNpcs()
	{
		return npcs;
	}

	public Map<Long, TileObject> getMemorials()
	{
		return memorials;
	}

	public void onNpcSpawned(NpcSpawned event)
	{
		track(event.getNpc());
	}

	public void onNpcDespawned(NpcDespawned event)
	{
		npcs.remove(event.getNpc().getIndex());
	}

	public void onNpcChanged(NpcChanged event)
	{
		track(event.getNpc());
	}

	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (event.getGameObject().getId() == ObjectID.KOURENDWOODLAND_STATUE)
		{
			memorials.put(key(event.getGameObject()), event.getGameObject());
		}
	}

	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		memorials.remove(key(event.getGameObject()));
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			clear();
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			scanOnce();
		}
	}

	/**
	 * One-shot capture of NPCs/objects already in the scene. Not called every tick.
	 */
	public void scanOnce()
	{
		if (client.getNpcs() != null)
		{
			for (NPC npc : client.getNpcs())
			{
				track(npc);
			}
		}
		Scene scene = client.getScene();
		if (scene == null)
		{
			return;
		}
		Tile[][][] tiles = scene.getTiles();
		if (tiles == null)
		{
			return;
		}
		int plane = client.getPlane();
		if (plane < 0 || plane >= tiles.length || tiles[plane] == null)
		{
			return;
		}
		for (Tile[] row : tiles[plane])
		{
			if (row == null)
			{
				continue;
			}
			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}
				GameObject[] objects = tile.getGameObjects();
				if (objects == null)
				{
					continue;
				}
				for (GameObject object : objects)
				{
					if (object != null && object.getId() == ObjectID.KOURENDWOODLAND_STATUE)
					{
						memorials.put(key(object), object);
					}
				}
			}
		}
	}

	public void clear()
	{
		npcs.clear();
		memorials.clear();
	}

	private void track(NPC npc)
	{
		if (npc == null)
		{
			return;
		}
		String label = NamedNpcCatalog.labelFor(npc.getName());
		if (label == null)
		{
			npcs.remove(npc.getIndex());
			return;
		}
		npcs.put(npc.getIndex(), new NpcLabel(npc, label));
	}

	private static long key(TileObject object)
	{
		WorldPoint wp = object.getWorldLocation();
		return ((long) object.getId() << 32)
			^ ((long) wp.getX() << 16)
			^ ((long) wp.getY() << 8)
			^ wp.getPlane();
	}

	public static final class NpcLabel
	{
		public final NPC npc;
		public final String label;

		NpcLabel(NPC npc, String label)
		{
			this.npc = npc;
			this.label = label;
		}
	}
}
