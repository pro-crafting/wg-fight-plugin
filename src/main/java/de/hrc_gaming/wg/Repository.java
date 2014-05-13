package de.hrc_gaming.wg;
import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Repository {
	private WarGear plugin;
	private Economy eco;
	
	public Repository(WarGear plugin)
	{
		this.plugin = plugin;
		this.eco = loadEco();
	}
	
	public String getDefaultKitName()
	{
		return this.plugin.getConfig().getString("kit");
	}

	public boolean isEconomyEnabled()
	{
		return this.plugin.getConfig().getBoolean("economy.enabled", false) &&
				this.getEco() != null;
	}
	
	public double getWinAmount()
	{
		return this.plugin.getConfig().getDouble("economy.win", 2.5);
	}
	
	public double getLoseAmount()
	{
		return this.plugin.getConfig().getDouble("economy.lose", -2.5);
	}
	
	public double getDrawAmount()
	{
		return this.plugin.getConfig().getDouble("economy.draw", 1);
	}
	
	public boolean isPrefixEnabled()
	{
		return this.plugin.getConfig().getBoolean("prefix-enabled", true);
	}
	
	public boolean areMetricsEnabled()
	{
		return this.plugin.getConfig().getBoolean("metrics", false);
	}
	
	public boolean isUpdateCheckEnabled()
	{
		return this.plugin.getConfig().getBoolean("update-check", false);
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
