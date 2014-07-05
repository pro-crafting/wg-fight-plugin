package de.pro_crafting.wg.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class WgTeam 
{
	private Map<UUID, TeamMember> teamMember;
	private boolean isReady;
	private TeamNames teamName;
	
	public WgTeam(TeamNames teamName)
	{
		this.teamName = teamName;
		isReady = false;
		this.teamMember = new HashMap<UUID, TeamMember>();
	}
	
	public void add(Player p, boolean isLeader)
	{
		this.teamMember.put(p.getUniqueId(), new TeamMember(p, isLeader));
	}
	
	public void remove(OfflinePlayer p)
	{
		this.teamMember.remove(p.getUniqueId());
	}
	
	public boolean isReady()
	{
		return this.isReady;
	}
	
	public void setIsReady(boolean isReady)
	{
		this.isReady = isReady;
	}
	
	public TeamNames getTeamName()
	{
		return this.teamName;
	}
	
	public Map<UUID, TeamMember> getTeamMembers()
	{
		return this.teamMember;
	}
	
	public TeamMember getTeamMember(OfflinePlayer player)
	{
		return this.teamMember.get(player.getUniqueId());
	}
	
	public boolean isAlive()
	{
		for (TeamMember current : this.teamMember.values())
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
		for (TeamMember current : this.teamMember.values())
		{
			if (current.isTeamLeader())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isReady ? 1231 : 1237);
		result = prime * result
				+ ((teamMember == null) ? 0 : teamMember.hashCode());
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
		WgTeam other = (WgTeam) obj;
		if (isReady != other.isReady)
			return false;
		if (teamMember == null) {
			if (other.teamMember != null)
				return false;
		} else if (!teamMember.equals(other.teamMember))
			return false;
		if (teamName != other.teamName)
			return false;
		return true;
	}

	public boolean isOnline() {
		for (TeamMember member : this.teamMember.values())
		{
			if (!member.isOnline())
			{
				return false;
			}
		}
		return true;
	}
}
