package de.hrc_gaming.wg;
import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.hrc_gaming.wg.arena.Arena;
import de.hrc_gaming.wg.team.TeamNames;

public class WarGearRepository {
	private WarGear plugin;
	private Economy eco;
	
	public WarGearRepository(WarGear plugin)
	{
		this.plugin = plugin;
		this.eco = loadEco();
	}
	
	public String getDefaultKitName()
	{
		return this.plugin.getConfig().getString("general.kit");
	}

	public boolean isEconomyEnabled()
	{
		return this.plugin.getConfig().getBoolean("general.economy.enabled", false) &&
				this.getEco() != null;
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
	
	public boolean isPrefixEnabled()
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
	
	public Economy getEco()
	{
		return this.eco;
	}
	
	
	
	private Economy loadEco()
	{
		if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return null;
		}
		RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
        	return economyProvider.getProvider();
        }
        return null;
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
