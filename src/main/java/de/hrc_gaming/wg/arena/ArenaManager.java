package de.hrc_gaming.wg.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
			loadArena(element);
		}
	}
	
	public void loadArena(String name)
	{
		this.plugin.getLogger().info("Lade Arena "+name);
		if (this.getArena(name) != null)
		{
			this.plugin.getLogger().info("Arena "+name+" ist bereits geladen.");
			return;
		}
		Arena arena = new Arena(this.plugin, name);
		if (arena.load())
		{
			this.plugin.getLogger().info("Arena "+name+" geladen.");
			this.arenas.put(name.toLowerCase(), arena);
			return;
		}
		this.plugin.getLogger().info("Arena "+name+" konnte nicht geladen werden.");
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
	
	public Arena getArenaOfTeamMember(OfflinePlayer player) {
		for (Arena arena : this.arenas.values())
		{
			if (arena.getTeam().getTeamMember(player) != null)
			{
				return arena;
			}
		}
		return null;
	}
}
