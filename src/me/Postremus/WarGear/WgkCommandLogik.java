package me.Postremus.WarGear;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.Postremus.WarGear.FightModes.ChestMode;
import me.Postremus.WarGear.FightModes.KitMode;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WgkCommandLogik {

	WarGear plugin;
	Timer manuelTimer;
	int manuelCounter;
	ArenaManager arena;
	
	public WgkCommandLogik(WarGear plugin)
	{
		this.plugin = plugin;
		this.arena = new ArenaManager(this.plugin);
	}
	
	public ArenaManager getArenaManager()
	{
		return this.arena;
	}
	
	public void setup(CommandSender sender, String arenaName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (!this.arena.getArena(arenaName).getFightRunning())
		{
			this.plugin.getRepo().init();
			this.arena.getArena(arenaName).setFightRunning(true);
			sender.sendMessage("Setup für "+arenaName+" gestartet.");
			return;
		}
		sender.sendMessage("Es ist bereits ein Fight Setup in der Arena "+ arenaName + " gestartet worden.");
	}
	
	public void start(CommandSender sender, String arenaName)
	{
		
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (!this.arena.getArena(arenaName).getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein Fight Setup gestartet werden.");
			return;
		}
		if (this.arena.getArena(arenaName).getKit() == null || this.arena.getArena(arenaName).getKit().length() == 0)
		{
			if (this.plugin.getRepo().getDefaultKitName() == null || this.plugin.getRepo().getDefaultKitName().length() == 0)
			{
				sender.sendMessage("Es wurde kein Kit ausgewählt oder ein standard Kit angegeben.");
				return;
			}
			else
			{
				this.arena.getArena(arenaName).setKit(this.plugin.getRepo().getDefaultKitName());
			}
		}
		if (this.arena.getArena(arenaName).getTeam().getTeamMembers().size() == 0)
		{
			sender.sendMessage("Es muss mindestens 1 Team geben.");
			return;
		}
		if (!this.arena.getArena(arenaName).getFightMode().getName().equalsIgnoreCase(this.plugin.getRepo().getFightMode(this.arena.getArena(arenaName))))
		{
			if (this.plugin.getRepo().getFightMode(this.arena.getArena(arenaName)).equalsIgnoreCase("kit"))
			{
				this.arena.getArena(arenaName).setFightMode(new KitMode(this.plugin, this.arena.getArena(arenaName)));
			}
			else
			{
				this.arena.getArena(arenaName).setFightMode(new ChestMode(this.plugin, this.arena.getArena(arenaName)));
			}
		}
		this.arena.getArena(arenaName).setArenaOpeningFlags(false);
		this.arena.getArena(arenaName).getTeam().GenerateTeamOutput();
		this.arena.getArena(arenaName).getFightMode().start();
	}
	
	public void setTeam(CommandSender sender, String teamName, List<String> teamMember, String arenaName)
	{
		if (!this.arena.getArena(arenaName).getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein Fight Setup gestartet werden.");
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
			this.arena.getArena(arenaName).getTeam().setTeam(team, TeamNames.Team1);
		}
		else if (teamName == "team2")
		{
			this.arena.getArena(arenaName).getTeam().setTeam(team, TeamNames.Team2);
		}
	}
	
	public void setKit(CommandSender sender, String kitName, String arenaName)
	{
		if (!this.arena.getArena(arenaName).getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein Fight Setup gestartet werden.");
			return;
		}
		if (!AdmincmdWrapper.existsKit(kitName, this.plugin.getServer()))
		{
			sender.sendMessage("Das Kit " + kitName + " gibt es nicht.");
			return;
		}
		this.arena.getArena(arenaName).setKit(kitName);
	}
    
	public void quit(CommandSender sender, String siegerTeam, String arenaName)
	{
		if (!this.arena.getArena(arenaName).getFightRunning())
		{
			sender.sendMessage("Es muss zuerst ein Fight Setup gestartet werden.");
			return;
		}
		this.arena.getArena(arenaName).close();
		this.arena.getArena(arenaName).setFightRunning(false);
		this.arena.getArena(arenaName).getFightMode().stop();
		if (siegerTeam.equalsIgnoreCase("Team1"))
		{
			this.arena.getArena(arenaName).getTeam().GenerateWinnerTeamOutput(TeamNames.Team1);
		}
		else
		{
			this.arena.getArena(arenaName).getTeam().GenerateWinnerTeamOutput(TeamNames.Team2);
		}
		this.arena.getArena(arenaName).getTeam().quitFight();
		this.arena.getArena(arenaName).setFightMode(new KitMode(this.plugin, this.arena.getArena(arenaName)));
	}
	
	public void showArenaNames(CommandSender sender)
	{
		sender.sendMessage("Verfügbare Arenen:");
		List<String> arenas = this.plugin.getRepo().getArenaNames();
		for (String arenaName : arenas)
		{
			sender.sendMessage(arenaName);
		}
	}
	
	public void showArenaInfo(CommandSender sender, String arenaName)
	{
		Arena arena = this.arena.getArena(arenaName);
		if (arena == null)
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		sender.sendMessage("---Arena Info---");
		sender.sendMessage("Arena Name: " + arena.getArenaName());
		sender.sendMessage("Welt: " + this.plugin.getRepo().getWorldName(arena));
		sender.sendMessage("Fight Modus: " + this.plugin.getRepo().getFightMode(arena));
		sender.sendMessage("Bodenhöhe: " + this.plugin.getRepo().getGroundHeight(arena));
		sender.sendMessage("Region Team1: " + this.plugin.getRepo().getRegionNameTeam1(arena));
		sender.sendMessage("Region Team2: " + this.plugin.getRepo().getRegionNameTeam2(arena));
		sender.sendMessage("Warp Team1: " + getStringFromLocation(this.plugin.getRepo().getFightStartWarpPointTeam1(arena)));
		sender.sendMessage("Warp Team2: " + getStringFromLocation(this.plugin.getRepo().getFightStartWarpPointTeam2(arena)));
		sender.sendMessage("Warp Fight Ende: " + getStringFromLocation(this.plugin.getRepo().getEndWarpPoint(arena)));
	}
	
	private String getStringFromLocation(Location loc)
	{
		String ret = "x: %d; y: %d; z: %d";
		return String.format(ret, new Object[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
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
		}
		this.manuelCounter++;
	}
}
