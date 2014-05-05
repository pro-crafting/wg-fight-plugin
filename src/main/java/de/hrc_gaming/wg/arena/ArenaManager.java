package de.hrc_gaming.wg.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import de.hrc_gaming.wg.WarGear;

public class ArenaManager {

	private WarGear plugin;
	private Map<String, Arena> arenas;
	
	public ArenaManager(WarGear plugin)
	{
		this.plugin = plugin;
		this.arenas = new HashMap<String, Arena>();
		this.loadArenas();
	}
	
	public void loadArenas()
	{
		this.arenas.clear();
		for (String element : this.plugin.getRepo().getArenaNames())
		{
			Arena toAdd = new Arena(this.plugin, element);
			if (toAdd.load())
			{
				this.arenas.put(element.toLowerCase(), toAdd);
			}
		}
	}
	
	public void loadArena(String name)
	{
		if (this.getArena(name) != null)
		{
			return;
		}
		Arena arena = new Arena(this.plugin, name);
		if (arena.load())
		{
			this.arenas.put(name.toLowerCase(), arena);
		}
	}
	
	public void unloadArenas()
	{
		for (Arena arena : this.arenas.values())
		{
			arena.unload();
		}
		this.arenas.clear();
	}
	
	public void unloadArena(String name)
	{
		Arena arena = this.getArena(name);
		if (arena != null)
		{
			arena.unload();
			this.arenas.remove(name.toLowerCase());
		}
	}
	
	public void saveArenas()
	{
		for (Arena arena : this.arenas.values())
		{
			arena.getRepo().save();
		}
	}
	
	public Arena getArena(String name)
	{
		return arenas.get(name.toLowerCase());
	}
	
	public Map<String, Arena> getArenas()
	{
		return this.arenas;
	}
	
	public boolean isArenaLoaded(String arenaName)
	{
		return getArena(arenaName) != null;
	}
	
	public Arena getArenaAtLocation(Location loc)
	{
		for (Arena arena : this.arenas.values())
		{
			if (arena.contains(loc))
			{
				return arena;
			}
		}
		return null;
	}
}
