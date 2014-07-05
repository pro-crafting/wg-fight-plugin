package de.pro_crafting.wg.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import de.pro_crafting.wg.OfflineRunable;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.team.TeamMember;
import de.pro_crafting.wg.team.WgTeam;

public class SpectatorMode {
	private WarGear plugin;
	private Arena arena;
	private int counter;
	private BukkitTask task;
	
	public SpectatorMode(WarGear plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		
	}

	public void start() {
		this.arena.broadcastMessage(ChatColor.GOLD+"Begutachtet die WarGears!");
		prepareTeamSpectating(this.arena.getTeam().getTeam1());
		prepareTeamSpectating(this.arena.getTeam().getTeam2());
		task = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable(){
			public void run()
			{
				spectateEndCountdown();
			}
		}, 0, 20);
	}

	private void spectateEndCountdown()
	{
		int time = this.arena.getRepo().getSpectatorModeTime();
		int diff = time - counter;
		if (counter == time)
		{
			finishTeamSpectating(this.arena.getTeam().getTeam1());
			finishTeamSpectating(this.arena.getTeam().getTeam2());
			this.arena.broadcastMessage(ChatColor.AQUA + "Zeit vorbei!");
			task.cancel();
			this.arena.updateState(State.Resetting);
		}
		else if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.GOLD+"Zeit endet in");
			this.arena.broadcastMessage(ChatColor.GOLD + ""+time+" Sekunden");
		}
		else if (counter % 30 == 0 && counter < time)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + ""+diff+" Sekunden");
		}
		else if (diff > 0 && diff < 4)
		{
			this.arena.broadcastMessage(ChatColor.AQUA + ""+diff+" Sekunden");
		}
		else if (diff > 3 && diff < 6)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + ""+diff+" Sekunden");
		}
		counter++;  
	}

	private void prepareTeamSpectating(WgTeam team)
	{
		OfflineRunable teamSpectatingPreparer = new OfflineRunable() {
			public void run(TeamMember member) {
				Player player = member.getPlayer();
				arena.teleport(player);
				Util.enableFly(player);
			}
		};
		for (TeamMember member : team.getTeamMembers().values())
		{
			this.plugin.getOfflineManager().run(teamSpectatingPreparer, member);
		}
	}
	
	private void finishTeamSpectating(WgTeam team)
	{
		OfflineRunable teamSpactatingFinisher = new OfflineRunable() {
			public void run(TeamMember member) {
				Player player = member.getPlayer();
				Util.disableFly(player);
			}
		};
		for (TeamMember member : team.getTeamMembers().values())
		{
			this.plugin.getOfflineManager().run(teamSpactatingFinisher, member);
		}
	}
}
