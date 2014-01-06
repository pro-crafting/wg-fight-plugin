package me.Postremus.WarGear;
import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.TeamNames;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WgkRepository {
	
	private WarGear plugin;
	
	public WgkRepository(WarGear plugin)
	{
		this.plugin = plugin;
		this.init();
	}

	public void init()
	{
	}
	
	public String getWorldName(Arena arena)
	{
		return this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".world");
	}
	
	public Location getEndWarpPoint(Arena arena)
	{
		World world = this.plugin.getServer().getWorld(this.getWorldName(arena));
		return this.loadLocationFromConfig("wgk.arenas."+arena.getArenaName()+".warpFightEnd", world);
	}
	
	public Location getFightStartWarpPointTeam1(Arena arena)
	{
		World world = this.plugin.getServer().getWorld(this.getWorldName(arena));
		return this.loadLocationFromConfig("wgk.arenas."+arena.getArenaName()+".warpFightStart.Team1", world);
	}
	
	public Location getFightStartWarpPointTeam2(Arena arena)
	{
		World world = this.plugin.getServer().getWorld(this.getWorldName(arena));
		return this.loadLocationFromConfig("wgk.arenas."+arena.getArenaName()+".warpFightStart.Team2", world);
	}
	
	public String getGroundSchematicName(Arena arena)
	{
		return this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".groundschematic");
	}
	
	private Location loadLocationFromConfig(String node, World world)
	{
		int x = this.plugin.getConfig().getInt(node+".x");
		int y = this.plugin.getConfig().getInt(node+".y");
		int z = this.plugin.getConfig().getInt(node+".z");
		return new Location(world, x, y, z);
	}
	
	public String getRegionNameTeam1(Arena arena)
	{
		return this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".regions.Team1");
	}
	
	public String getRegionNameTeam2(Arena arena)
	{
		return this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".regions.Team2");
	}
	
	public String getFightMode(Arena arena)
	{
		return this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".mode");
	}
	
	public int getGroundHeight(Arena arena)
	{
		int ret = 4;
		try
		{
			ret = Integer.parseInt(this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".groundHeight"));
			return ret;
		}
		catch(NumberFormatException ex)
		{
			System.out.println("[WarGear]Die Grounheight Option in der Config muss eine Ganzzahl sein für die Arena" + arena.getArenaName());
			return 4;
		}
	}
	
	public String getDefaultArenaName()
	{
		return this.plugin.getConfig().getString("wgk.defaults.arena");
	}
	
	public String getDefaultKitName()
	{
		return this.plugin.getConfig().getString("wgk.defaults.kit");
	}
	
	public String getArenaRegion(Arena arena)
	{
		return this.plugin.getConfig().getString("wgk.arenas."+arena.getArenaName()+".arenaRegion");
	}
	
	public boolean getAutoReset(Arena arena)
	{
		return this.plugin.getConfig().getBoolean("wgk.arenas."+arena.getArenaName()+".auto-reset", true);
	}
	
	public Boolean existsArena(String arena)
	{
		for (String arenaName : this.plugin.getConfig().getConfigurationSection("wgk.arenas").getKeys(false)) 
		{
			if (arenaName.equalsIgnoreCase(arena))
			{
				return true;
			}
		}
		return false;
	}
	
	public Location getWarpForTeam(TeamNames team, Arena arena)
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
	
	public WorldGuardPlugin getWorldGuard() {
	    Plugin wgPlugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) wgPlugin;
	}
	
	public WorldEditPlugin getWorldEdit()
	{
		Plugin wgPlugin = this.plugin.getServer().getPluginManager().getPlugin("WorldEdit");
		 
	    // WorldGuard may not be loaded
	    if (wgPlugin == null || !(wgPlugin instanceof WorldEditPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldEditPlugin) wgPlugin;
	}
	
	public List<String> getArenaNames()
	{
		List<String> ret = new ArrayList<String>();
		for (String arenaName : this.plugin.getConfig().getConfigurationSection("wgk.arenas").getKeys(false)) 
		{
			if (arenaName != null)
			{
				ret.add(arenaName);
			}
		}
		return ret;
	}
	
	public String getArenaAtLocation(Location loc)
	{
		List<String> arenas = this.getArenaNames();
		WorldGuardPlugin wgPlugin = this.getWorldGuard();
		RegionManager manager = wgPlugin.getRegionManager(loc.getWorld());
		for (String arenaName : arenas)
		{
			String arenaRegion = this.plugin.getConfig().getString("wgk.arenas."+arenaName+".arenaRegion");
			ProtectedRegion r = manager.getRegion(arenaRegion);
			if (r != null && r.contains(BukkitUtil.toVector(loc)))
			{
				return arenaName;
			}
		}
		return "";
	}
	
	public List<Player> getPlayerOfRegion(ProtectedRegion region)
	{
		List<Player> ret = new ArrayList<Player>();
		for (Player player : this.plugin.getServer().getOnlinePlayers())
		{
			if (region.contains(BukkitUtil.toVector(player.getLocation())))
			{
				ret.add(player);
			}
		}
		return ret;
	}
}
