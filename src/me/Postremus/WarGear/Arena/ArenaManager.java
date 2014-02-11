package me.Postremus.WarGear.Arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.Postremus.WarGear.WarGear;

public class ArenaManager {

	private WarGear plugin;
	private List<Arena> arenas;
	
	public ArenaManager(WarGear plugin)
	{
		this.plugin = plugin;
		this.arenas = new ArrayList<Arena>();
		this.loadArenas();
	}
	
	public void loadArenas()
	{
		this.arenas.removeAll(this.arenas);
		for (String element : this.plugin.getRepo().getArenaNames())
		{
			Arena toAdd = new Arena(this.plugin, element);
			if (toAdd.load())
			{
				this.arenas.add(toAdd);
			}
		}
	}
	
	public void loadArena(String name)
	{
		Arena arena = this.getArena(name);
		if (arena != null)
		{
			return;
		}
		arena = new Arena(this.plugin, name);
		if (arena.load())
		{
			this.arenas.add(arena);
		}
	}
	
	public void unloadArenas()
	{
		for (Arena arena : this.arenas)
		{
			arena.unload();
		}
		this.arenas.removeAll(this.arenas);
	}
	
	public void unloadArena(String name)
	{
		Arena arena = this.getArena(name);
		if (arena != null)
		{
			arena.unload();
			this.arenas.remove(arena);
		}
	}
	
	public void saveArenas()
	{
		for (Arena arena : this.arenas)
		{
			arena.getRepo().save();
		}
	}
	
	public Arena getArena(String name)
	{
		for (Arena element : arenas)
		{
			if (element.getArenaName().equalsIgnoreCase(name))
			{
				return element;
			}
		}
		return null;
	}
	
	public List<Arena> getArenas()
	{
		return this.arenas;
	}
	
	public boolean isArenaLoaded(String arenaName)
	{
		for (Arena arena : this.arenas)
		{
			if (arena.getArenaName().equalsIgnoreCase(arenaName))
			{
				return true;
			}
		}
		return false;
	}
	
	public Arena getArenaAtLocation(Location loc)
	{
		for (Arena arena : this.arenas)
		{
			if (arena.contains(loc))
			{
				return arena;
			}
		}
		return null;
	}
}
