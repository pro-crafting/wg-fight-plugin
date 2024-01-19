package com.pro_crafting.mc.wg.group;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GroupMember {

  private UUID playerUUID;
  private boolean alive;
  private boolean isLeader;

  public GroupMember(UUID playerUUID, boolean isTeamLeader) {
    this.playerUUID = playerUUID;
    this.alive = true;
    this.isLeader = isTeamLeader;
  }

  public Player getPlayer() {
    return this.getOfflinePlayer().getPlayer();
  }

  public OfflinePlayer getOfflinePlayer() {
    return Bukkit.getOfflinePlayer(this.playerUUID);
  }

  public boolean isAlive() {
    return this.alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public boolean isLeader() {
    return this.isLeader;
  }

  public boolean isOnline() {
    return this.getOfflinePlayer().isOnline();
  }

  public String getName() {
    return this.isOnline() ? this.getPlayer().getDisplayName() : this.getOfflinePlayer().getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GroupMember that = (GroupMember) o;

    return playerUUID.equals(that.playerUUID);

  }

  @Override
  public int hashCode() {
    return playerUUID.hashCode();
  }

  @Override
  public String toString() {
    return "GroupMember{" +
        "player=" + playerUUID +
        ", alive=" + alive +
        ", isLeader=" + isLeader +
        '}';
  }
}
