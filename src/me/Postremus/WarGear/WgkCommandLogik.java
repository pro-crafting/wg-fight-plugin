package me.Postremus.WarGear;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Arena.ArenaManager;
import me.Postremus.WarGear.Events.FightQuitEvent;
import me.Postremus.WarGear.FightModes.ChestMode;
import me.Postremus.WarGear.FightModes.KitMode;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WgkCommandLogik implements Listener{

	WarGear plugin;
	Timer manuelTimer;
	int manuelCounter;
	ArenaManager arena;
	
	public WgkCommandLogik(WarGear plugin)
	{
		this.plugin = plugin;
		this.arena = new ArenaManager(this.plugin);
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
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
		if (this.arena.getArena(arenaName).getFightState() == FightState.Idle)
		{
			this.plugin.getRepo().init();
			this.arena.getArena(arenaName).updateFightState(FightState.Setup);
			sender.sendMessage("Setup für "+arenaName+" gestartet.");
			return;
		}
		sender.sendMessage("Es ist bereits ein Fight Setup in der Arena "+ arenaName + " gestartet worden.");
	}
	
	public void addTeamLeader(CommandSender sender, String arenaName, String playerName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (this.arena.getArena(arenaName).getFightState() == FightState.Idle)
		{
			this.setup(sender, arenaName);
		}
		if (this.arena.getArena(arenaName).getFightState() == FightState.Running)
		{
			sender.sendMessage("Hier läuft bereits ein Fight.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			sender.sendMessage(playerName +"ist kein Spieler.");
			return;
		}
		WgTeam team = this.arena.getArena(arenaName).getTeam().getTeamWithOutLeader();
		if (team == null)
		{
			p.sendMessage("Beide Team's haben einen Teamleiter.");
			return;
		}
		team.add(p, true);
		p.teleport(this.plugin.getRepo().getWarpForTeam(team.getTeamName(), this.arena.getArena(arenaName)));
		p.sendMessage("Mit \"/wgk team add <spieler>\" fügst du Spieler zu deinem Team hinzu.");
		p.sendMessage("Mit \"/wgk team remove <spieler>\" entfernst du Spieler aus deinem Team.");
		p.sendMessage("Mit \"/wgk team ready\" schaltest du dein Team bereit.");
	}
	
	public void addTeamMember(CommandSender sender, String arenaName, String playerName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (this.arena.getArena(arenaName).getFightState() == FightState.Running)
		{
			sender.sendMessage("Während eines Fights kannst du keine Mitglieder hinzufügen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			sender.sendMessage(playerName +"ist kein Spieler.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Der Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = this.arena.getArena(arenaName).getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).getIsTeamLeader())
		{
			senderPlayer.sendMessage("Der Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		if (isAnywhereInTeam(p))
		{
			senderPlayer.sendMessage(p.getName()+" ist bereits in einen Team.");
			return;
		}
		team.add(p, false);
		p.sendMessage("Du bist jetzt im Team von "+senderPlayer.getName()+".");
		p.sendMessage("Mit \"/wgk team leave\" verlässt du das Team.");
	}
	
	public void removeTeamMember(CommandSender sender, String arenaName, String playerName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (this.arena.getArena(arenaName).getFightState() == FightState.Running)
		{
			sender.sendMessage("Während eines Fights kannst du keine Mitglieder entfernen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			sender.sendMessage(playerName +"ist kein Spieler.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Der Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = this.arena.getArena(arenaName).getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).getIsTeamLeader())
		{
			senderPlayer.sendMessage("Der Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		if (team.getTeamMember(p) == null)
		{
			senderPlayer.sendMessage(p.getName()+" ist nicht in deinem Team.");
			return;
		}
		if (senderPlayer.getName().equalsIgnoreCase(playerName))
		{
			senderPlayer.sendMessage("Der Team Leiter kann sich nicht rauswerfen.");
			return;
		}
		team.remove(p);
		p.sendMessage("Du bist nicht mehr in einen Team.");
	}
	
	private boolean isAnywhereInTeam(Player p)
	{
		for (Arena currArena : this.arena.getArenas())
		{
			if (currArena.getTeam().getTeamOfPlayer(p)!= null)
			{
				return true;
			}
		}
		return false;
	}
	
	public void leaveTeam(CommandSender sender, String arenaName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (this.arena.getArena(arenaName).getFightState() == FightState.Running)
		{
			sender.sendMessage("Während eines Fights kannst du nicht aus deinem Team raus.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Der Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = this.arena.getArena(arenaName).getTeam().getTeamOfPlayer(senderPlayer);
		team.remove(senderPlayer);
	}
	
	public void readifyTeam(CommandSender sender, String arenaName)
	{
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (this.arena.getArena(arenaName).getFightState() == FightState.Running)
		{
			sender.sendMessage("Während eines Fights kannst du keine Mitglieder entfernen.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Der Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = this.arena.getArena(arenaName).getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).getIsTeamLeader())
		{
			senderPlayer.sendMessage("Der Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		team.setIsReady(!team.getIsReady());
		if (team.getIsReady())
		{
			senderPlayer.sendMessage("Dein Team ist bereit.");
			if (this.arena.getArena(arenaName).getTeam().areBothTeamsReady())
			{
				this.start(sender, arenaName);
			}
		}
		else
		{
			senderPlayer.sendMessage("Dein Team ist nicht mehr bereit.");
		}
	}
	
	public void start(CommandSender sender, String arenaName)
	{
		
		if (!this.plugin.getRepo().existsArena(arenaName))
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		if (this.arena.getArena(arenaName).getFightState() != FightState.Setup)
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
		this.arena.getArena(arenaName).updateFightState(FightState.Running);
	}
	
	public void setKit(CommandSender sender, String kitName, String arenaName)
	{
		if (this.arena.getArena(arenaName).getFightState() != FightState.Setup)
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
    
	@EventHandler (priority = EventPriority.LOWEST)
	public void quit(FightQuitEvent event)
	{
		event.getArena().close();
		event.getArena().updateFightState(FightState.Idle);
		event.getArena().getFightMode().stop();
		event.getArena().getTeam().GenerateWinnerTeamOutput(event.getWinnerTeam().getTeamName());
		event.getArena().getTeam().quitFight();
		event.getArena().setFightMode(new KitMode(this.plugin, event.getArena()));
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
		sender.sendMessage("BodenSchematic: " + this.plugin.getRepo().getGroundSchematicName(arena));
		sender.sendMessage("Region Team1: " + this.plugin.getRepo().getRegionNameTeam1(arena));
		sender.sendMessage("Region Team2: " + this.plugin.getRepo().getRegionNameTeam2(arena));
		sender.sendMessage("Warp Team1: " + getStringFromLocation(this.plugin.getRepo().getFightStartWarpPointTeam1(arena)));
		sender.sendMessage("Warp Team2: " + getStringFromLocation(this.plugin.getRepo().getFightStartWarpPointTeam2(arena)));
		sender.sendMessage("Warp Fight Ende: " + getStringFromLocation(this.plugin.getRepo().getEndWarpPoint(arena)));
	}
	
	public void resetArena(CommandSender sender, String arenaName)
	{
		Arena arena = this.arena.getArena(arenaName);
		if (arena == null)
		{
			sender.sendMessage("Die Arena "+ arenaName+" existiert nicht.");
			return;
		}
		try
		{
			this.arena.getArena(arenaName).getReseter().reset();
		}
		catch(Exception ex)
		{
			sender.sendMessage("Arena " + arenaName + " konnte nicht geresetet werden.");
			ex.printStackTrace();
		}
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
