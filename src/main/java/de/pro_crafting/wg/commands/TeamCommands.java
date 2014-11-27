package de.pro_crafting.wg.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.pro_crafting.commandframework.Command;
import de.pro_crafting.commandframework.CommandArgs;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.group.PlayerRole;
import de.pro_crafting.wg.group.Group;

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
		args.getSender().sendMessage("§B/wgk team invite <playername> <team1|team2>");
		args.getSender().sendMessage("§B/wgk team accept");
		args.getSender().sendMessage("§B/wgk team decline");
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
		if (args.length() == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs(0);
		
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
		Group team = arena.getGroupManager().getTeamWithOutLeader();
		if (team == null)
		{
			p.sendMessage("§cBeide Team's haben einen Teamleiter.");
			return;
		}
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null)
		{
			p.sendMessage("§c"+p.getDisplayName()+" ist bereits in einem Team.");
			return;
		}
		team.add(p, true);
		p.teleport(arena.getGroupManager().getTeamSpawn(team.getTeamName()));
		p.sendMessage("§7Mit §B\"/wgk team invite <spieler>\" §7fügst du Spieler zu deinem Team hinzu.");
		p.sendMessage("§7Mit §B\"/wgk team remove <spieler>\" §7entfernst du Spieler aus deinem Team.");
		p.sendMessage("§7Mit §B\"/wgk team ready\" §7schaltest du dein Team bereit.");
		this.plugin.getScoreboard().addTeamMember(arena, team.getTeamMember(p), team.getTeamName());
	}
	
	@Command(name = "wgk.team.add", description = "Fügt ein Spieler zu deinem Team hinzu.",
			usage = "/wgk team add", permission="wargear.team.add", inGameOnly=true)
	public void add(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.length() == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs(0);
		
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
		
		Player senderPlayer = args.getPlayer();
		OfflinePlayer leader = null;
		Group team = null;
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null)
		{
			senderPlayer.sendMessage("§c"+p.getDisplayName()+" ist bereits in einem Team.");
			return;
		}
		if (args.length() == 1 || !senderPlayer.hasPermission("wargear.team.add.other")) {
			team = arena.getGroupManager().getTeamOfPlayer(senderPlayer);
			if (team != null && team.getTeamMember(senderPlayer) != null && !team.getTeamMember(senderPlayer).isTeamLeader())
			{
				senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
				return;
			}
			leader = senderPlayer;
		} else {
			String teamString = args.getArgs(1);
			PlayerRole teamName = PlayerRole.Team1;
			if (teamString.equalsIgnoreCase("team2")) {
				teamName = PlayerRole.Team2;
			}
			team = arena.getGroupManager().getTeamOfName(teamName);
			leader = team.getTeamLeader().getOfflinePlayer();
			if (leader == null) {
				senderPlayer.sendMessage("§cDas Team hat keinen Leader.");
			}
		}
		if(leader.isOnline()){
			Player onlineleader = (Player) team.getTeamLeader().getPlayer();
			p.sendMessage("§7Du bist jetzt im Team von §B"+onlineleader.getDisplayName()+"§7.");
		} else {
			p.sendMessage("§7Du bist jetzt im Team von §B"+leader.getName()+"§7.");
		}
		
		p.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
		team.add(p, false);
		this.plugin.getScoreboard().addTeamMember(arena, team.getTeamMember(p), team.getTeamName());
	}
	
	@Command(name = "wgk.team.remove", description = "Entfernt einen Spieler zu deinem Team hinzu.",
			usage = "/wgk team remove", permission="wargear.team.remove", inGameOnly=true)
	public void remove(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.length() == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs(0);
		
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

		Player senderPlayer = args.getPlayer();
		Group team = null;
		if (args.length() == 1 || !senderPlayer.hasPermission("wargear.team.remove.other")) {
			team = arena.getGroupManager().getTeamOfPlayer(senderPlayer);
			if (!team.getTeamMember(senderPlayer).isTeamLeader())
			{
				senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
				return;
			}
			if (team.getTeamMember(p) == null)
			{
				senderPlayer.sendMessage("§c"+p.getDisplayName()+" ist nicht in deinem Team.");
				return;
			}
			if (senderPlayer.getDisplayName().equalsIgnoreCase(playerName))
			{
				senderPlayer.sendMessage("§cDer Team Leiter kann sich nicht rauswerfen.");
				return;
			}
		} else {
			String teamString = args.getArgs(1);
			PlayerRole teamName = PlayerRole.Team1;
			if (teamString.equalsIgnoreCase("team2")) {
				teamName = PlayerRole.Team2;
			}
			team = arena.getGroupManager().getTeamOfName(teamName);
		}
		p.sendMessage("§7Du bist nicht mehr im Team von §8."+senderPlayer.getDisplayName());
		this.plugin.getScoreboard().removeTeamMember(arena, team.getTeamMember(p), team.getTeamName());
		team.remove(p);
	}
	
	@Command(name = "wgk.team.invite", description = "Lädt einen Spieler zu dein Team ein",
			usage = "/wgk team invite <name>", permission="wargear.team.invite", inGameOnly=true)
	public void invite(CommandArgs args) {
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.length() == 0)
		{
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs(0);
		
		if (arena.getState() == State.Running || arena.getState() == State.PreRunning)
		{
			args.getSender().sendMessage("§cWährend eines Fightes kannst du keine Mitglieder einladen.");
			return;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null)
		{
			args.getSender().sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}

		Player senderPlayer = args.getPlayer();
		OfflinePlayer leader = null;
		Group team = null;
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null)
		{
			senderPlayer.sendMessage("§c"+p.getDisplayName()+" ist bereits in einem Team.");
			return;
		}
		if (args.length() == 1 || !senderPlayer.hasPermission("wargear.team.invite.other")) {
			team = arena.getGroupManager().getTeamOfPlayer(senderPlayer);
			if (team != null && team.getTeamMember(senderPlayer) != null && !team.getTeamMember(senderPlayer).isTeamLeader())
			{
				senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
				return;
			}
			leader = senderPlayer;
		} else {
			String teamString = args.getArgs(1);
			PlayerRole teamName = PlayerRole.Team1;
			if (teamString.equalsIgnoreCase("team2")) {
				teamName = PlayerRole.Team2;
			}
			team = arena.getGroupManager().getTeamOfName(teamName);
			leader = team.getTeamLeader().getOfflinePlayer();
			if (leader == null) {
				senderPlayer.sendMessage("§cDas Team hat keinen Leader.");
			}
		}
		this.plugin.getInviteManager().addInvite(arena.getGroupManager().getGroupKey(leader), p);
		
	}
	
	@Command(name = "wgk.team.accept", description = "Akzeptiert eine Einladung.",
			usage = "/wgk team accept", permission="wargear.team.accept", inGameOnly=true)
	public void accept(CommandArgs args) { 
		this.plugin.getInviteManager().acceptInvite(args.getPlayer());
	}
	
	@Command(name = "wgk.team.decline", description = "Lehnt eine Einladung ab.",
			usage = "/wgk team decline", permission="wargear.team.decline", inGameOnly=true)
	public void decline(CommandArgs args) { 
		this.plugin.getInviteManager().declineInvite(args.getPlayer());
	}
	
	@Command(name = "wgk.team.leave", description = "Entfernt dich aus dem Team.",
			usage = "/wgk team leave", permission="wargear.team.leave", inGameOnly=true)
	public void leave(CommandArgs args)
	{
		Player senderPlayer = args.getPlayer();
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(senderPlayer);
		if (arena == null)
		{
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		
		if (arena.getState() != State.Setup)
		{
			args.getSender().sendMessage("§cWährend eines Fightes kannst du nicht dein Team verlassen.");
			return;
		}
		Group team = arena.getGroupManager().getTeamOfPlayer(senderPlayer);
		this.plugin.getScoreboard().removeTeamMember(arena, team.getTeamMember(senderPlayer), team.getTeamName());
		team.remove(senderPlayer);
		senderPlayer.sendMessage("§7Du bist raus aus dem Team.");
	}
	
	@Command(name = "wgk.team.ready", description = "Schaltet dein Team bereit",
			usage = "/wgk team ready", permission="wargear.team.ready", inGameOnly=true)
	public void ready(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null)
		{
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}	
		
		if (arena.getState() != State.Setup)
		{
			args.getSender().sendMessage("§cWährend eines Fightes kannst du das Team nicht bereit schalten.");
			return;
		}
		Player senderPlayer = args.getPlayer();
		Group team = arena.getGroupManager().getTeamOfPlayer(senderPlayer);
		if (team == null)
		{
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		team.setIsReady(!team.isReady());
		if (team.isReady())
		{
			senderPlayer.sendMessage("§7Dein Team ist bereit.");
			if (arena.getGroupManager().areBothTeamsReady())
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
