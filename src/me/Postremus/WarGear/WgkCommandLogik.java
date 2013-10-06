package me.Postremus.WarGear;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.Postremus.WarGear.FightModes.KitMode;
import me.Postremus.WarGear.FightModes.PVPMode;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class WgkCommandLogik {

	WarGear plugin;
	Timer manuelTimer;
	int manuelCounter;
	TeamManager teams;
	IFightMode fightMode;
	Arena arena;
	
	public WgkCommandLogik(WarGear plugin)
	{
		this.plugin = plugin;
		this.teams = new TeamManager(this.plugin, this.arena);
		this.arena = new Arena(this.plugin);
		this.fightMode = new KitMode(this.teams, this.plugin, this.arena);
	}
	
	public void setup(CommandSender sender)
	{
		String arenaName = "";
		if (!(sender instanceof ConsoleCommandSender))
		{
			arenaName = this.plugin.getRepo().getArenaOfPlayer((Player)sender);
		}
		if (arenaName == "")
		{
			arenaName = this.plugin.getRepo().getDefaultArenaName();
		}
		this.setup(sender, arenaName);
	}
	
	public void setup(CommandSender sender, String arenaName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (!this.plugin.getRepo().getFightRunning())
		{
			this.plugin.getRepo().init();
			this.plugin.getRepo().setFightRunning(true);
			this.arena.setArenaName(arenaName);
			this.teams.setArena(this.arena);
			//fightmode muss wieder auf standard gesetzt werden
			//damit der TeamManager aktualisiert wird
			this.fightMode = new KitMode(this.teams, this.plugin, this.arena);
			sender.sendMessage("Setup für "+arenaName+" gestartet.");
			return;
		}
		sender.sendMessage("Es ist bereits ein fight setup gestartet worden.");
	}
	
	public void start(CommandSender sender)
	{
		if (!this.plugin.getRepo().getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein fight setup gestartet werden.");
			return;
		}
		if (this.plugin.getRepo().getKit() == null || this.plugin.getRepo().getKit().length() == 0)
		{
			sender.sendMessage("Es wurde kein Kit ausgewählt.");
			return;
		}
		if (this.teams.getTeamMembers().size() == 0)
		{
			sender.sendMessage("Es muss mindestens 1 Team geben.");
			return;
		}
		if (!this.fightMode.getName().equalsIgnoreCase(this.plugin.getRepo().getFightMode(this.arena)))
		{
			if (this.plugin.getRepo().getFightMode(this.arena).equalsIgnoreCase("pvp"))
			{
				this.fightMode = new PVPMode(this.teams, this.plugin, this.arena);
			}
			else if (this.plugin.getRepo().getFightMode(this.arena).equalsIgnoreCase("kit"))
			{
				this.fightMode = new KitMode(this.teams, this.plugin, this.arena);
			}
		}
		this.arena.setArenaOpeningFlags(false);
		this.teams.GenerateTeamOutput();
		this.fightMode.start();
	}
	
	public void setTeam(CommandSender sender, String teamName, List<String> teamMember)
	{
		if (!this.plugin.getRepo().getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein fight setup gestartet werden.");
			return;
		}
		List<Player> team = new ArrayList<Player>();
		for(String player : teamMember)
		{
			Player p = this.plugin.getServer().getPlayer(player);
			if (p == null)
			{
				sender.sendMessage(player + " ist nicht Online! Team wird nicht gesetzt.");
				return;
			}
			team.add(p);
		}
		if (teamName == "team1")
		{
			this.teams.setTeam(team, TeamNames.Team1);
		}
		else if (teamName == "team2")
		{
			this.teams.setTeam(team, TeamNames.Team2);
		}
	}
	
	public void setKit(CommandSender sender, String kitName)
	{
		if (!this.plugin.getRepo().getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein fight setup gestartet werden.");
			return;
		}
		if (!AdmincmdWrapper.existsKit(kitName, this.plugin.getServer()))
		{
			sender.sendMessage("Das Kit " + kitName + " gibt es nicht.");
			return;
		}
		this.plugin.getRepo().setKit(kitName);
	}
    
	public void setMode(String mode)
	{
		if (mode.equalsIgnoreCase("pvp"))
		{
			this.fightMode = new PVPMode(this.teams, this.plugin, this.arena);
		}
		else if (mode.equalsIgnoreCase("kit"))
		{
			this.fightMode = new KitMode(this.teams, this.plugin, this.arena);
		}
	}
	
	public void quit(CommandSender sender, String siegerTeam)
	{
		if (!this.plugin.getRepo().getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein fight setup gestartet werden.");
			return;
		}
		this.arena.close();
		this.plugin.getRepo().setFightRunning(false);
		this.fightMode.stop();
		if (siegerTeam.equalsIgnoreCase("Team1"))
		{
			this.teams.GenerateWinnerTeamOutput(TeamNames.Team1);
		}
		else
		{
			this.teams.GenerateWinnerTeamOutput(TeamNames.Team2);
		}
		this.teams.quitFight();
		this.fightMode = new KitMode(this.teams, this.plugin, this.arena);
	}
	
	public Arena getArena()
	{
		return this.arena;
	}
	
	public void setArenaName(String arena)
	{
		this.arena.setArenaName(arena);
	}
	
	public void StartManuelCountdown()
	{
		this.manuelCounter = 0;
		this.manuelTimer = new Timer();
		this.manuelTimer.schedule(new TimerTask(){
			@Override
	         public void run() {
				ManuelCountDown();          
	         }
		}, 0, 1000);
	}
	
	private void ManuelCountDown()
	{
		if (this.manuelCounter == 0)
		{
			this.plugin.getServer().broadcastMessage(ChatColor.GOLD + "40 Sekunden");
		}
		else if (this.manuelCounter == 10)
		{
			this.plugin.getServer().broadcastMessage(ChatColor.GOLD + "30 Sekunden");
		}
		else if (this.manuelCounter == 20)
		{
			this.plugin.getServer().broadcastMessage(ChatColor.GOLD + "20 Sekunden");
		}
		else if (this.manuelCounter == 25)
		{
			this.plugin.getServer().broadcastMessage(ChatColor.GOLD + "15 Sekunden");
		}
		else if (this.manuelCounter == 30)
		{
			this.plugin.getServer().broadcastMessage(ChatColor.GOLD + "10 Sekunden");
		}
		else if (this.manuelCounter > 30 && 40-this.manuelCounter > 3)
		{
			int diff = 40-this.manuelCounter;
			this.plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "" + diff +" Sekunden");
		}
		else if (this.manuelCounter > 36 && 40-this.manuelCounter > 0)
		{
			int diff = 40-this.manuelCounter;
			this.plugin.getServer().broadcastMessage(ChatColor.AQUA + ""+ diff +" Sekunden");
		}
		else if (40-this.manuelCounter == 0)
		{
			this.manuelTimer.cancel();
			this.plugin.getServer().broadcastMessage(ChatColor.DARK_GREEN + "GO!");
			this.arena.setArenaOpeningFlags(true);
		}
		this.manuelCounter++;
	}
}
