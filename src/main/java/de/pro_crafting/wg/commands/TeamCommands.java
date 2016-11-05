package de.pro_crafting.wg.commands;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Switch;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.commands.provider.Sender;
import de.pro_crafting.wg.group.Group;
import de.pro_crafting.wg.group.GroupMember;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.PlayerRole;
import de.pro_crafting.wg.group.invitation.InvitationType;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Map.Entry;

public class TeamCommands {
    private WarGear plugin;

    public TeamCommands(WarGear plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"", "help"}, desc = "Zeigt die Hilfe an.")
    @Require("wargear.team")
    public void team(CommandSender sender) {
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

    @Command(aliases = {"leader"}, desc = "Setzt den Leiter eines Teams.", min = 1)
    @Require("wargear.team.leader")
    public void leader(@Sender Player player, @Switch('a') Arena arena, Player leader) {
        if (arena.getState() != State.Idle && arena.getState() != State.Setup) {
            player.sendMessage("§cEs läuft bereits ein Fight in " + arena.getName() + ".");
            return;
        }
        if (arena.getState() == State.Idle) {
            arena.updateState(State.Setup);
        }

        Group team = arena.getGroupManager().getGroupWithOutLeader();
        if (team == null) {
            player.sendMessage("§cBeide Team's haben einen Teamleiter.");
            return;
        }
        if (this.plugin.getArenaManager().getArenaOfTeamMember(leader) != null) {
            player.sendMessage("§c" + leader.getDisplayName() + " ist bereits in einem Team.");
            return;
        }
        team.add(leader, true);
        leader.teleport(arena.getGroupManager().getGroupSpawn(team.getRole()));
        leader.sendMessage("§7Mit §B\"/wgk team invite <spieler>\" §7lädst du Spieler in deinem Team ein.");
        leader.sendMessage("§7Mit §B\"/wgk team remove <spieler>\" §7entfernst du Spieler aus deinem Team.");
        leader.sendMessage("§7Mit §B\"/wgk team ready\" §7schaltest du dein Team bereit.");
        this.plugin.getScoreboard().addTeamMember(arena, team.getMember(leader), team.getRole());
        arena.updateRegion(PlayerRole.Team1);
        arena.updateRegion(PlayerRole.Team2);
    }

    @Command(aliases = {"add"}, desc = "Fügt ein Spieler zu deinem Team hinzu.", min = 1)
    @Require("wargear.team")
    public void add(@Sender Player player, @Switch('a') Arena arena, Player member, @Optional PlayerRole forTeam) {
        Entry<PlayerGroupKey, Player> entry = canBeAdded(player, arena, member, forTeam);
        if (entry == null) {
            return;
        }
        PlayerGroupKey groupKey = entry.getKey();
        Player p = entry.getValue();

        p.sendMessage("§7Mit §8\"/wgk team leave\" §7verlässt du das Team.");
        groupKey.getGroup().add(p, false);
        this.plugin.getScoreboard().addTeamMember(groupKey.getArena(), groupKey.getGroup().getMember(p), groupKey.getRole());
        groupKey.getArena().updateRegion(PlayerRole.Team1);
        groupKey.getArena().updateRegion(PlayerRole.Team2);
    }

    private Entry<PlayerGroupKey, Player> canBeAdded(Player player, Arena arena, Player member, PlayerRole teamName) {
        if (arena.getState() != State.Setup) {
            player.sendMessage("§cWährend eines Fightes kannst du keine Mitglieder einladen.");
            return null;
        }

        OfflinePlayer leader;
        if (this.plugin.getArenaManager().getArenaOfTeamMember(member) != null) {
            player.sendMessage("§c" + member.getDisplayName() + " ist bereits in einem Team.");
            return null;
        }
        if (teamName == null || !player.hasPermission("wargear.team.invitation.other")) {
            Group team = arena.getGroupManager().getGroupOfPlayer(player);
            if (team == null) {
                player.sendMessage("§cDafür musst du in einem Team sein.");
                return null;
            }
            if (team.getMember(player) != null && !team.getMember(player).isLeader()) {
                player.sendMessage("§cDer Command muss vom Teamleiter ausgeführt werden.");
                return null;
            }
            leader = player;
        } else {
            Group team = arena.getGroupManager().getTeamOfGroup(teamName);
            leader = team.getLeader().getOfflinePlayer();
            if (leader == null) {
                player.sendMessage("§cDas Team hat keinen Leader.");
            }
        }
        return new AbstractMap.SimpleEntry<>(arena.getGroupManager().getGroupKey(leader), member);
    }

    @Command(aliases = {"invite"}, desc = "Lädt einen Spieler zu dein Team ein", min = 1)
    @Require("wargear.team.invite")
    public void invite(@Sender Player player, @Switch('a') Arena arena, Player member, @Optional PlayerRole forTeam) {
        Entry<PlayerGroupKey, Player> entry = canBeAdded(player, arena, member, forTeam);
        if (entry == null) {
            return;
        }
        PlayerGroupKey groupKey = entry.getKey();
        Player p = entry.getValue();

        this.plugin.getInviteManager().addInvitation(groupKey.getGroup().getLeader(), p, InvitationType.Team);
        groupKey.getArena().updateRegion(PlayerRole.Team1);
        groupKey.getArena().updateRegion(PlayerRole.Team2);
    }

    @Command(aliases = {"remove"}, desc = "Entfernt einen Spieler aus deinem Team.", min = 1)
    @Require("wargear.team.remove")
    public void remove(@Sender Player player, Player member) {
        Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(member);

        if (arena == null) {
            player.sendMessage("§B" + member.getDisplayName() + "§c ist in keinem Team.");
            return;
        }

        if (arena.getState() != State.Setup) {
            player.sendMessage("§cDer Fight von §B" + member.getDisplayName() + "§7 läuft zurzeit.");
            return;
        }

        PlayerGroupKey playerKey = arena.getGroupManager().getGroupKey(member);
        GroupMember teamleader = playerKey.getGroup().getLeader();

        if (player.equals(teamleader.getOfflinePlayer())) {
            if (member.getUniqueId().equals(teamleader.getOfflinePlayer().getUniqueId())) {
                player.sendMessage("§cDer Team Leiter kann sich nicht selbst herauswerfen.");
                return;
            }
        } else if (!player.hasPermission("wargear.team.remove.other")) {
            player.sendMessage("§cDu bist nicht der Team Leiter.");
            return;
        }

        this.plugin.getScoreboard().removeTeamMember(arena, playerKey.getGroup().getMember(member), playerKey.getRole());
        playerKey.getGroup().remove(member);
        arena.updateRegion(PlayerRole.Team1);
        arena.updateRegion(PlayerRole.Team2);
        member.sendMessage("§7Du bist nicht mehr im Team von §B" + player.getDisplayName());
        member.sendMessage("§B" + player.getDisplayName() + "§7 ist nicht mehr in deinem Team.");
    }

    @Command(aliases = {"accept"}, desc = "Akzeptiert eine Einladung.")
    @Require("wargear.team.accept")
    public void accept(@Sender Player player) {
        this.plugin.getInviteManager().acceptInvite(player);
    }

    @Command(aliases = {"decline"}, desc = "Lehnt eine Einladung ab.")
    @Require("wargear.team.decline")
    public void decline(@Sender Player player) {
        this.plugin.getInviteManager().declineInvitation(player);
    }

    @Command(aliases = {"leave"}, desc = "Entfernt dich aus dem Team.")
    @Require("wargear.team.leave")
    public void leave(@Sender Player player, @Switch('a') Arena arena) {
        if (arena.getState() != State.Setup) {
            player.sendMessage("§cWährend eines Fightes kannst du dein Team nicht verlassen.");
            return;
        }

        Group team = arena.getGroupManager().getGroupOfPlayer(player);
        if (team == null) {
            player.sendMessage("§cDu bist in keinem Team.");
            return;
        }
        this.plugin.getScoreboard().removeTeamMember(arena, team.getMember(player), team.getRole());
        team.remove(player);
        player.sendMessage("§7Du bist raus aus dem Team.");
        arena.updateRegion(PlayerRole.Team1);
        arena.updateRegion(PlayerRole.Team2);
    }

    @Command(aliases = {"ready"}, desc = "Schaltet dein Team bereit")
    @Require("wargear.team.ready")
    public void ready(@Sender Player player, @Switch('a') Arena arena) {
        if (arena.getState() != State.Setup) {
            player.sendMessage("§cWährend eines Fightes kannst du das Team nicht bereit schalten.");
            return;
        }
        Group team = arena.getGroupManager().getGroupOfPlayer(player);
        if (team == null) {
            player.sendMessage("§cDu bist in keinem Team.");
            return;
        }
        if (!team.getLeader().getOfflinePlayer().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cDu bist nicht der Team Leiter.");
            return;
        }
        team.setIsReady(!team.isReady());
        if (team.isReady()) {
            player.sendMessage("§7Dein Team ist bereit.");
            if (arena.getGroupManager().isReady()) {
                arena.startFight(player);
            }
        } else {
            player.sendMessage("§7Dein Team ist nicht mehr bereit.");
        }
    }
}