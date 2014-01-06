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
		for (TeamMember tmp : this.teamMember)
		{
			if (tmp.getPlayer().getName().equalsIgnoreCase(p.getName()))
			{
				return tmp;
			}
		}
		return null;
	}
	
	public boolean isSomoneAlive()
	{
		for (TeamMember tmp : this.teamMember)
		{
			if (tmp.getAlive())
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean hasTeamLeader()
	{
		for (TeamMember tmp : this.teamMember)
		{
			if (tmp.getIsTeamLeader())
			{
				return true;
			}
		}
		return false;
	}
}
