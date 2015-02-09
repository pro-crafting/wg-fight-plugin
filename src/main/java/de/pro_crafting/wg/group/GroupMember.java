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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.alive == null) ? 0 : this.alive.hashCode());
		result = prime
				* result
				+ ((this.isLeader == null) ? 0 : this.isLeader
						.hashCode());
		result = prime * result
				+ ((this.playerId == null) ? 0 : this.playerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupMember other = (GroupMember) obj;
		if (this.alive == null) {
			if (other.alive != null)
				return false;
		} else if (!this.alive.equals(other.alive))
			return false;
		if (this.isLeader == null) {
			if (other.isLeader != null)
				return false;
		} else if (!this.isLeader.equals(other.isLeader))
			return false;
		if (this.playerId == null) {
			if (other.playerId != null)
				return false;
		} else if (!this.playerId.equals(other.playerId))
			return false;
		return true;
	}
}
