package me.Postremus.WarGear;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Arena {
	WarGear plugin;
	String name;
	private TeamManager team;
	
	public Arena(WarGear plugin)
	{
		this.plugin = plugin;
		this.name = this.plugin.getRepo().getDefaultArenaName();
		this.team = new TeamManager(plugin, this);
	}

	public String getArenaName()
	{
		return this.name;
	}
	
	public void setArenaName(String arena)
	{
		this.name = arena;
	}
	
	public void open()
	{
		this.setArenaOpeningFlags(true);
		this.broadcastMessage(ChatColor.GREEN + "Arena "+this.name+" Freigegeben!");
	}
	
	public void close()
	{
		this.setArenaOpeningFlags(false);
		this.broadcastMessage(ChatColor.GREEN + "Arena "+this.name+" gesperrt!");
	}
	
	public void setArenaOpeningFlags(Boolean allowed)
	{
		String value = "allow";
		if (!allowed)
		{
			value = "deny";
		}
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.TNT, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.BUILD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.PVP, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam1(this), DefaultFlag.GHAST_FIREBALL, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.TNT, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.BUILD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.PVP, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.FIRE_SPREAD, value);
		setFlag(this.plugin.getRepo().getRegionNameTeam2(this), DefaultFlag.GHAST_FIREBALL, value);
	}
	
	public void setFlag(String RegionName, StateFlag flag, String value)
    {
    	WorldGuardPlugin worldGuard = this.plugin.getRepo().getWorldGuard();
		RegionManager regionManager = worldGuard.getRegionManager(this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this)));
		ProtectedRegion region = regionManager.getRegion(RegionName);
	    try {
			region.setFlag(flag, flag.parseInput(worldGuard, this.plugin.getServer().getConsoleSender(), value));
		} catch (InvalidFlagFormat e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void broadcastMessage(String message)
	{
		for (Player player : this.plugin.getRepo().getPlayerOfRegion(this.plugin.getRepo().getArenaRegion(this), this.plugin.getRepo().getWorldName(this)))
		{
			player.sendMessage(message);
		}
	}

	public TeamManager getTeam() {
		return team;
	}
}
