package de.pro_crafting.wg.commands;

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pro_crafting.commandframework.Command;
import de.pro_crafting.commandframework.CommandArgs;
import de.pro_crafting.wg.Util;
import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.group.Group;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.PlayerRole;

public class TeamCommands {
	private WarGear plugin;
	
	public TeamCommands(WarGear plugin) {
		this.plugin = plugin;
	}
	
	@Command(name = "wgk.team", aliases = { "wgk.team.help" }, description = "Zeigt die Hilfe an.", usage = "/wgk team", permission="wargear.team")
	public void team(CommandArgs args) {
		CommandSender sender = args.getSender();
		sender.sendMessage("§c§LKein passender Befehl gefunden!");
		sender.sendMessage("§B/wgk team leader <playername>");
		sender.sendMessage("§B/wgk team add <playername>");
		sender.sendMessage("§B/wgk team add <playername> [team1|team2]");
		sender.sendMessage("§B/wgk team remove <playername>");
		sender.sendMessage("§B/wgk team remove <playername>");
		sender.sendMessage("§B/wgk team invite <playername> [team1|team2]");
		sender.sendMessage("§B/wgk team accept");
		sender.sendMessage("§B/wgk team decline");
		sender.sendMessage("§B/wgk team leave");
	}
	
	@Command(name = "wgk.team.leader", description = "Setzt den Leiter eines Teams.", usage = "/wgk team leader", permission="wargear.team.leader")
	public void leader(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null) {
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}
		if (args.length() == 0) {
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs(0);
		
		if (arena.getState() != State.Idle && arena.getState() != State.Setup) {
			args.getSender().sendMessage("§cEs läuft bereits ein Fight in "+arena.getName()+".");
			return;
		}
		if (arena.getState() == State.Idle) {
			arena.updateState(State.Setup);
		}
		
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null) {
			args.getSender().sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		Group team = arena.getGroupManager().getGroupWithOutLeader();
		if (team == null) {
			p.sendMessage("§cBeide Team's haben einen Teamleiter.");
			return;
		}
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null) {
			p.sendMessage("§c"+p.getDisplayName()+" ist bereits in einem Team.");
			return;
		}
		team.add(p, true);
		p.teleport(arena.getGroupManager().getGroupSpawn(team.getRole()));
		p.sendMessage("§7Mit §B\"/wgk team invite <spieler>\" §7lädst du Spieler in deinem Team ein.");
		p.sendMessage("§7Mit §B\"/wgk team remove <spieler>\" §7entfernst du Spieler aus deinem Team.");
		p.sendMessage("§7Mit §B\"/wgk team ready\" §7schaltest du dein Team bereit.");
		this.plugin.getScoreboard().addTeamMember(arena, team.getMember(p), team.getRole());
	}
	
	@Command(name = "wgk.team.add", description = "Fügt ein Spieler zu deinem Team hinzu.",
			usage = "/wgk team add", permission="wargear.team.add", inGameOnly=true)
	public void add(CommandArgs args)
	{
		Entry<PlayerGroupKey, Player> entry = canBeAdded(args);
		if (entry == null) {
			return;
		}
		PlayerGroupKey groupKey = entry.getKey();
		Player p = entry.getValue();
		
		p.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
		groupKey.getGroup().add(p, false);
		this.plugin.getScoreboard().addTeamMember(groupKey.getArena(), groupKey.getGroup().getMember(p), groupKey.getRole());
	}
	
	private Entry<PlayerGroupKey, Player> canBeAdded(CommandArgs args) {
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null) {
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return null;
		}
		if (args.length() == 0) {
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return null;
		}
		
		String playerName = args.getArgs(0);
		
		if (arena.getState() != State.Setup) {
			args.getSender().sendMessage("§cWährend eines Fightes kannst du keine Mitglieder einladen.");
			return null;
		}
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null) {
			args.getSender().sendMessage("§c"+playerName +" ist kein Spieler.");
			return null;
		}

		Player senderPlayer = args.getPlayer();
		OfflinePlayer leader = null;
		Group team = null;
		if (this.plugin.getArenaManager().getArenaOfTeamMember(p) != null) {
			senderPlayer.sendMessage("§c"+p.getDisplayName()+" ist bereits in einem Team.");
			return null;
		}
		if (args.length() == 1 || !senderPlayer.hasPermission("wargear.team.invite.other")) {
			team = arena.getGroupManager().getGroupOfPlayer(senderPlayer);
			if (team == null) {
				senderPlayer.sendMessage("§cDafür musst du in einem Team sein.");
				return null;
			}
			if (team.getMember(senderPlayer) != null && !team.getMember(senderPlayer).isLeader()) {
				senderPlayer.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
				return null;
			}
			leader = senderPlayer;
		} else {
			String teamString = args.getArgs(1);
			PlayerRole teamName = PlayerRole.Team1;
			if (teamString.equalsIgnoreCase("team2")) {
				teamName = PlayerRole.Team2;
			}
			team = arena.getGroupManager().getTeamOfGroup(teamName);
			leader = team.getLeader().getOfflinePlayer();
			if (leader == null) {
				senderPlayer.sendMessage("§cDas Team hat keinen Leader.");
			}
		}
		return new AbstractMap.SimpleEntry<PlayerGroupKey, Player>(arena.getGroupManager().getGroupKey(leader), p);
	}
	
	@Command(name = "wgk.team.invite", description = "Lädt einen Spieler zu dein Team ein",
			usage = "/wgk team invite <name>", permission="wargear.team.invite", inGameOnly=true)
	public void invite(CommandArgs args) {
		Entry<PlayerGroupKey, Player> entry = canBeAdded(args);
		if (entry == null) {
			return;
		}
		PlayerGroupKey groupKey = entry.getKey();
		Player p = entry.getValue();
		
		this.plugin.getInviteManager().addInvite(groupKey, p);	
	}
	
	@Command(name = "wgk.team.remove", description = "Entfernt einen Spieler zu deinem Team hinzu.",
			usage = "/wgk team remove", permission="wargear.team.remove", inGameOnly=true)
	public void remove(CommandArgs args) {
		if (args.length() == 0) {
			args.getSender().sendMessage("§cDu musst einen Spieler angeben.");
			return;
		}
		
		String playerName = args.getArgs(0);
		Player senderPlayer = args.getPlayer();
		
		Player p = this.plugin.getServer().getPlayer(playerName);
		if (p == null) {
			senderPlayer.sendMessage("§c"+playerName +" ist kein Spieler.");
			return;
		}
		
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(p);
		
		if (arena == null) {
			senderPlayer.sendMessage("§B"+p.getDisplayName()+"§c ist in keinem Team.");
			return;
		}
		
		if (arena.getState() != State.Setup) {
			senderPlayer.sendMessage("§cDer Fight von §B"+p.getDisplayName()+"§7 läuft zurzeit.");
			return;
		}
		
		PlayerGroupKey playerKey = arena.getGroupManager().getGroupKey(p);
		GroupMember teamleader = playerKey.getGroup().getLeader();
		
		if (senderPlayer.equals(teamleader.getOfflinePlayer().getUniqueId())) {
			if (p.getUniqueId().equals(teamleader.getOfflinePlayer().getUniqueId())) {
				senderPlayer.sendMessage("§cDer Team Leiter kann sich nicht selbst herauswerfen.");
				return;
			}
		} else if (!senderPlayer.hasPermission("wargear.team.remove.other")) {
			senderPlayer.sendMessage("§cDu bist nicht der Team Leiter.");
			return;
		}
		
		p.sendMessage("§7Du bist nicht mehr im Team von §B"+senderPlayer.getDisplayName());
		p.sendMessage("§B"+senderPlayer.getDisplayName()+"§7 ist nicht mehr in deinem Team.");
		this.plugin.getScoreboard().removeTeamMember(arena, playerKey.getGroup().getMember(p), playerKey.getRole());
		playerKey.getGroup().remove(p);
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
	public void leave(CommandArgs args) {
		Player senderPlayer = args.getPlayer();
		Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(senderPlayer);
		if (arena == null) {
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		
		if (arena.getState() != State.Setup) {
			args.getSender().sendMessage("§cWährend eines Fightes kannst du nicht dein Team verlassen.");
			return;
		}
		Group team = arena.getGroupManager().getGroupOfPlayer(senderPlayer);
		if (team == null) {
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		this.plugin.getScoreboard().removeTeamMember(arena, team.getMember(senderPlayer), team.getRole()());
		team.remove(senderPlayer);
		senderPlayer.sendMessage("§7Du bist raus aus dem Team.");
	}
	
	@Command(name = "wgk.team.ready", description = "Schaltet dein Team bereit",
			usage = "/wgk team ready", permission="wargear.team.ready", inGameOnly=true)
	public void ready(CommandArgs args)
	{
		Arena arena = Util.getArenaFromSender(plugin, args.getSender(), args.getArgs());
		if (arena == null) {
			args.getSender().sendMessage("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
			return;
		}	
		
		if (arena.getState() != State.Setup) {
			args.getSender().sendMessage("§cWährend eines Fightes kannst du das Team nicht bereit schalten.");
			return;
		}
		Player senderPlayer = args.getPlayer();
		Group team = arena.getGroupManager().getGroupOfPlayer(senderPlayer);
		if (team == null) {
			senderPlayer.sendMessage("§cDu bist in keinem Team.");
			return;
		}
		if (!team.getLeader().getOfflinePlayer().getUniqueId().equals(senderPlayer.getUniqueId())) {
			senderPlayer.sendMessage("§cDu bist nicht der Team Leiter.");
			return;
		}
		team.setIsReady(!team.isReady());
		if (team.isReady()) {
			senderPlayer.sendMessage("§7Dein Team ist bereit.");
			if (arena.getGroupManager().isReady()) {
				arena.startFight(args.getSender());
			}
		}
		else {
			senderPlayer.sendMessage("§7Dein Team ist nicht mehr bereit.");
		}
	}
}
