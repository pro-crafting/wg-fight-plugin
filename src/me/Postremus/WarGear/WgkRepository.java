package me.Postremus.WarGear;
import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WgkRepository {
	
	private WarGear plugin;
	private String kitName;
	private Boolean fightRunning;
	
	public WgkRepository(WarGear plugin)
	{
		this.plugin = plugin;
		this.init();
	}

	public void init()
	{
		kitName = "";
		fightRunning = false;
	}
	
	public String getWorldName(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".world");
	}
	
	public String getEndWarpPointName(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".warpFightEnd");
	}
	
	public String getFightStartWarpPointTeam1(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".warpFightStart.Team1");
	}
	
	public String getFightStartWarpPointTeam2(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".warpFightStart.Team2");
	}
	
	public String getRegionNameTeam1(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".regions.Team1");
	}
	
	public String getRegionNameTeam2(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".regions.Team2");
	}
	
	public String getFightMode(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".mode");
	}
	
	public int getGroundHeight(Arena arena)
	{
		int ret = 4;
		try
		{
			ret = Integer.parseInt(this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".groundHeight"));
			return ret;
		}
		catch(NumberFormatException ex)
		{
			System.out.println("[WarGear]Die Grounheight Option in der Config muss eine Ganzzahl sein für die Arena" + arena.getArenaName());
			System.out.println("[WarGear]Benutze 4 als GroundHeight");
			return 4;
		}
	}
	
	public String getDefaultArenaName()
	{
		return this.plugin.config.getString("wgk.defaults.arena");
	}
	
	public String getArenaRegion(Arena arena)
	{
		return this.plugin.config.getString("wgk.arenas."+arena.getArenaName()+".arenaRegion");
	}
	
	public Boolean existsArena(String arena)
	{
		for (String arenaName : this.plugin.config.getConfigurationSection("wgk.arenas").getKeys(false)) 
		{
			if (arenaName.equalsIgnoreCase(arena))
			{
				return true;
			}
		}
		return false;
	}
	
	public String getRegionForTeam(TeamNames team, Arena arena)
	{
		if (team == TeamNames.Team1)
		{
			return this.getFightStartWarpPointTeam1(arena);
		}
		else
		{
			return this.getFightStartWarpPointTeam2(arena);
		}
	}
	
	public void setFightRunning(Boolean state)
	{
		this.fightRunning = state;
	}
	
	public Boolean getFightRunning()
	{
		return this.fightRunning;
	}
	
	public void setKit(String kitName)
	{
		this.kitName = kitName;
	}
	
	public String getKit()
	{
		return this.kitName;
	}
	
	public WorldGuardPlugin getWorldGuard() {
	    Plugin wgPlugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) wgPlugin;
	}
	
	public List<String> getArenaRegions()
	{
		List<String> ret = new ArrayList<String>();
		for (String arenaName : this.plugin.config.getConfigurationSection("wgk.arenas").getKeys(false)) 
		{
			String toAdd = this.plugin.config.getString("wgk.arenas."+arenaName+".arenaRegion");
			if (toAdd != null)
			{
				ret.add(toAdd);
			}
		}
		return ret;
	}
	
	public String getArenaOfPlayer(Player player)
	{
		List<String> arenas = this.getArenaRegions();
		WorldGuardPlugin wgPlugin = this.getWorldGuard();
		RegionManager manager = wgPlugin.getRegionManager(player.getWorld());
		for (String arenaName : arenas)
		{
			ProtectedRegion r = manager.getRegion(arenaName);
			if (r.contains(BukkitUtil.toVector(player.getLocation())))
			{
				return arenaName;
			}
		}
		return "";
	}
	
	public List<Player> getPlayerOfRegion(String regionName, String worldName)
	{
		WorldGuardPlugin wgPlugin = this.getWorldGuard();
		RegionManager manager = wgPlugin.getRegionManager(this.plugin.getServer().getWorld(worldName));
		List<Player> ret = new ArrayList<Player>();
		ProtectedRegion r = manager.getRegion(regionName);
		for (Player player : this.plugin.getServer().getOnlinePlayers())
		{
			if (r.contains(BukkitUtil.toVector(player.getLocation())))
			{
				ret.add(player);
			}
		}
		return ret;
	}
}
