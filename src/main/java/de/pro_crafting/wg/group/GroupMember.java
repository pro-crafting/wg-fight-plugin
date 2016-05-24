package de.pro_crafting.wg.group;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class GroupMember {
	private OfflinePlayer player;
	private Boolean alive;
	private Boolean isLeader;
	
	public GroupMember(Player player, boolean isTeamLeader) {
		this.player = player;
		this.alive = true;
		this.isLeader = isTeamLeader;
	}
	
	public Player getPlayer() {
		return this.getOfflinePlayer().getPlayer();
	}
	
	public OfflinePlayer getOfflinePlayer() {
		Player player = Bukkit.getPlayer(this.player.getUniqueId());
		if (player != null) {
			this.player = player;
		}
		return this.player.getPlayer();
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

		return player.equals(that.player);

	}

	@Override
	public int hashCode() {
		return player.hashCode();
	}

	@Override
	public String toString() {
		return "GroupMember{" +
				"player=" + player +
				", alive=" + alive +
				", isLeader=" + isLeader +
				'}';
	}
}
