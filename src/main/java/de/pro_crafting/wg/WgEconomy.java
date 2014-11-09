package de.pro_crafting.wg;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.pro_crafting.wg.event.DrawQuitEvent;
import de.pro_crafting.wg.event.WinQuitEvent;
import de.pro_crafting.wg.group.TeamMember;
import de.pro_crafting.wg.group.WgTeam;

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
		for (TeamMember member : team.getTeamMembers())
		{
			if (amount < 0)
			{
				this.plugin.getRepo().getEco().withdrawPlayer(member.getOfflinePlayer(), amount);
			}
			else
			{
				this.plugin.getRepo().getEco().depositPlayer(member.getOfflinePlayer(), amount);
			}
		}
	}
}
