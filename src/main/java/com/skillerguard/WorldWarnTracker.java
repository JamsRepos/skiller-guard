package com.skillerguard;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
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
import net.runelite.api.GameState;

@Singleton
public class WorldWarnTracker
{
	private final Client client;
	private final Map<Integer, NpcLabel> npcs = new HashMap<>();
	private final Map<Long, ObjectLabel> objects = new HashMap<>();

	@Inject
	WorldWarnTracker(Client client)
	{
		this.client = client;
	}

	public Map<Integer, NpcLabel> getNpcs()
	{
		return npcs;
	}

	public Map<Long, ObjectLabel> getObjects()
	{
		return objects;
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
		track(event.getGameObject());
	}

	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		objects.remove(key(event.getGameObject()));
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
					track(object);
				}
			}
		}
	}

	public void clear()
	{
		npcs.clear();
		objects.clear();
	}

	private void track(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		String label = NamedObjectCatalog.labelFor(resolvedObjectId(object.getId()));
		if (label == null)
		{
			objects.remove(key(object));
			return;
		}
		objects.put(key(object), new ObjectLabel(object, label));
	}

	private void track(NPC npc)
	{
		if (npc == null)
		{
			return;
		}
		String label = NamedNpcCatalog.labelFor(npc.getId());
		if (label == null)
		{
			npcs.remove(npc.getIndex());
			return;
		}
		npcs.put(npc.getIndex(), new NpcLabel(npc, label));
	}

	private int resolvedObjectId(int id)
	{
		try
		{
			ObjectComposition def = client.getObjectDefinition(id);
			if (def != null && def.getImpostorIds() != null)
			{
				ObjectComposition impostor = def.getImpostor();
				if (impostor != null)
				{
					return impostor.getId();
				}
			}
		}
		catch (Exception ignored)
		{
			// Impostor lookup needs varbits that are not always present.
		}
		return id;
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

	public static final class ObjectLabel
	{
		public final TileObject object;
		public final String label;

		ObjectLabel(TileObject object, String label)
		{
			this.object = object;
			this.label = label;
		}
	}
}
