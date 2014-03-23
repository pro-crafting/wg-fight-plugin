package me.Postremus.WarGear.Team;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class WgTeam 
{
	private List<TeamMember> teamMember;
	private boolean isReady;
	private TeamNames teamName;
	
	public WgTeam(TeamNames teamName)
	{
		this.teamName = teamName;
		isReady = false;
		this.teamMember = new ArrayList<TeamMember>();
	}
	
	public void add(Player p, boolean isLeader)
	{
		this.teamMember.add(new TeamMember(p, isLeader));
	}
	
	public void remove(Player p)
	{
		TeamMember toRemove = this.getTeamMember(p);
		if (toRemove != null)
		{
			this.teamMember.remove(toRemove);
		}
	}
	
	public boolean getIsReady()
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
	
	public List<TeamMember> getTeamMembers()
	{
		return this.teamMember;
	}
	
	public TeamMember getTeamMember(Player p)
	{
		for (TeamMember current : this.teamMember)
		{
			if (current.getPlayerName().equalsIgnoreCase(p.getName()))
			{
				return current;
			}
		}
		return null;
	}
	
	public boolean isSomoneAlive()
	{
		for (TeamMember current : this.teamMember)
		{
			if (current.getAlive())
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean hasTeamLeader()
	{
		for (TeamMember current : this.teamMember)
		{
			if (current.getIsTeamLeader())
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
}
