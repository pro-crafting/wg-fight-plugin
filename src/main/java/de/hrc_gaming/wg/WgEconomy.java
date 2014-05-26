package de.hrc_gaming.wg;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.hrc_gaming.wg.event.DrawQuitEvent;
import de.hrc_gaming.wg.event.WinQuitEvent;
import de.hrc_gaming.wg.team.WgTeam;

public class WgEconomy implements Listener{
	private WarGear plugin;
	
	public WgEconomy(WarGear plugin)
	{
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler (priority=EventPriority.LOWEST)
	public void handleQuitEvent(WinQuitEvent event)
	{
		this.giveTeamMoney(event.getWinnerTeam(), this.plugin.getRepo().getWinAmount());
		this.giveTeamMoney(event.getLooserTeam(), this.plugin.getRepo().getLoseAmount());
	}
	
	@EventHandler (priority=EventPriority.LOWEST)
	public void handleQuitEvent(DrawQuitEvent event)
	{
		this.giveTeamMoney(event.getTeam1(), this.plugin.getRepo().getDrawAmount());
		this.giveTeamMoney(event.getTeam2(), this.plugin.getRepo().getDrawAmount());
	}
	
	private void giveTeamMoney(WgTeam team, double amount)
	{
		for (UUID id : team.getTeamMembers().keySet())
		{
			if (amount < 0)
			{
				this.plugin.getRepo().getEco().withdrawPlayer(Bukkit.getPlayer(id), amount);
			}
			else
			{
				this.plugin.getRepo().getEco().depositPlayer(Bukkit.getPlayer(id), amount);
			}
		}
	}
}
