package me.Postremus.WarGear.Arena;

import java.util.ArrayList;
import java.util.List;

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
		for (String element : this.plugin.getRepo().getArenaNames())
		{
			this.arenas.add(new Arena(this.plugin, element));
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
	
	public Arena getDefaultArena()
	{
		String defaultName = this.plugin.getRepo().getDefaultArenaName();
		for (Arena element : arenas)
		{
			if (element.getArenaName().equalsIgnoreCase(defaultName))
			{
				return element;
			}
		}
		return null;
	}
}
