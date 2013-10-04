package me.Postremus.WarGear;

import org.bukkit.entity.Player;

public class TeamMember{

	private Player player;
	private TeamNames team;
	private Boolean alive;
	
	public TeamMember(Player player, TeamNames team)
	{
		this.player = player;
		this.team = team;
		this.alive = true;
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public TeamNames getTeam()
	{
		return this.team;
	}
	
	public Boolean getAlive()
	{
		return this.alive;
	}
	
	public void setAlive(Boolean alive)
	{
		this.alive = alive;
	}
}
