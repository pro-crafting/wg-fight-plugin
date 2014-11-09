package de.pro_crafting.wg.group;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Group 
{
	private Map<UUID, GroupMember> groupMember;
	private boolean isReady;
	private PlayerRole teamName;
	
	public Group(PlayerRole teamName)
	{
		this.teamName = teamName;
		isReady = false;
		this.groupMember = new HashMap<UUID, GroupMember>();
	}
	
	public void add(Player p, boolean isLeader)
	{
		this.groupMember.put(p.getUniqueId(), new GroupMember(p, isLeader));
	}
	
	public void remove(OfflinePlayer p)
	{
		this.groupMember.remove(p.getUniqueId());
	}
	
	public boolean isReady()
	{
		return this.isReady;
	}
	
	public void setIsReady(boolean isReady)
	{
		this.isReady = isReady;
	}
	
	public PlayerRole getTeamName()
	{
		return this.teamName;
	}
	
	public Collection<GroupMember> getTeamMembers()
	{
		return this.groupMember.values();
	}
	
	public GroupMember getTeamMember(OfflinePlayer player)
	{
		return this.groupMember.get(player.getUniqueId());
	}
	
	public boolean isAlive()
	{
		for (GroupMember current : this.groupMember.values())
		{
			if (current.isAlive())
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean hasTeamLeader()
	{
		for (GroupMember current : this.groupMember.values())
		{
			if (current.isTeamLeader())
			{
				return true;
			}
		}
		return false;
	}
	
	public GroupMember getTeamLeader() {
		for (GroupMember current : this.groupMember.values())
		{
			if (current.isTeamLeader())
			{
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
				+ ((groupMember == null) ? 0 : groupMember.hashCode());
		result = prime * result
				+ ((teamName == null) ? 0 : teamName.hashCode());
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
		Group other = (Group) obj;
		if (isReady != other.isReady)
			return false;
		if (groupMember == null) {
			if (other.groupMember != null)
				return false;
		} else if (!groupMember.equals(other.groupMember))
			return false;
		if (teamName != other.teamName)
			return false;
		return true;
	}

	public boolean isOnline() {
		for (GroupMember member : this.groupMember.values())
		{
			if (!member.isOnline())
			{
				return false;
			}
		}
		return true;
	}
}
