package com.pro_crafting.mc.wg;

import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.arena.State;
import com.pro_crafting.mc.wg.event.FightQuitEvent;
import com.pro_crafting.mc.wg.event.WinQuitEvent;
import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.group.GroupMember;
import com.pro_crafting.mc.wg.group.PlayerRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class OfflineManager implements Listener {

  private WarGear plugin;
  private Map<Group, List<OfflineRunable>> groupRunnables;
  private Map<String, List<OfflineRunable>> memberRunnables;
  private List<GroupMember> offlineGroupMembers;
  private BukkitTask task;
  private int kickTime;

  public OfflineManager(WarGear plugin) {
    this.plugin = plugin;
    this.groupRunnables = new HashMap<>();
    this.memberRunnables = new HashMap<>();
    this.offlineGroupMembers = new ArrayList<>();
    this.task = this.plugin.getServer().getScheduler()
        .runTaskTimer(plugin, new Runnable() {
          public void run() {
            checkTeamMembers();
          }
        }, 0, 20);
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.kickTime = this.plugin.getRepo().getOfflineKickTime();
  }

  public boolean run(OfflineRunable runable, Group group) {
    if (!group.isOnline()) {
      for (GroupMember member : group.getMembers()) {
        run(runable, member);
      }
    } else {
      runTeam(runable, group);
    }
    return group.isOnline();
  }

  public boolean runComplete(OfflineRunable runable, Group group) {
    if (!group.isOnline()) {
      if (!this.groupRunnables.containsKey(group)) {
        this.groupRunnables.put(group, new ArrayList<>());
      }
      this.groupRunnables.get(group).add(runable);
    } else {
      runTeam(runable, group);
    }
    return group.isOnline();
  }

  public boolean run(OfflineRunable runable, GroupMember member) {
    if (!member.isOnline()) {
      if (!this.memberRunnables.containsKey(member)) {
        this.memberRunnables.put(member.getOfflinePlayer().getUniqueId().toString(),
            new ArrayList<>());
      }
      this.memberRunnables.get(member.getOfflinePlayer().getUniqueId().toString()).add(runable);
    } else {
      runable.run(member);
    }
    return member.isOnline();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void handlePlayerQuits(PlayerQuitEvent event) {
    Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(
        event.getPlayer());
    if (arena != null) {
      GroupMember member = arena.getGroupManager()
          .getGroupMember(event.getPlayer());
      if (member.isAlive()) {
        this.offlineGroupMembers.add(member);
      }
    }
  }

  private boolean isTooLongOffline(GroupMember member) {
    return (System.currentTimeMillis() - member.getOfflinePlayer()
        .getLastPlayed()) > kickTime * 1000;
  }

  private void checkTeamMembers() {
    Iterator<GroupMember> offlineIterator = this.offlineGroupMembers
        .iterator();
    while (offlineIterator.hasNext()) {
      GroupMember current = offlineIterator.next();
      if (current.isOnline()) {
        offlineIterator.remove();
      } else if (isTooLongOffline(current)) {
        offlineIterator.remove();
        kickOfflineMember(current);
      }
    }

    Iterator<Entry<String, List<OfflineRunable>>> memberIterator = this.memberRunnables
        .entrySet().iterator();
    while (memberIterator.hasNext()) {
      Entry<String, List<OfflineRunable>> current = memberIterator
          .next();
      Arena arena = this.plugin.getArenaManager()
          .getArenaOfTeamMember(Bukkit.getOfflinePlayer(UUID.fromString(current.getKey())));
      if (arena == null) {
        memberIterator.remove();
        continue;
      }
      GroupMember member = arena.getGroupManager()
          .getGroupMember(Bukkit.getOfflinePlayer(UUID.fromString(current.getKey())));
      if (member.isOnline()) {
        for (OfflineRunable runable : current.getValue()) {
          runable.run(member);
        }
        memberIterator.remove();
      } else if (isTooLongOffline(member)) {
        memberIterator.remove();
        kickOfflineMember(member);
      }
    }

    Iterator<Entry<Group, List<OfflineRunable>>> groupIterator = this.groupRunnables
        .entrySet().iterator();
    while (groupIterator.hasNext()) {
      Entry<Group, List<OfflineRunable>> current = groupIterator.next();
      boolean everyoneOnline = true;
      for (GroupMember member : current.getKey().getMembers()) {
        if (isTooLongOffline(member)) {
          kickOfflineMember(member);
        } else if (!member.isOnline()) {
          everyoneOnline = false;
        }
      }
      if (everyoneOnline) {
        for (OfflineRunable runable : current.getValue()) {
          runTeam(runable, current.getKey());
        }
        groupIterator.remove();
      }
    }
  }

  private void kickOfflineMember(GroupMember offlineMember) {
    OfflinePlayer player = offlineMember.getOfflinePlayer();
    Arena arena = this.plugin.getArenaManager().getArenaOfTeamMember(player);
    if (arena == null) {
      return;
    }
    Group team = arena.getGroupManager().getGroupOfPlayer(player);
    if (arena.getState() == State.Setup) {
      if (offlineMember.isLeader()) {
        List<GroupMember> members = new ArrayList<>(team.getMembers());
        for (GroupMember member : members) {
          this.plugin.getScoreboard().removeTeamMember(arena, member, team.getRole());
          team.remove(member.getOfflinePlayer());
        }
      } else {
        team.remove(player);
        this.plugin.getScoreboard().removeTeamMember(arena, offlineMember, team.getRole());
      }
    } else {
      this.plugin.getScoreboard().removeTeamMember(arena, offlineMember, team.getRole());
      offlineMember.setAlive(false);
    }
    if ((!team.isAlive() || team.getMembers().size() == 0) && (arena.getState() == State.PreRunning
        || arena.getState() == State.Running)) {
      FightQuitEvent event = new WinQuitEvent(arena, "Gegnerisches Team ist offline.",
          arena.getGroupManager().getTeamOfGroup(team.getRole() == PlayerRole.Team1 ?
              PlayerRole.Team2 : PlayerRole.Team1), team, FightQuitReason.FightLeader);
      Bukkit.getPluginManager().callEvent(event);
    }
  }

  private void runTeam(OfflineRunable runable, Group group) {
    for (GroupMember member : group.getMembers()) {
      runMember(runable, member);
    }
  }

  private void runMember(OfflineRunable runable, GroupMember member) {
    if (member.isOnline()) {
      runable.run(member);
    }
  }
}
