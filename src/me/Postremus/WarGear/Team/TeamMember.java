package me.Postremus.WarGear.Team;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamMember{

	private String playerName;
	private Boolean alive;
	private Boolean isTeamLeader;
	
	public TeamMember(Player player, boolean isTeamLeader)
	{
		this.playerName = player.getName();
		this.alive = true;
		this.isTeamLeader = isTeamLeader;
	}
	
	public Player getPlayer()
	{
		return Bukkit.getServer().getPlayer(playerName);
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
