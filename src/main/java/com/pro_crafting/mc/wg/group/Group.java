package com.pro_crafting.mc.wg.group;

import com.pro_crafting.mc.wg.event.GroupUpdateEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@EqualsAndHashCode
public class Group {

  protected PlayerGroupKey playerGroupKey;
  private Map<UUID, GroupMember> members;
  private boolean isReady;
  private int cannons;

  public Group(PlayerGroupKey playerGroupKey) {
    this.playerGroupKey = playerGroupKey;
    this.isReady = false;
    this.members = new HashMap<>();
  }

  public void add(Player p, boolean isLeader) {
    Map<UUID, GroupMember> newMembers = new HashMap<>(members);
    newMembers.put(p.getUniqueId(), new GroupMember(p, isLeader));

    Bukkit.getPluginManager()
        .callEvent(
            new GroupUpdateEvent(members.values(), newMembers.values(), this.getPlayerGroupKey()));

    this.members = newMembers;
  }

  public void remove(OfflinePlayer p) {
    Map<UUID, GroupMember> newMembers = new HashMap<>(members);
    newMembers.remove(p.getUniqueId());

    Bukkit.getPluginManager()
        .callEvent(
            new GroupUpdateEvent(members.values(), newMembers.values(), this.getPlayerGroupKey()));

    this.members = newMembers;
  }

  public boolean isReady() {
    return this.isReady;
  }

  public void setIsReady(boolean isReady) {
    this.isReady = isReady;
  }

  public PlayerRole getRole() {
    return this.playerGroupKey.getRole();
  }

  public PlayerGroupKey getPlayerGroupKey() {
    return playerGroupKey;
  }

  public Collection<GroupMember> getMembers() {
    return this.members.values();
  }

  public GroupMember getMember(OfflinePlayer player) {
    return this.members.get(player.getUniqueId());
  }

  public boolean isAlive() {
    for (GroupMember current : this.members.values()) {
      if (current.isAlive()) {
        return true;
      }
    }
    return false;
  }

  public boolean hasLeader() {
    return getLeader() != null;
  }

  public GroupMember getLeader() {
    for (GroupMember current : this.members.values()) {
      if (current.isLeader()) {
        return current;
      }
    }
    return null;
  }

  public int getCannons() {
    return this.cannons;
  }

  public void setCannons(int cannons) {
    this.cannons = cannons;
  }

  public boolean isOnline() {
    for (GroupMember member : this.members.values()) {
      if (!member.isOnline()) {
        return false;
      }
    }
    return true;
  }

  public void broadcast(String message) {
    for (GroupMember groupMember : this.members.values()) {
      if (groupMember.isOnline()) {
        groupMember.getPlayer().sendMessage(message);
      }
    }
  }
}
