package de.pro_crafting.wg.group;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class GroupMember {
	private UUID playerId;
	private Boolean alive;

	private Boolean isLeader;
	
	public GroupMember(Player player, boolean isTeamLeader) {
		this.playerId = player.getUniqueId();
		this.alive = true;
		this.isLeader = isTeamLeader;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(this.playerId);
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(this.playerId);
	}
	
	public Boolean isAlive() {
		return this.alive;
	}
	
	public void setAlive(Boolean alive) {
		this.alive = alive;
	}
	
	public Boolean isLeader() {
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GroupMember that = (GroupMember) o;

		return playerId.equals(that.playerId);

	}

	@Override
	public int hashCode() {
		return playerId.hashCode();
	}
}
