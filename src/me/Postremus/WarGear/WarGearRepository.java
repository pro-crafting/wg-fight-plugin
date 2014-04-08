package me.Postremus.WarGear;
import java.util.ArrayList;
import java.util.List;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Team.TeamNames;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WarGearRepository {
	
	private WarGear plugin;
	
	public WarGearRepository(WarGear plugin)
	{
		this.plugin = plugin;
	}
	
	public String getDefaultKitName()
	{
		return this.plugin.getConfig().getString("general.kit");
	}

	public boolean getIsEconomyEnabled()
	{
		return this.plugin.getConfig().getBoolean("general.economy.enabled", false);
	}
	
	public double getWinAmount()
	{
		return this.plugin.getConfig().getDouble("general.economy.win", 2.5);
	}
	
	public double getLoseAmount()
	{
		return this.plugin.getConfig().getDouble("general.economy.lose", -2.5);
	}
	
	public double getDrawAmount()
	{
		return this.plugin.getConfig().getDouble("general.economy.draw", 1);
	}
	
	public boolean getIsPrefixEnabled()
	{
		return this.plugin.getConfig().getBoolean("general.enable-prefix", true);
	}
	
	public Location getWarpForTeam(TeamNames team, Arena arena)
	{
		if (team == TeamNames.Team1)
		{
			return arena.getRepo().getTeam1Warp();
		}
		else
		{
			return arena.getRepo().getTeam2Warp();
		}
	}
	
	public WorldGuardPlugin getWorldGuard() {
		return (WorldGuardPlugin) this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	}
	
	public WorldEditPlugin getWorldEdit()
	{
		return (WorldEditPlugin)this.plugin.getServer().getPluginManager().getPlugin("WorldEdit");
	}
	
	public List<String> getArenaNames()
	{
		List<String> ret = new ArrayList<String>();
		for (String arenaName : this.plugin.getConfig().getConfigurationSection("arenas").getKeys(false)) 
		{
			if (arenaName != null)
			{
				ret.add(arenaName);
			}
		}
		return ret;
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
