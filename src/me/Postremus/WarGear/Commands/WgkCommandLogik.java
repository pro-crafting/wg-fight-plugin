package me.Postremus.WarGear.Commands;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.Postremus.WarGear.DrawReason;
import me.Postremus.WarGear.FightState;
import me.Postremus.WarGear.TeamWinReason;
import me.Postremus.WarGear.WarGear;
import me.Postremus.WarGear.Arena.Arena;
import me.Postremus.WarGear.Arena.ArenaManager;
import me.Postremus.WarGear.Events.DrawQuitEvent;
import me.Postremus.WarGear.Events.FightQuitEvent;
import me.Postremus.WarGear.Events.TeamWinQuitEvent;
import me.Postremus.WarGear.FightModes.ChestMode;
import me.Postremus.WarGear.FightModes.KitMode;
import me.Postremus.WarGear.Team.WgTeam;

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
	
	public WgkCommandLogik(WarGear plugin)
	{
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	public void addTeamLeader(CommandSender sender, String arenaName, String playerName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() == FightState.Idle)
		{
			arena.updateFightState(FightState.Setup);
		}
		if (arena.getFightState() == FightState.Running)
		{
			sender.sendMessage("§cHier läuft bereits ein Fight.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			sender.sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		WgTeam team =arena.getTeam().getTeamWithOutLeader();
		if (team == null)
		{
			p.sendMessage("§cBeide Team's haben einen Teamleiter.");
			return;
		}
		team.add(p, true);
		p.teleport(this.plugin.getRepo().getWarpForTeam(team.getTeamName(), arena));
		p.sendMessage("§7Mit §B\"/wgk team add <spieler>\" §7fügst du Spieler zu deinem Team hinzu.");
		p.sendMessage("§7Mit §B\"/wgk team remove <spieler>\" §7entfernst du Spieler aus deinem Team.");
		p.sendMessage("§7Mit §B\"/wgk team ready\" §7schaltest du dein Team bereit.");
		arena.getScore().addTeamMember(team.getTeamMember(p), team.getTeamName());
	}
	
	public void addTeamMember(CommandSender sender, String arenaName, String playerName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() == FightState.Running)
		{
			sender.sendMessage("§cWährend eines Fights kannst du keine Mitglieder hinzufügen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			sender.sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("§cDer Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = arena.getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).getIsTeamLeader())
		{
			senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		if (isAnywhereInTeam(p))
		{
			senderPlayer.sendMessage("§c"+p.getName()+" ist bereits in einen Team.");
			return;
		}
		team.add(p, false);
		p.sendMessage("§7Du bist jetzt im Team von §B"+senderPlayer.getName()+".");
		p.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
		arena.getScore().addTeamMember(team.getTeamMember(p), team.getTeamName());
	}
	
	public void removeTeamMember(CommandSender sender, String arenaName, String playerName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() == FightState.Running)
		{
			sender.sendMessage("§Während eines Fights kannst du keine Mitglieder entfernen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			sender.sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("§cDer Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = arena.getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).getIsTeamLeader())
		{
			senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		if (team.getTeamMember(p) == null)
		{
			senderPlayer.sendMessage("§c"+p.getName()+" ist nicht in deinem Team.");
			return;
		}
		if (senderPlayer.getName().equalsIgnoreCase(playerName))
		{
			senderPlayer.sendMessage("§cDer Team Leiter kann sich nicht rauswerfen.");
			return;
		}
		p.sendMessage("§7Du bist nicht mehr im Team von §8."+senderPlayer.getName());
		arena.getScore().removeTeamMember(team.getTeamMember(p), team.getTeamName());
		team.remove(p);
	}
	
	private boolean isAnywhereInTeam(Player p)
	{
		for (Arena currArena : this.plugin.getArenaManager().getArenas())
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
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() == FightState.Running)
		{
			sender.sendMessage("§cWährend eines Fights kannst du nicht aus deinem Team raus.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("§cDer Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		if (!isAnywhereInTeam(senderPlayer))
		{
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		WgTeam team = arena.getTeam().getTeamOfPlayer(senderPlayer);
		arena.getScore().removeTeamMember(team.getTeamMember(senderPlayer), team.getTeamName());
		team.remove(senderPlayer);
		senderPlayer.sendMessage("§7Du bist raus aus dem Team.");
	}
	
	public void readifyTeam(CommandSender sender, String arenaName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() == FightState.Running)
		{
			sender.sendMessage("§cWährend eines Fights kannst du keine Mitglieder entfernen.");
			return;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage("§cDer Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)sender;
		WgTeam team = arena.getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).getIsTeamLeader())
		{
			senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		team.setIsReady(!team.getIsReady());
		if (team.getIsReady())
		{
			senderPlayer.sendMessage("§7Dein Team ist bereit.");
			if (arena.getTeam().areBothTeamsReady())
			{
				this.start(sender, arenaName);
			}
		}
		else
		{
			senderPlayer.sendMessage("§7Dein Team ist nicht mehr bereit.");
		}
	}
	
	public void start(CommandSender sender, String arenaName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() != FightState.Setup)
		{
			sender.sendMessage("§cEs muss zuerst ein Fight Setup gestartet werden.");
			return;
		}
		if (arena.getKit() == null || arena.getKit().length() == 0)
		{
			if (this.plugin.getRepo().getDefaultKitName() == null || this.plugin.getRepo().getDefaultKitName().length() == 0)
			{
				sender.sendMessage("§cEs wurde kein Kit ausgewählt oder ein Standard Kit angegeben.");
				return;
			}
			else
			{
				arena.setKit(this.plugin.getRepo().getDefaultKitName());
			}
		}
		if (!arena.getFightMode().getName().equalsIgnoreCase(arena.getRepo().getFightMode()))
		{
			if (arena.getRepo().getFightMode().equalsIgnoreCase("kit"))
			{
				arena.setFightMode(new KitMode(this.plugin, arena));
			}
			else
			{
				arena.setFightMode(new ChestMode(this.plugin, arena));
			}
		}
		arena.setArenaOpeningFlags(false);
		arena.getTeam().GenerateTeamOutput();
		arena.getFightMode().start();
		arena.updateFightState(FightState.Running);
	}
	
	public void setKit(CommandSender sender, String kitName, String arenaName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		if (arena.getFightState() != FightState.Setup)
		{
			sender.sendMessage("§cEs muss zuerst ein Fight Setup gestartet werden.");
			return;
		}
		if (!this.plugin.getKitApi().existsKit(kitName))
		{
			sender.sendMessage("§cDas Kit " + kitName + " gibt es nicht.");
			return;
		}
		arena.setKit(kitName);
	}
    
	@EventHandler (priority = EventPriority.LOWEST)
	public void quit(FightQuitEvent event)
	{
		event.getArena().close();
		event.getArena().updateFightState(FightState.Idle);
		event.getArena().getFightMode().stop();
		if (event instanceof TeamWinQuitEvent)
		{
			TeamWinQuitEvent winEvent = (TeamWinQuitEvent)event;
			String toBroadcast = "";
			if (winEvent.getReason() == TeamWinReason.Death)
			{
				toBroadcast = ChatColor.DARK_GREEN + "Jeder aus dem ["+winEvent.getLooserTeam().getTeamName().toString().toUpperCase()+"] ist tot.";
			}
			event.getArena().broadcastMessage(toBroadcast);
			event.getArena().getTeam().GenerateWinnerTeamOutput(winEvent.getWinnerTeam().getTeamName());
		}
		else if (event instanceof DrawQuitEvent)
		{
			DrawQuitEvent drawEvent = (DrawQuitEvent)event;
			if (drawEvent.getReason() == DrawReason.Time)
			{
				event.getArena().broadcastMessage(ChatColor.DARK_GREEN + "Zeit abgelaufen - Unentschieden");
			}
		}
		event.getArena().getTeam().quitFight();
		event.getArena().setFightMode(new KitMode(this.plugin, event.getArena()));
	}
	
	public void showArenaNames(CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "---Verfügbare Arenen---");
		List<String> arenas = this.plugin.getRepo().getArenaNames();
		for (String arenaName : arenas)
		{
			sender.sendMessage(arenaName);
		}
	}
	
	public void showArenaInfo(CommandSender sender, String arenaName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		sender.sendMessage(ChatColor.GREEN + "---Arena Info---");
		sender.sendMessage(ChatColor.GRAY+"Arena Name: " + ChatColor.AQUA + arena.getArenaName());
		sender.sendMessage(ChatColor.GRAY+"Welt: " + ChatColor.AQUA + arena.getRepo().getWorld().getName());
		sender.sendMessage(ChatColor.GRAY+"Fight Modus: " + ChatColor.AQUA + arena.getRepo().getFightMode());
		sender.sendMessage(ChatColor.GRAY+"Bodenhöhe: " + ChatColor.AQUA + arena.getRepo().getGroundHeight());
		sender.sendMessage(ChatColor.GRAY+"BodenSchematic: " + ChatColor.AQUA + arena.getRepo().getGroundSchematic());
		sender.sendMessage(ChatColor.GRAY+"Auto Reset: " + ChatColor.AQUA + arena.getRepo().getAutoReset());
		sender.sendMessage(ChatColor.GRAY+"Region Team1: " + ChatColor.AQUA + arena.getRepo().getTeam1Region().getId());
		sender.sendMessage(ChatColor.GRAY+"Region Team2: " + ChatColor.AQUA + arena.getRepo().getTeam2Region().getId());
		sender.sendMessage(ChatColor.GRAY+"Warp Team1: " + ChatColor.AQUA + getStringFromLocation(arena.getRepo().getTeam1Warp()));
		sender.sendMessage(ChatColor.GRAY+"Warp Team2: " + ChatColor.AQUA + getStringFromLocation(arena.getRepo().getTeam2Warp()));
		sender.sendMessage(ChatColor.GRAY+"Warp Fight Ende: " + ChatColor.AQUA + getStringFromLocation(arena.getRepo().getFightEndWarp()));
	}
	
	public void resetArena(CommandSender sender, String arenaName)
	{
		Arena arena = this.plugin.getArenaManager().getArena(arenaName);
		try
		{
			arena.getReseter().reset();
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
