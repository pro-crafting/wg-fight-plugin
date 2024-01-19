package com.pro_crafting.mc.wg;

import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.arena.State;
import com.pro_crafting.mc.wg.event.FightQuitEvent;
import com.pro_crafting.mc.wg.event.WinQuitEvent;
import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.group.GroupMember;
import com.pro_crafting.mc.wg.group.PlayerRole;

import java.time.Duration;
import java.time.LocalDateTime;
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

public class OfflineManager implements Listener {

  private final WarGear plugin;

  private final Map<UUID, List<OfflineRunnable>> playerRunnables;

  private final Map<UUID, LocalDateTime> playerLastLogoutTime;


  private final List<GroupMember> offlineGroupMembers;
  private final int kickTime;

  public OfflineManager(WarGear plugin) {
    this.plugin = plugin;
    this.playerRunnables = new HashMap<>();
    this.playerLastLogoutTime = new HashMap<>();
    this.offlineGroupMembers = new ArrayList<>();
    this.plugin.getServer().getScheduler()
        .runTaskTimer(plugin, this::checkTeamMembers, 0, 20);
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.kickTime = this.plugin.getRepo().getOfflineKickTime();
  }

  public boolean queueOnlineExecution(OfflineRunnable runable, Group group) {
    if (!group.isOnline()) {
      for (GroupMember member : group.getMembers()) {
        queueOnlineExecution(runable, member);
      }
    } else {
      run(runable, group);
    }
    return group.isOnline();
  }

  private boolean queueOnlineExecution(OfflineRunnable runnable, GroupMember member) {
    if (!member.isOnline()) {
      List<OfflineRunnable> runnables = this.playerRunnables.computeIfAbsent(member.getOfflinePlayer().getUniqueId(), uuid -> new ArrayList<>());
      runnables.add(runnable);
    } else {
      runnable.run(member);
    }
    return member.isOnline();
  }


  private void run(OfflineRunnable runable, Group group) {
    for (GroupMember member : group.getMembers()) {
      runMember(runable, member);
    }
  }

  private void runMember(OfflineRunnable runable, GroupMember member) {
    if (member.isOnline()) {
      runable.run(member);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void handlePlayerQuits(PlayerQuitEvent event) {
    playerLastLogoutTime.put(event.getPlayer().getUniqueId(), LocalDateTime.now());

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

  private boolean isOfflineForTooLong(UUID playerUUID) {
    LocalDateTime lastLogoutTime = playerLastLogoutTime.get(playerUUID);
    if (lastLogoutTime == null) {
      return false;
    }

    return Duration.between(lastLogoutTime, LocalDateTime.now()).toSeconds() > kickTime;
  }

  private void checkTeamMembers() {

    // Check which players are still fighting but offline, and kick them
    Iterator<GroupMember> offlineIterator = this.offlineGroupMembers
        .iterator();
    while (offlineIterator.hasNext()) {
      GroupMember current = offlineIterator.next();
      if (current.isOnline()) {
        offlineIterator.remove();
      } else if (isOfflineForTooLong(current.getOfflinePlayer().getUniqueId())) {
        offlineIterator.remove();
        kickOfflineMember(current);
      }
    }


    // check if any of the member runnables can be executed?
    Iterator<Entry<UUID, List<OfflineRunnable>>> memberIterator = this.playerRunnables
        .entrySet().iterator();
    while (memberIterator.hasNext()) {
      Entry<UUID, List<OfflineRunnable>> current = memberIterator
          .next();
      Arena arena = this.plugin.getArenaManager()
          .getArenaOfTeamMember(Bukkit.getOfflinePlayer(current.getKey()));
      if (arena == null) {
        memberIterator.remove();
        continue;
      }

      GroupMember member = arena.getGroupManager()
          .getGroupMember(Bukkit.getOfflinePlayer(current.getKey()));
      if (member.isOnline()) {
        for (OfflineRunnable runnable : current.getValue()) {
          runnable.run(member);
        }
        memberIterator.remove();
      } else if (isOfflineForTooLong(current.getKey())) {
        memberIterator.remove();
        kickOfflineMember(member);
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

    if ((!team.isAlive() || team.getMembers().isEmpty()) && (arena.getState() == State.PreRunning
        || arena.getState() == State.Running)) {
      FightQuitEvent event = new WinQuitEvent(arena, "Gegnerisches Team ist offline.",
          arena.getGroupManager().getTeamOfGroup(team.getRole() == PlayerRole.Team1 ?
              PlayerRole.Team2 : PlayerRole.Team1), team, FightQuitReason.FightLeader);
      Bukkit.getPluginManager().callEvent(event);
    }
  }

}
