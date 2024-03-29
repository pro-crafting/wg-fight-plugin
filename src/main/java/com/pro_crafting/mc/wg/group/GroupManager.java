package com.pro_crafting.mc.wg.group;

import com.pro_crafting.mc.wg.OfflineRunnable;
import com.pro_crafting.mc.wg.Util;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.event.GroupUpdateEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class GroupManager {

  private WarGear plugin;
  private Arena arena;
  private Group group1;
  private Group group2;
  private Map<PlayerRole, PlayerGroupKey> groupKeys;

  public GroupManager(WarGear plugin, Arena arena) {
    this.plugin = plugin;
    this.arena = arena;

    this.groupKeys = new EnumMap<>(PlayerRole.class);
    this.groupKeys.put(PlayerRole.Team1, new PlayerGroupKey(arena, PlayerRole.Team1));
    this.groupKeys.put(PlayerRole.Team2, new PlayerGroupKey(arena, PlayerRole.Team2));
    this.groupKeys.put(PlayerRole.Viewer, new PlayerGroupKey(arena, PlayerRole.Viewer));

    this.group1 = new Group(getGroupKey(PlayerRole.Team1));
    this.group2 = new Group(getGroupKey(PlayerRole.Team2));
  }

  public Location getGroupSpawn(PlayerRole role) {
    if (role == PlayerRole.Team1) {
      return arena.getRepo().getTeam1Warp();
    } else {
      return arena.getRepo().getTeam2Warp();
    }
  }

  public void quitFight() {
    quiteFightForGroup(this.group1);
    quiteFightForGroup(this.group2);

    Bukkit.getPluginManager().callEvent(
        new GroupUpdateEvent(Collections.unmodifiableCollection(this.group1.getMembers()),
            new ArrayList<>(),
            getGroupKey(PlayerRole.Team1))
    );
    this.group1 = new Group(getGroupKey(PlayerRole.Team1));

    Bukkit.getPluginManager().callEvent(
        new GroupUpdateEvent(Collections.unmodifiableCollection(this.group2.getMembers()),
            new ArrayList<>(),
            getGroupKey(PlayerRole.Team2))
    );
    this.group2 = new Group(getGroupKey(PlayerRole.Team2));
  }


  private void quiteFightForGroup(Group group) {
    final Location teleportLocation = this.arena.getRepo().getSpawnWarp();
    OfflineRunnable fightQuiter = member -> {
      member.getPlayer().getInventory().clear();
      member.getPlayer().teleport(teleportLocation, TeleportCause.PLUGIN);
    };
    this.plugin.getOfflineManager().queueOnlineExecution(fightQuiter, group);
  }

  public void sendWinnerOutput(PlayerRole role) {
    String group = getRolePrefix(role) + "§2" + concateGroupPlayers(this.getTeamOfGroup(role));
    this.arena.broadcastMessage(group + " hat gewonnen!");
  }

  private String concateGroupPlayers(Group group) {
    String ret = "";
    for (GroupMember member : group.getMembers()) {
      if (member.isOnline()) {
        ret += member.getPlayer().getDisplayName() + " ";
      } else {
        ret += member.getOfflinePlayer().getName() + " ";
      }

    }
    return ret.trim();
  }

  public void sendGroupOutput() {
    String group1 = arena.getRepo().getTeam1Prefix() +
        getRolePrefix(PlayerRole.Team1) + ChatColor.YELLOW + "" + ChatColor.ITALIC
        + concateGroupPlayers(this.getGroup1());
    String group2 = arena.getRepo().getTeam2Prefix() +
        getRolePrefix(PlayerRole.Team2) + ChatColor.YELLOW + "" + ChatColor.ITALIC
        + concateGroupPlayers(this.getGroup2());
    this.arena.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + group1);
    this.arena.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + group2);
  }

  private String getRolePrefix(PlayerRole role) {
    return "§8[" + getPrefix(role) + role.toString() + "§8]";
  }

  public void healGroup(Group group) {
    OfflineRunnable healer = member -> Util.makeHealthy(member.getPlayer());
    this.plugin.getOfflineManager().queueOnlineExecution(healer, group);
  }

  public Group getTeamOfGroup(PlayerRole role) {
    if (role == PlayerRole.Team1) {
      return this.group1;
    } else {
      return this.group2;
    }
  }

  public String getPrefix(PlayerRole role) {
    if (role == PlayerRole.Team1) {
      return this.arena.getRepo().getTeam1Prefix();
    } else if (role == PlayerRole.Team2) {
      return this.arena.getRepo().getTeam2Prefix();
    }
    return "§7";
  }

  public PlayerRole getRole(OfflinePlayer player) {
    Group group = getGroupOfPlayer(player);
    if (group != null) {
      return group.getRole();
    }
    return PlayerRole.Viewer;
  }

  public Group getGroupOfPlayer(OfflinePlayer p) {
    if (this.group1.getMember(p) != null) {
      return this.group1;
    } else if (this.group2.getMember(p) != null) {
      return this.group2;
    }
    return null;
  }

  public Group getGroupWithOutLeader() {
    if (!this.group1.hasLeader()) {
      return this.group1;
    } else if (!this.group2.hasLeader()) {
      return this.group2;
    }
    return null;
  }

  public boolean isReady() {
    return this.group1.isReady() && this.group2.isReady();
  }

  public boolean isAlive(Player p) {
    if (this.getGroupOfPlayer(p) == null || this.getGroupOfPlayer(p).getMember(p) == null) {
      return false;
    }
    return this.getGroupOfPlayer(p).getMember(p).isAlive();
  }

  public GroupMember getGroupMember(OfflinePlayer p) {
    if (this.group1.getMember(p) != null) {
      return this.group1.getMember(p);
    }
    if (this.group2.getMember(p) != null) {
      return this.group2.getMember(p);
    }
    return null;
  }

  public Group getGroup1() {
    return group1;
  }

  public Group getGroup2() {
    return group2;
  }

  public PlayerGroupKey getGroupKey(OfflinePlayer player) {
    return getGroupKey(this.getRole(player));
  }

  public PlayerGroupKey getGroupKey(PlayerRole role) {
    return this.groupKeys.get(role);
  }
}
