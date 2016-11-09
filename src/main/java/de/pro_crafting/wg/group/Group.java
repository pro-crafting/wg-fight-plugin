package de.pro_crafting.wg.group;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Group  {
	private Map<UUID, GroupMember> member;
	private boolean isReady;
	protected PlayerRole role;
	private int cannons;
	
	public Group(PlayerRole role) {
		this.role = role;
		this.isReady = false;
		this.member = new HashMap<UUID, GroupMember>();
	}
	
	public void add(Player p, boolean isLeader) {
		this.member.put(p.getUniqueId(), new GroupMember(p, isLeader));
	}
	
	public void remove(OfflinePlayer p) {
		this.member.remove(p.getUniqueId());
	}
	
	public boolean isReady() {
		return this.isReady;
	}
	
	public void setIsReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	public PlayerRole getRole() {
		return this.role;
	}
	
	public Collection<GroupMember> getMembers() {
		return this.member.values();
	}
	
	public GroupMember getMember(OfflinePlayer player) {
		return this.member.get(player.getUniqueId());
	}
	
	public boolean isAlive() {
		for (GroupMember current : this.member.values()) {
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
		for (GroupMember current : this.member.values()) {
			if (current.isLeader()) {
				return current;
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isReady ? 1231 : 1237);
		result = prime * result
				+ ((member == null) ? 0 : member.hashCode());
		result = prime * result
				+ ((role == null) ? 0 : role.hashCode());
		return result;
	}

	public int getCannons() {
		return this.cannons;
	}

	public void setCannons(int cannons) {
		this.cannons = cannons;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (isReady != other.isReady)
			return false;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		if (role != other.role)
			return false;
		return true;
	}

	public boolean isOnline() {
		for (GroupMember member : this.member.values())
		{
			if (!member.isOnline())
			{
				return false;
			}
		}
		return true;
	}

	public void broadcast(String message) {
		for (GroupMember groupMember : this.member.values()) {
			if (groupMember.isOnline()) {
				groupMember.getPlayer().sendMessage(message);
			}
		}
	}
}
