package me.Postremus.WarGear.FightModes;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.Postremus.WarGear.AdmincmdWrapper;
import me.Postremus.WarGear.Arena;
import me.Postremus.WarGear.IFightMode;
import me.Postremus.WarGear.TeamManager;
import me.Postremus.WarGear.TeamMember;
import me.Postremus.WarGear.WarGear;

public class PVPMode implements IFightMode{
	TeamManager teams;
	WarGear plugin;
	Arena arena;
	Timer timer;
	int counter;
	
	public PVPMode(TeamManager teams, WarGear plugin, Arena arena)
	{
		this.teams = teams;
		this.plugin = plugin;
		this.arena = arena;
	}
	
	@Override
	public void start() {
		this.plugin.getServer().broadcastMessage(ChatColor.YELLOW+"Gleich: PVP-Kampf in der "+this.arena.getArenaName()+" Arena");
		for (TeamMember player : this.teams.getTeamMembers())
		{
			player.getPlayer().getInventory().clear();
		    player.getPlayer().teleport(this.plugin.getRepo().getWarpForTeam(player.getTeam(), this.arena), TeleportCause.PLUGIN);
		    player.getPlayer().setGameMode(GameMode.CREATIVE);
		}
		this.arena.open();
		counter = 0;
		timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
	         public void run() {
				buildCountdown();          
	         }
		}, 0, 1000);
	}
	
	public void buildCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.YELLOW+"Bitte alle Teilnehmer ihre Hauptquartiere aufbauen");
			this.arena.broadcastMessage(ChatColor.YELLOW+"Bauphase endet in:");
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "5 Minuten");
		}
		else if (counter == 60)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "4 Minuten");
		}
		else if (counter == 120)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "3 Minuten");
		}
		else if (counter == 180)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "2 Minuten");
		}
		else if (counter == 240)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "1 Minute");
		}
		else if (counter == 270)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "30 Sekunden");
		}
		else if (counter == 290)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "10 Sekunden");
		}
		else if (counter > 290 && 300-counter > 3)
		{
			int diff = 300-counter;
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "" + diff +" Sekunden");
		}
		else if (counter > 296 && 300-counter > 0)
		{
			int diff = 300-counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+ diff +" Sekunden");
		}
		else if (300-counter == 0)
		{
			timer.cancel();
			for (TeamMember player : this.teams.getTeamMembers())
			{
				player.getPlayer().getInventory().clear();
				player.getPlayer().getInventory().setArmorContents(null);
			    AdmincmdWrapper.giveKit(this.plugin.getRepo().getKit(), player.getPlayer(), this.plugin.getServer());
			    
			    player.getPlayer().teleport(this.plugin.getRepo().getWarpForTeam(player.getTeam(), this.arena), TeleportCause.PLUGIN);
			    player.getPlayer().setGameMode(GameMode.SURVIVAL);
				AdmincmdWrapper.disableFly(player.getPlayer());
				AdmincmdWrapper.heal(player.getPlayer());
			}
			this.arena.open();
			counter = 0;
			timer = new Timer();
			timer.schedule(new TimerTask(){
				@Override
		         public void run() {
					fightCountdown();          
		         }
			}, 0, 1000);
			return;
		}
		counter++;
	}
	
	public void fightCountdown()
	{
		if (counter == 0)
		{
			this.arena.broadcastMessage(ChatColor.YELLOW+"Bitte alle Teilnehmer auf ihre Plätze");
			this.arena.broadcastMessage(ChatColor.YELLOW+"Fight startet in:");
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "40 Sekunden");
		}
		else if (counter == 10)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "30 Sekunden");
		}
		else if (counter == 20)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "20 Sekunden");
		}
		else if (counter == 25)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "15 Sekunden");
		}
		else if (counter == 30)
		{
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "10 Sekunden");
		}
		else if (counter > 30 && 40-counter > 3)
		{
			int diff = 40-counter;
			this.arena.broadcastMessage(ChatColor.DARK_GREEN + "" + diff +" Sekunden");
		}
		else if (counter > 36 && 40-counter > 0)
		{
			int diff = 40-counter;
			this.arena.broadcastMessage(ChatColor.AQUA + ""+ diff +" Sekunden");
		}
		else if (40-counter == 0)
		{
			timer.cancel();
			this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this.arena)).setDifficulty(Difficulty.EASY);
			this.arena.open();
		}
		counter++;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		this.plugin.getServer().getWorld(this.plugin.getRepo().getWorldName(this.arena)).setDifficulty(Difficulty.PEACEFUL);
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "pvp";
	}
}
