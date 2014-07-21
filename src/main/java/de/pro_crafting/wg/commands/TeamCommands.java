package de.pro_crafting.wg.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.pro_crafting.commandframework.Command;
import de.pro_crafting.commandframework.CommandArgs;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.team.TeamNames;
import de.pro_crafting.wg.team.WgTeam;

public class TeamCommands {
	private WarGear plugin;
	
	public TeamCommands(WarGear plugin)
	{
		this.plugin = plugin;
	}
	
	@Command(name = "wgk.team", aliases = { "wgk.team.help" }, description = "Zeigt die Hilfe an.", usage = "/wgk team", permission="wargear.team")
	public void team(CommandArgs args)
	{
		args.getSender().sendMessage("§c§LKein passender Befehl gefunden!");
		args.getSender().sendMessage("§B/wgk team leader <playername>");
		args.getSender().sendMessage("§B/wgk team add <playername>");
		args.getSender().sendMessage("§B/wgk team add <playername> <team1|team2>");
		args.getSender().sendMessage("§B/wgk team remove <playername>");
		args.getSender().sendMessage("§B/wgk team remove <playername> <team1|team2>");
		args.getSender().sendMessage("§B/wgk team leave");
	}
	
	@Command(name = "wgk.team.leader", description = "Setzt den Leiter eines Teams.", usage = "/wgk team leader", permission="wargear.team.leader")
	public void leader(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.getArgs().length == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs()[0];
		
		if (arena.getState() == State.Running || arena.getState() == State.PreRunning)
		{
			args.getSender().sendMessage("§cEs läuft bereits ein Fight in "+arena.getName()+".");
			return;
		}
		if (arena.getState() == State.Idle)
		{
			arena.updateState(State.Setup);
		}
		
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			args.getSender().sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		WgTeam team = arena.getTeam().getTeamWithOutLeader();
		if (team == null)
		{
			p.sendMessage("§cBeide Team's haben einen Teamleiter.");
			return;
		}
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null)
		{
			p.sendMessage("§c"+p.getName()+" ist bereits in einem Team.");
			return;
		}
		team.add(p, true);
		p.teleport(arena.getTeam().getTeamSpawn(team.getTeamName()));
		p.sendMessage("§7Mit §B\"/wgk team add <spieler>\" §7fügst du Spieler zu deinem Team hinzu.");
		p.sendMessage("§7Mit §B\"/wgk team remove <spieler>\" §7entfernst du Spieler aus deinem Team.");
		p.sendMessage("§7Mit §B\"/wgk team ready\" §7schaltest du dein Team bereit.");
		arena.getScore().addTeamMember(team.getTeamMember(p), team.getTeamName());
	}
	
	@Command(name = "wgk.team.add", description = "Fügt ein Spieler zu deinem Team hinzu.",
			usage = "/wgk team add", permission="wargear.team.add")
	public void add(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.getArgs().length == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs()[0];
		
		if (arena.getState() == State.Running || arena.getState() == State.PreRunning)
		{
			args.getSender().sendMessage("§cWährend eines Fightes kannst du keine Mitglieder hinzufügen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			args.getSender().sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		if (!(args.getSender() instanceof Player))
		{
			args.getSender().sendMessage("§cDer Command muss von einem Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)args.getSender();
		OfflinePlayer leader = null;
		WgTeam team = null;
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null)
		{
			senderPlayer.sendMessage("§c"+p.getName()+" ist bereits in einem Team.");
			return;
		}
		if (args.getArgs().length == 1 || !senderPlayer.hasPermission("wargear.team.add.other")) {
			team = arena.getTeam().getTeamOfPlayer(senderPlayer);
			if (team != null && team.getTeamMember(senderPlayer) != null && !team.getTeamMember(senderPlayer).isTeamLeader())
			{
				senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
				return;
			}
			leader = senderPlayer;
		} else {
			String teamString = args.getArgs()[1];
			TeamNames teamName = TeamNames.Team1;
			if (teamString.equalsIgnoreCase("team2")) {
				teamName = TeamNames.Team2;
			}
			team = arena.getTeam().getTeamOfName(teamName);
			leader = team.getTeamLeader().getOfflinePlayer();
			if (leader == null) {
				senderPlayer.sendMessage("§cDas Team hat keinen Leader.");
			}
		}
		team.add(p, false);
		p.sendMessage("§7Du bist jetzt im Team von §B"+leader.getName()+"§7.");
		p.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
		arena.getScore().addTeamMember(team.getTeamMember(p), team.getTeamName());
	}
	
	@Command(name = "wgk.team.remove", description = "Entfernt einen Spieler zu deinem Team hinzu.",
			usage = "/wgk team remove", permission="wargear.team.remove")
	public void remove(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.getArgs().length == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs()[0];
		
		if (arena.getState() == State.Running || arena.getState() == State.Running)
		{
			args.getSender().sendMessage("§Während eines Fightes kannst du keine Mitglieder entfernen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			args.getSender().sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		if (!(args.getSender() instanceof Player))
		{
			args.getSender().sendMessage("§cDer Command muss von einen Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)args.getSender();
		WgTeam team = null;
		if (args.getArgs().length == 1 || !senderPlayer.hasPermission("wargear.team.remove.other")) {
			team = arena.getTeam().getTeamOfPlayer(senderPlayer);
			if (!team.getTeamMember(senderPlayer).isTeamLeader())
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
		} else {
			String teamString = args.getArgs()[1];
			TeamNames teamName = TeamNames.Team1;
			if (teamString.equalsIgnoreCase("team2")) {
				teamName = TeamNames.Team2;
			}
			team = arena.getTeam().getTeamOfName(teamName);
		}
		p.sendMessage("§7Du bist nicht mehr im Team von §8."+senderPlayer.getName());
		arena.getScore().removeTeamMember(team.getTeamMember(p), team.getTeamName());
		team.remove(p);
	}
	
	@Command(name = "wgk.team.leave", description = "Entfernt dich aus dem Team.",
			usage = "/wgk team leave", permission="wargear.team.leave")
	public void leave(CommandArgs args)
	{
		if (!(args.getSender() instanceof Player))
		{
			args.getSender().sendMessage("§cDer Command muss von einem Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)args.getSender();
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(senderPlayer);
		if (arena == null)
		{
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		
		if (arena.getState() == State.Running || arena.getState() == State.PreRunning)
		{
			args.getSender().sendMessage("§cWährend eines Fightes kannst du nicht dein Team verlassen.");
			return;
		}
		WgTeam team = arena.getTeam().getTeamOfPlayer(senderPlayer);
		arena.getScore().removeTeamMember(team.getTeamMember(senderPlayer), team.getTeamName());
		team.remove(senderPlayer);
		senderPlayer.sendMessage("§7Du bist raus aus dem Team.");
	}
		
	@Command(name = "wgk.team.ready", description = "Schaltet dein Team bereit",
			usage = "/wgk team ready", permission="wargear.team.ready")
	public void ready(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}	
		
		if (arena.getState() == State.Running || arena.getState() == State.PreRunning)
		{
			args.getSender().sendMessage("§cWährend eines Fightes kannst du das Team nicht bereit schalten.");
			return;
		}
		if (!(args.getSender() instanceof Player))
		{
			args.getSender().sendMessage("§cDer Command muss von einem Spieler ausgeführt werden.");
			return;
		}
		Player senderPlayer = (Player)args.getSender();
		WgTeam team = arena.getTeam().getTeamOfPlayer(senderPlayer);
		if (!team.getTeamMember(senderPlayer).isTeamLeader())
		{
			senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
			return;
		}
		team.setIsReady(!team.isReady());
		if (team.isReady())
		{
			senderPlayer.sendMessage("§7Dein Team ist bereit.");
			if (arena.getTeam().areBothTeamsReady())
			{
				arena.startFight(args.getSender());
			}
		}
		else
		{
			senderPlayer.sendMessage("§7Dein Team ist nicht mehr bereit.");
		}
	}
}
