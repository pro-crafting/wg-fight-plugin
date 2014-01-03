package me.Postremus.WarGear;

import org.bukkit.entity.Player;

public class TeamMember{

	private Player player;
	private Boolean alive;
	private Boolean isTeamLeader;
	
	public TeamMember(Player player, boolean isTeamLeader)
	{
		this.player = player;
		this.alive = true;
		this.isTeamLeader = isTeamLeader;
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public Boolean getAlive()
	{
		return this.alive;
	}
	
	public Boolean getIsTeamLeader()
	{
		return this.isTeamLeader;
	}
	
	public void setAlive(Boolean alive)
	{
		this.alive = alive;
	}
}
