package de.hrc_gaming.wg.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import de.hrc_gaming.wg.WarGear;
import de.hrc_gaming.wg.team.TeamMember;
import de.hrc_gaming.wg.team.WgTeam;

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
		if (counter == 0)
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
		else if (diff > 4 && diff < 6)
		{
			this.arena.broadcastMessage(ChatColor.GOLD + ""+diff+" Sekunden");
		}
		else if (counter == time)
		{
			finishTeamSpectating(this.arena.getTeam().getTeam1());
			finishTeamSpectating(this.arena.getTeam().getTeam2());
			this.arena.broadcastMessage(ChatColor.AQUA + "Zeit vorbei!");
			task.cancel();
			this.arena.updateState(State.Resetting);
		}
		counter++;
	}

	private void prepareTeamSpectating(WgTeam team)
	{
		for (TeamMember member : team.getTeamMembers().values())
		{
			Player player = member.getPlayer();
			player.teleport(arena.getSpawnLocation(player));
			player.setGameMode(GameMode.CREATIVE);
		}
	}
	
	private void finishTeamSpectating(WgTeam team)
	{
		for (TeamMember member : team.getTeamMembers().values())
		{
			Player player = member.getPlayer();
			player.setGameMode(GameMode.SURVIVAL);
		}
	}
}
